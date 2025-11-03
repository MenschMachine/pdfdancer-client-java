package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.client.http.*;
import com.tfc.pdf.pdfdancer.api.common.model.*;
import com.tfc.pdf.pdfdancer.api.common.model.text.Paragraph;
import com.tfc.pdf.pdfdancer.api.common.request.*;
import com.tfc.pdf.pdfdancer.api.common.response.CommandResult;
import com.tfc.pdf.pdfdancer.api.common.response.DocumentSnapshot;
import com.tfc.pdf.pdfdancer.api.common.response.PageSnapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.tfc.pdf.pdfdancer.api.common.model.ObjectType.FORM_FIELD;
import static com.tfc.pdf.pdfdancer.api.common.util.ExceptionUtils.wrapCheckedException;
import static com.tfc.pdf.pdfdancer.api.common.util.FileUtils.writeBytesToFile;

/**
 * REST API client for interacting with the PDFDancer PDF manipulation service.
 * This client provides a convenient Java interface for performing PDF operations
 * including session management, object searching, manipulation, and retrieval.
 * Handles authentication, session lifecycle, and HTTP communication transparently.
 */
public class PDFDancer {
    public static final double DEFAULT_EPSILON = 0.01;
    private static final String ENV_TOKEN = "PDFDANCER_TOKEN";
    private static final URI DEFAULT_BASE_URI = URI.create("http://localhost:8080");
    private static final String ALL_TYPES_KEY = "__ALL__";
    private static final String TYPES_PARAGRAPH = "PARAGRAPH";
    private static final String TYPES_TEXT_LINE = "TEXT_LINE";
    /**
     * Authentication token for API access.
     */
    private final String token;
    /**
     * Unique session identifier for this PDF editing session.
     */
    private final String sessionId;
    /**
     * HTTP client for making requests to the PDFDancer API.
     */
    private final PdfDancerHttpClient httpClient;
    private final PdfDancerHttpClient.Blocking blockingClient;
    private final Map<String, DocumentSnapshot> documentSnapshotCache = new HashMap<>();
    private final Map<PageSnapshotKey, PageSnapshot> pageSnapshotCache = new HashMap<>();
    private final Map<DocumentSnapshotKey, TypedDocumentSnapshot<?>> typedDocumentSnapshotCache = new HashMap<>();
    private final Map<TypedPageSnapshotKey, TypedPageSnapshot<?>> typedPageSnapshotCache = new HashMap<>();

    /**
     * Private constructor for factory methods.
     * Initializes the client with a session ID from either existing PDF or blank PDF creation.
     *
     * @param token     authentication token for API access
     * @param sessionId the session ID from the API
     * @param client    HTTP client for API communication
     */
    private PDFDancer(String token, String sessionId, PdfDancerHttpClient client) {
        this.token = token;
        this.sessionId = sessionId;
        this.httpClient = client;
        this.blockingClient = client.toBlocking();
    }

    /**
     * Creates a new PDFDancer client by uploading an existing PDF file.
     * Uses default HTTP client configured for localhost:8080.
     *
     * @param token   authentication token for API access
     * @param pdfFile PDF file to upload and process
     * @return PDFDancer client instance with an active session
     */
    @SuppressWarnings("unused")
    public static PDFDancer createSession(String token, File pdfFile) {
        return createSession(token, readFile(pdfFile), getDefaultClient());
    }

    /**
     * Creates a new PDFDancer client by uploading an existing PDF file.
     * Uses default HTTP client configured for localhost:8080.
     * Authentication:
     * - If PDFDANCER_TOKEN is set, uses it
     * - Otherwise, automatically issues an anonymous token and proceeds
     *
     * @param pdfFile PDF file to upload and process
     * @return PDFDancer client instance with an active session
     */
    @SuppressWarnings("unused")
    public static PDFDancer createSession(File pdfFile) {
        PdfDancerHttpClient client = getDefaultClient();
        byte[] bytes = readFile(pdfFile);
        String token = envTokenOrNull();
        return token != null ? createSession(token, bytes, client) : createAnonSession(bytes, client);
    }

    /**
     * Creates a new PDFDancer client by uploading an existing PDF file.
     * Uses default HTTP client configured for localhost:8080.
     * Authentication:
     * - If PDFDANCER_TOKEN is set, uses it
     * - Otherwise, automatically issues an anonymous token and proceeds
     *
     * @param pdfFile PDF file to upload and process
     * @return PDFDancer client instance with an active session
     */
    @SuppressWarnings("unused")
    public static PDFDancer createSession(String pdfFile) {
        return createSession(new File(pdfFile));
    }

    /**
     * Creates a new PDFDancer client by uploading an existing PDF.
     * This method initializes the client, uploads the PDF data to create
     * a new session, and prepares the client for PDF manipulation operations.
     *
     * @param token    authentication token for API access
     * @param bytesPDF PDF file data as byte array
     * @param client   HTTP client for API communication
     * @return PDFDancer client instance with an active session
     */
    public static PDFDancer createSession(String token, byte[] bytesPDF, PdfDancerHttpClient client) {
        String sessionId = uploadPdfForSession(token, bytesPDF, client);
        return new PDFDancer(token, sessionId, client);
    }

    @SuppressWarnings("unused")
    public static PDFDancer createSession(String token, byte[] bytesPDF, HttpClient httpClient) {
        return createSession(token, bytesPDF, PdfDancerHttpClient.create(httpClient, DEFAULT_BASE_URI));
    }

    @SuppressWarnings("unused")
    public static PDFDancer createSession(String token, byte[] bytesPDF, HttpClient httpClient, URI baseUri) {
        return createSession(token, bytesPDF, PdfDancerHttpClient.create(httpClient, baseUri));
    }

    /**
     * Creates a new PDFDancer client with a blank PDF.
     * Uses default page size (A4), orientation (PORTRAIT), and page count (1).
     * Uses default HTTP client configured for localhost:8080.
     * Authentication:
     * - If PDFDANCER_TOKEN is set, uses it
     * - Otherwise, automatically issues an anonymous token and proceeds
     *
     * @return PDFDancer client instance with a blank PDF session
     */
    @SuppressWarnings("unused")
    public static PDFDancer createNew() {
        PdfDancerHttpClient client = getDefaultClient();
        String token = envTokenOrNull();
        if (token == null) {
            token = obtainAnonymousToken(client);
        }
        return createNew(token, PageSize.A4, Orientation.PORTRAIT, 1, client);
    }

    static PDFDancer createAnonSession(byte[] testPdf, PdfDancerHttpClient client) {
        String fingerprint = buildFingerprint();
        MutableHttpRequest<?> request = HttpRequest.POST("/keys/anon", null)
                .header("X-Fingerprint", fingerprint);
        AnonTokenResponse token = client.toBlocking().retrieve(request, AnonTokenResponse.class);
        return createSession(token.token(), testPdf, client);
    }

    @SuppressWarnings("unused")
    static PDFDancer createAnonSession(byte[] testPdf, HttpClient httpClient) {
        return createAnonSession(testPdf, PdfDancerHttpClient.create(httpClient, DEFAULT_BASE_URI));
    }

    @SuppressWarnings("unused")
    static PDFDancer createAnonSession(byte[] testPdf, HttpClient httpClient, URI baseUri) {
        return createAnonSession(testPdf, PdfDancerHttpClient.create(httpClient, baseUri));
    }

    /**
     * Creates a new PDFDancer client with a blank PDF using custom parameters.
     * Uses default HTTP client configured for localhost:8080.
     *
     * @param token            authentication token for API access
     * @param pageSize         page size (use PageSize.A4, PageSize.LETTER, PageSize.LEGAL, or PageSize.custom(width, height))
     * @param orientation      orientation (PORTRAIT or LANDSCAPE)
     * @param initialPageCount number of initial blank pages
     * @return PDFDancer client instance with a blank PDF session
     */
    @SuppressWarnings("unused")
    public static PDFDancer createNew(String token, PageSize pageSize,
                                      Orientation orientation,
                                      int initialPageCount) {
        return createNew(token, pageSize, orientation, initialPageCount, getDefaultClient());
    }

    /**
     * Creates a new PDFDancer client with a blank PDF using custom parameters.
     * Uses default HTTP client configured for localhost:8080.
     * Authentication:
     * - If PDFDANCER_TOKEN is set, uses it
     * - Otherwise, automatically issues an anonymous token and proceeds
     *
     * @param pageSize         page size (use PageSize.A4, PageSize.LETTER, PageSize.LEGAL, or PageSize.custom(width, height))
     * @param orientation      orientation (PORTRAIT or LANDSCAPE)
     * @param initialPageCount number of initial blank pages
     * @return PDFDancer client instance with a blank PDF session
     */
    @SuppressWarnings("unused")
    public static PDFDancer createNew(PageSize pageSize,
                                      Orientation orientation,
                                      int initialPageCount) {
        PdfDancerHttpClient client = getDefaultClient();
        String token = envTokenOrNull();
        if (token == null) {
            token = obtainAnonymousToken(client);
        }
        return createNew(token, pageSize, orientation, initialPageCount, client);
    }

    /**
     * Creates a new PDFDancer client with a blank PDF using custom HTTP client.
     *
     * @param token            authentication token for API access
     * @param pageSize         page size (use PageSize.A4, PageSize.LETTER, PageSize.LEGAL, or PageSize.custom(width, height))
     * @param orientation      orientation (PORTRAIT or LANDSCAPE)
     * @param initialPageCount number of initial blank pages
     * @param client           HTTP client for API communication
     * @return PDFDancer client instance with a blank PDF session
     */
    public static PDFDancer createNew(String token, PageSize pageSize,
                                      Orientation orientation,
                                      int initialPageCount, PdfDancerHttpClient client) {
        String sessionId = createBlankPdfSession(token, pageSize, orientation, initialPageCount, client);
        return new PDFDancer(token, sessionId, client);
    }

    @SuppressWarnings("unused")
    public static PDFDancer createNew(String token, PageSize pageSize,
                                      Orientation orientation,
                                      int initialPageCount, HttpClient httpClient) {
        return createNew(token, pageSize, orientation, initialPageCount, PdfDancerHttpClient.create(httpClient, DEFAULT_BASE_URI));
    }

    @SuppressWarnings("unused")
    public static PDFDancer createNew(String token, PageSize pageSize,
                                      Orientation orientation,
                                      int initialPageCount, HttpClient httpClient, URI baseUri) {
        return createNew(token, pageSize, orientation, initialPageCount, PdfDancerHttpClient.create(httpClient, baseUri));
    }

    /**
     * Creates a default HTTP client configured for localhost development.
     * This method provides a preconfigured client for connecting to a local
     * PDFDancer API server running on the standard development port.
     *
     * @return HTTP client configured for localhost:8080
     * @throws RuntimeException if URL creation fails
     */
    private static PdfDancerHttpClient getDefaultClient() {
        return PdfDancerHttpClient.createDefault(DEFAULT_BASE_URI);
    }

    private static String envTokenOrNull() {
        String token = System.getenv(ENV_TOKEN);
        return (token == null || token.isBlank()) ? null : token;
    }

    private static String obtainAnonymousToken(PdfDancerHttpClient client) {
        String fingerprint = buildFingerprint();
        MutableHttpRequest<?> request = HttpRequest.POST("/keys/anon", null)
                .header("X-Fingerprint", fingerprint);
        AnonTokenResponse token = client.toBlocking().retrieve(request, AnonTokenResponse.class);
        return token.token();
    }

    private static String buildFingerprint() {
        try {
            String ip = getLocalIp();
            String uid = getUid();
            String osType = System.getProperty("os.name", "unknown");
            String sdkLanguage = "java";
            String timezone = getTimezone();
            String localeStr = getLocaleStr();
            String hostname = getHostname();
            String installSalt = getOrCreateSalt();
            String data = ip + uid + osType + sdkLanguage + timezone + localeStr + hostname + installSalt;
            return sha256Hex(data);
        } catch (Exception e) {
            // Fallback to a deterministic minimal fingerprint if anything goes wrong
            String fallback = Optional.ofNullable(System.getProperty("user.name")).orElse("unknown")
                    + Optional.ofNullable(System.getProperty("os.name")).orElse("unknown")
                    + Optional.of(ZoneId.systemDefault().getId()).orElse("UTC");
            return sha256Hex(fallback);
        }
    }

    private static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getUid() {
        try {
            String user = System.getProperty("user.name");
            if (user == null || user.isBlank()) user = System.getenv("USER");
            if (user == null || user.isBlank()) user = System.getenv("USERNAME");
            return (user == null || user.isBlank()) ? "unknown" : user;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getTimezone() {
        try {
            return ZoneId.systemDefault().getId();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getLocaleStr() {
        try {
            Locale loc = Locale.getDefault();
            String s = (loc == null) ? "" : loc.toString();
            return s.isBlank() ? "en_US" : s;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            String env = Optional.ofNullable(System.getenv("HOSTNAME"))
                    .orElseGet(() -> System.getenv("COMPUTERNAME"));
            return (env == null || env.isBlank()) ? "unknown" : env;
        }
    }

    private static String getOrCreateSalt() {
        try {
            Path home = Paths.get(System.getProperty("user.home"));
            Path dir = home.resolve(".pdfdancer");
            Path file = dir.resolve("fingerprint.salt");
            if (Files.exists(file)) {
                try {
                    String s = Files.readString(file).trim();
                    if (!s.isBlank()) return s;
                } catch (Exception ignored) {
                }
            }
            String salt = UUID.randomUUID().toString();
            try {
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
                Files.writeString(file, salt);
            } catch (Exception ignored) {
            }
            return salt;
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // Extremely unlikely; return a simple fallback
            return Integer.toHexString(s.hashCode());
        }
    }

    /**
     * Uploads PDF data to create a new session on the server.
     * This method uploads the PDF file to the server, which analyzes its structure
     * and creates a session for subsequent manipulation operations.
     *
     * @param token  authentication token for the request
     * @param pdf    PDF file data as byte array
     * @param client HTTP client for API communication
     * @return unique session identifier for future operations
     */
    private static String uploadPdfForSession(String token, byte[] pdf, PdfDancerHttpClient client) {
        MultipartBody body = MultipartBody.builder()
                .addPart("pdf", "test.pdf", MediaType.APPLICATION_PDF_TYPE, pdf)
                .build();
        return client.toBlocking().retrieve(
                HttpRequest.POST("/session/create", body)
                        .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
                        .bearerAuth(token),
                String.class
        );
    }

    /**
     * Creates a blank PDF session on the server.
     * This method requests the server to create a new blank PDF with the specified
     * parameters and returns a session ID for subsequent manipulation operations.
     *
     * @param token            authentication token for the request
     * @param pageSize         page size (standard or custom)
     * @param orientation      orientation
     * @param initialPageCount number of initial blank pages
     * @param client           HTTP client for API communication
     * @return unique session identifier for future operations
     */
    private static String createBlankPdfSession(String token, com.tfc.pdf.pdfdancer.api.common.model.PageSize pageSize,
                                                com.tfc.pdf.pdfdancer.api.common.model.Orientation orientation,
                                                int initialPageCount, PdfDancerHttpClient client) {
        return client.toBlocking().retrieve(
                HttpRequest.POST("/session/new",
                                new com.tfc.pdf.pdfdancer.api.common.request.CreateBlankPdfRequest(pageSize, orientation, initialPageCount))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token),
                String.class
        );
    }

    /**
     * Reads a file completely into a byte array.
     * This utility method safely reads the entire contents of a file
     * using proper resource management with try-with-resources.
     *
     * @param file the file to read
     * @return complete file contents as byte array
     * @throws RuntimeException if file reading fails
     */
    private static byte[] readFile(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            try {
                return Objects.requireNonNull(inputStream).readAllBytes();
            } catch (IOException e) {
                throw wrapCheckedException(e);
            }
        } catch (IOException e) {
            throw wrapCheckedException(e);
        }
    }

    public String getToken() {
        return token;
    }

    /**
     * Deletes a page from the PDF document.
     * This method removes the specified page from the document permanently,
     * updating the page numbering for subsequent pages.
     *
     * @param pageRef reference to the page to be deleted
     * @return true if the page was successfully deleted, false otherwise
     */
    public Boolean deletePage(ObjectRef pageRef) {
        String path = "/pdf/page/delete";
        Boolean result = blockingClient.retrieve(
                HttpRequest.DELETE(path, pageRef)
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Boolean.class
        );
        invalidateSnapshotCaches();
        return result;
    }

    /**
     * Searches for PDF objects matching the specified criteria.
     * This method provides flexible search capabilities across all PDF content,
     * allowing filtering by object type and getPosition constraints.
     *
     * @param type     the type of objects to find (null for all types)
     * @param position positional constraints for the search (null for all positions)
     * @return list of object references matching the search criteria
     */
    private List<ObjectRef> find(ObjectType type, Position position) {
        String path = "/pdf/find";
        return blockingClient.retrieve(
                HttpRequest.POST(path, new FindRequest(type, position, null))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Argument.listOf(ObjectRef.class)
        );
    }

    /**
     * Deletes the specified PDF object from the document.
     * This method permanently removes the object from the PDF document,
     * updating the document structure accordingly.
     *
     * @param objectRef reference to the object to be deleted
     * @return true if the object was successfully deleted, false otherwise
     */
    protected boolean delete(ObjectRef objectRef) {
        String path = "/pdf/delete";
        Boolean result = blockingClient.retrieve(
                HttpRequest.DELETE(path, new DeleteRequest(objectRef))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Boolean.class
        );
        invalidateSnapshotCaches();
        return Boolean.TRUE.equals(result);
    }

    /**
     * Retrieves references to all pages in the PDF document.
     * This method returns a list of object references for every page
     * in the current document, enabling page-level operations.
     *
     * @return list of object references for all pages in the document
     */
    public List<PageRef> getPages() {
        String path = "/pdf/page/find";
        return blockingClient.retrieve(
                HttpRequest.POST(path, null)
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Argument.listOf(PageRef.class)
        );
    }

    /**
     * Retrieves a reference to a specific page by its page number.
     * This method returns an object reference for the specified page,
     * enabling targeted page operations.
     *
     * @param pageIndex the page number to retrieve (1-based indexing)
     * @return object reference for the specified page, or null if not found
     */
    public ObjectRef getPage(int pageIndex) {
        String path = "/pdf/page/find?pageIndex=" + pageIndex;
        List<ObjectRef> result = blockingClient.retrieve(
                HttpRequest.POST(path, null)
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Argument.listOf(ObjectRef.class)
        );
        if (result.isEmpty()) return null;
        else return result.get(0);
    }

    /**
     * Downloads the current state of the PDF document with all modifications applied.
     * This method retrieves the complete PDF file as binary data, reflecting
     * all changes made during the current session.
     *
     * @return PDF file data as byte array with all session modifications applied
     */
    public byte[] getFileBytes() {
        String path = "/session/" + sessionId + "/pdf";
        return blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token),
                byte[].class
        );
    }

    /**
     * Moves a PDF object to a new getPosition within the document.
     * This method relocates the specified object to the given coordinates
     * while preserving all other object properties.
     *
     * @param objectRef reference to the object to be moved
     * @param position  new getPosition for the object
     * @return true if the object was successfully moved, false otherwise
     */
    protected Boolean move(ObjectRef objectRef, Position position) {
        String path = "/pdf/move";
        Boolean result = blockingClient.retrieve(
                HttpRequest.PUT(path, new MoveRequest(objectRef, position))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Boolean.class
        );
        invalidateSnapshotCaches();
        return result;
    }

    /**
     * Adds an image to the PDF document at the specified getPosition.
     * This convenience method sets the image getPosition and adds it to the document
     * in a single operation.
     *
     * @param image    the image object to add to the document
     * @param position the getPosition where the image should be placed
     * @return true if the image was successfully added, false otherwise
     */
    protected boolean addImage(Image image, Position position) {
        image.setPosition(position);
        return this.addImage(image);
    }

    /**
     * Adds an image to the PDF document.
     * This method adds the image object to the document at its current getPosition.
     * The image must have a valid getPosition atPosition before calling this method.
     *
     * @param image the image object to add (must have getPosition atPosition)
     * @return true if the image was successfully added, false otherwise
     * @throws IllegalArgumentException if the image getPosition is null
     */
    protected boolean addImage(Image image) {
        if (image.getPosition() == null) {
            throw new IllegalArgumentException("Image getPosition is null");
        }
        return addObject(image);
    }

    protected Boolean addObject(PDFObject object) {
        String path = "/pdf/add";
        MutableHttpRequest<AddRequest> request = HttpRequest.POST(path, new AddRequest(object))
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(token)
                .header("X-Session-Id", sessionId);
        Boolean result = this.retrieve(request, Boolean.class);
        invalidateSnapshotCaches();
        return result;
    }

    private <T> T retrieve(MutableHttpRequest<?> request, Class<T> returnType) {
        return blockingClient.retrieve(request, returnType);
    }

    private void invalidateSnapshotCaches() {
        documentSnapshotCache.clear();
        pageSnapshotCache.clear();
        typedDocumentSnapshotCache.clear();
        typedPageSnapshotCache.clear();
    }

    private String normalizeTypes(String types) {
        if (types == null || types.isBlank()) {
            return ALL_TYPES_KEY;
        }
        String normalized = Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.joining(","));
        return normalized.isBlank() ? ALL_TYPES_KEY : normalized;
    }

    private DocumentSnapshot getDocumentSnapshotCached(String types) {
        String key = normalizeTypes(types);
        DocumentSnapshot cached = documentSnapshotCache.get(key);
        if (cached != null) {
            return cached;
        }
        DocumentSnapshot snapshot = fetchDocumentSnapshot(types);
        documentSnapshotCache.put(key, snapshot);
        // Seed page cache with the pages from this document snapshot for the same type filter
        List<PageSnapshot> pages = snapshot.pages();
        for (int i = 0; i < pages.size(); i++) {
            pageSnapshotCache.put(new PageSnapshotKey(i, key), pages.get(i));
        }
        return snapshot;
    }

    private PageSnapshot getPageSnapshotCached(int pageIndex, String types) {
        String key = normalizeTypes(types);
        PageSnapshotKey cacheKey = new PageSnapshotKey(pageIndex, key);
        PageSnapshot cached = pageSnapshotCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        PageSnapshot snapshot = fetchPageSnapshot(pageIndex, types);
        pageSnapshotCache.put(cacheKey, snapshot);
        return snapshot;
    }

    private DocumentSnapshot fetchDocumentSnapshot(String types) {
        String path = "/pdf/document/snapshot";
        if (types != null && !types.isBlank()) {
            path += "?types=" + types;
        }
        return blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                DocumentSnapshot.class
        );
    }

    private PageSnapshot fetchPageSnapshot(int pageIndex, String types) {
        String path = "/pdf/page/" + pageIndex + "/snapshot";
        if (types != null && !types.isBlank()) {
            path += "?types=" + types;
        }
        return blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                PageSnapshot.class
        );
    }

    private <T extends ObjectRef> TypedDocumentSnapshot<T> getTypedDocumentSnapshot(Class<T> elementClass, String types) {
        String key = normalizeTypes(types);
        DocumentSnapshotKey cacheKey = new DocumentSnapshotKey(elementClass, key);
        @SuppressWarnings("unchecked")
        TypedDocumentSnapshot<T> cached = (TypedDocumentSnapshot<T>) typedDocumentSnapshotCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        TypedDocumentSnapshot<T> snapshot = fetchTypedDocumentSnapshot(elementClass, types);
        typedDocumentSnapshotCache.put(cacheKey, snapshot);
        List<TypedPageSnapshot<T>> pages = snapshot.getPages();
        for (int i = 0; i < pages.size(); i++) {
            typedPageSnapshotCache.put(new TypedPageSnapshotKey(i, elementClass, key), pages.get(i));
        }
        return snapshot;
    }

    private <T extends ObjectRef> TypedPageSnapshot<T> getTypedPageSnapshot(int pageIndex,
                                                                            Class<T> elementClass,
                                                                            String types) {
        String key = normalizeTypes(types);
        TypedPageSnapshotKey cacheKey = new TypedPageSnapshotKey(pageIndex, elementClass, key);
        @SuppressWarnings("unchecked")
        TypedPageSnapshot<T> cached = (TypedPageSnapshot<T>) typedPageSnapshotCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        TypedPageSnapshot<T> snapshot = fetchTypedPageSnapshot(pageIndex, elementClass, types);
        typedPageSnapshotCache.put(cacheKey, snapshot);
        return snapshot;
    }

    private <T extends ObjectRef> TypedDocumentSnapshot<T> fetchTypedDocumentSnapshot(Class<T> elementClass, String types) {
        String path = "/pdf/document/snapshot";
        if (types != null && !types.isBlank()) {
            path += "?types=" + types;
        }
        return blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Argument.of(TypedDocumentSnapshot.class, elementClass)
        );
    }

    private <T extends ObjectRef> TypedPageSnapshot<T> fetchTypedPageSnapshot(int pageIndex,
                                                                              Class<T> elementClass,
                                                                              String types) {
        String path = "/pdf/page/" + pageIndex + "/snapshot";
        if (types != null && !types.isBlank()) {
            path += "?types=" + types;
        }
        return blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Argument.of(TypedPageSnapshot.class, elementClass)
        );
    }

    private <T extends ObjectRef> List<T> getTypedElements(TypedPageSnapshot<T> page, Class<T> elementClass) {
        List<T> rawElements = page.getElements();
        if (rawElements == null || rawElements.isEmpty()) {
            return Collections.emptyList();
        }
        for (Object element : rawElements) {
            if (!elementClass.isInstance(element)) {
                throw new IllegalStateException("Expected elements of type " + elementClass.getName() + " but got " + element.getClass().getName());
            }
        }
        return rawElements;
    }

    private <T extends ObjectRef> List<T> flattenTypedDocument(TypedDocumentSnapshot<T> snapshot, Class<T> elementClass) {
        List<T> results = new ArrayList<>();
        for (TypedPageSnapshot<T> page : snapshot.getPages()) {
            List<T> elements = getTypedElements(page, elementClass);
            results.addAll(elements);
        }
        return results;
    }

    private List<FormFieldRef> collectFormFieldRefsFromDocument() {
        List<FormFieldRef> results = new ArrayList<>();
        for (Form.FormType filter : Form.FormType.values()) {
            TypedDocumentSnapshot<FormFieldRef> snapshot = getTypedDocumentSnapshot(FormFieldRef.class, filter.name());
            List<FormFieldRef> elements = flattenTypedDocument(snapshot, FormFieldRef.class);
            elements.stream()
                    .map(ref -> adjustFormFieldType(ref, filter))
                    .filter(Objects::nonNull)
                    .forEach(results::add);
        }
        return results;
    }

    private List<FormFieldRef> collectFormFieldRefsFromPage(int pageIndex) {
        List<FormFieldRef> results = new ArrayList<>();
        for (Form.FormType filter : Form.FormType.values()) {
            TypedPageSnapshot<FormFieldRef> snapshot = getTypedPageSnapshot(pageIndex, FormFieldRef.class, filter.name());
            List<FormFieldRef> elements = getTypedElements(snapshot, FormFieldRef.class);
            elements.stream()
                    .map(ref -> adjustFormFieldType(ref, filter))
                    .filter(Objects::nonNull)
                    .forEach(results::add);
        }
        return results;
    }

    private FormFieldRef adjustFormFieldType(FormFieldRef ref, Form.FormType filter) {
        if (ref == null) {
            return null;
        }
        ObjectType desiredType;
        try {
            desiredType = ObjectType.valueOf(filter.name());
        } catch (IllegalArgumentException ex) {
            desiredType = ref.getType();
        }
        if (desiredType == null || desiredType == ref.getType()) {
            return ref;
        }
        return new FormFieldRef(ref.getInternalId(), ref.getPosition(), desiredType, ref.getObjectRefType(), ref.getName(), ref.getValue());
    }

    private TextTypeObjectRef ensureTextType(TextTypeObjectRef ref, ObjectType desiredType) {
        if (ref == null) {
            return null;
        }
        if (desiredType == null || desiredType == ref.getType()) {
            return ref;
        }
        List<TextTypeObjectRef> children = ref.getChildren();
        List<TextTypeObjectRef> childCopies = (children == null || children.isEmpty()) ? null : new ArrayList<>(children);
        List<Double> lineSpacings = ref.getLineSpacings();
        List<Double> lineSpacingCopy = (lineSpacings == null || lineSpacings.isEmpty()) ? null : new ArrayList<>(lineSpacings);
        return new TextTypeObjectRef(
                ref.getInternalId(),
                ref.getPosition(),
                desiredType,
                ref.getObjectRefType(),
                ref.getFontName(),
                ref.getFontSize(),
                ref.getText(),
                lineSpacingCopy,
                ref.getColor(),
                ref.getStatus(),
                childCopies
        );
    }

    private List<TextTypeObjectRef> findParagraphs(Position position) {
        String path = "/pdf/find";
        return blockingClient.retrieve(
                HttpRequest.POST(path, new FindRequest(ObjectType.PARAGRAPH, position, null))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Argument.listOf(TextTypeObjectRef.class)
        );
    }

    private List<TextTypeObjectRef> findTextLines(Position position) {
        String path = "/pdf/find";
        return blockingClient.retrieve(
                HttpRequest.POST(path, new FindRequest(ObjectType.TEXT_LINE, position, null))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Argument.listOf(TextTypeObjectRef.class)
        );
    }

    private List<FormFieldRef> findFormFields(Position position) {
        String path = "/pdf/find";
        return blockingClient.retrieve(
                HttpRequest.POST(path, new FindRequest(FORM_FIELD, position, null))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Argument.listOf(FormFieldRef.class)
        );
    }

    @SuppressWarnings("unused")
    private List<FormFieldRef> findFormFields() {
        return findFormFields(null);
    }

    private List<ObjectRef> collectAllElements(DocumentSnapshot snapshot) {
        List<ObjectRef> results = new ArrayList<>();
        if (snapshot == null || snapshot.pages() == null) {
            return results;
        }
        for (PageSnapshot page : snapshot.pages()) {
            if (page == null || page.elements() == null) {
                continue;
            }
            for (ObjectRef element : page.elements()) {
                if (element != null && element.getType() != null) {
                    results.add(element);
                }
            }
        }
        return results;
    }

    private List<ObjectRef> collectObjectsByType(DocumentSnapshot snapshot, Set<ObjectType> types) {
        List<ObjectRef> results = new ArrayList<>();
        if (snapshot == null || snapshot.pages() == null) {
            return results;
        }
        for (PageSnapshot page : snapshot.pages()) {
            accumulateObjectsByType(results, page, types);
        }
        return results;
    }

    private List<ObjectRef> collectObjectsByType(PageSnapshot snapshot, Set<ObjectType> types) {
        List<ObjectRef> results = new ArrayList<>();
        accumulateObjectsByType(results, snapshot, types);
        return results;
    }

    private void accumulateObjectsByType(List<ObjectRef> target, PageSnapshot snapshot, Set<ObjectType> types) {
        if (snapshot == null || snapshot.elements() == null) {
            return;
        }
        for (ObjectRef element : snapshot.elements()) {
            if (element == null) {
                continue;
            }
            ObjectType elementType = element.getType();
            if (elementType == null) {
                continue;
            }
            if (types.contains(elementType)) {
                target.add(element);
            }
        }
    }

    private boolean containsPoint(ObjectRef ref, double x, double y) {
        return containsPoint(ref, x, y, DEFAULT_EPSILON);
    }

    private boolean containsPoint(ObjectRef ref, double x, double y, double epsilon) {
        Position position = ref.getPosition();
        if (position == null || position.getBoundingRect() == null) {
            return false;
        }
        BoundingRect rect = position.getBoundingRect();
        double rectX = rect.getX();
        double rectY = rect.getY();
        double rectWidth = rect.getWidth();
        double rectHeight = rect.getHeight();
        // Apply epsilon tolerance
        double minX = rectX - epsilon;
        double maxX = rectX + rectWidth + epsilon;
        double minY = rectY - epsilon;
        double maxY = rectY + rectHeight + epsilon;
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    private boolean startsWithIgnoreCase(String value, String prefix) {
        if (value == null || prefix == null) {
            return false;
        }
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    protected boolean modifyParagraph(ObjectRef ref, Paragraph newParagraph) {
        String path = "/pdf/modify";
        MutableHttpRequest<ModifyRequest> request = HttpRequest.PUT(path, new ModifyRequest(ref, newParagraph))
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(token)
                .header("X-Session-Id", sessionId);
        CommandResult result = retrieve(request, CommandResult.class);
        invalidateSnapshotCaches();
        return result.success();
    }

    protected boolean modifyTextLine(ObjectRef ref, String newTextLine) {
        String path = "/pdf/text/line";
        CommandResult result = blockingClient.retrieve(
                HttpRequest.PUT(path, new ModifyTextRequest(ref, newTextLine))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                CommandResult.class
        );
        invalidateSnapshotCaches();
        return result.success();
    }

    protected boolean modifyParagraph(ObjectRef ref, String newText) {
        String path = "/pdf/text/paragraph";
        CommandResult result = blockingClient.retrieve(
                HttpRequest.PUT(path, new ModifyTextRequest(ref, newText))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                CommandResult.class
        );
        invalidateSnapshotCaches();
        return result.success();
    }

    protected boolean addParagaph(Paragraph newParagraph) {
        if (newParagraph.getPosition() == null) {
            throw new IllegalArgumentException("Paragraph getPosition is null");
        }
        if (newParagraph.getPosition().getPageIndex() == null) {
            throw new IllegalArgumentException("Paragraph getPosition page number is null");
        }
        if (newParagraph.getPosition().getPageIndex() < 0) {
            throw new IllegalArgumentException("Paragraph getPosition page number is less than 0");
        }
        return addObject(newParagraph);
    }

    public List<Font> findFonts(String fontName, int fontSize) {
        String path = "/font/find?fontName=" + fontName;
        List<String> fonts = blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Argument.listOf(String.class)
        );
        return fonts.stream().map(name -> new Font(name, fontSize)).collect(Collectors.toUnmodifiableList());
    }

    public String registerFont(File ttfFile) {
        String path = "/font/register";
        byte[] bytes = readFile(ttfFile);
        MultipartBody body = MultipartBody.builder()
                .addPart("ttfFile", ttfFile.getName(), new MediaType("font/ttf"), bytes)
                .build();
        return blockingClient.retrieve(
                HttpRequest.POST(path, body)
                        .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                String.class
        );
    }

    public ParagraphBuilder newParagraph() {
        return new ParagraphBuilder(this);
    }

    public ImageBuilder newImage() {
        return new ImageBuilder(this);
    }

    public void save(String filePath) {
        try {
            writeBytesToFile(this.getFileBytes(), filePath);
        } catch (IOException e) {
            throw wrapCheckedException(e);
        }
    }

    /**
     * Retrieves a complete snapshot of the entire PDF document.
     * This method returns all pages with their elements, document metadata,
     * and font catalog in a single response, significantly reducing API overhead.
     *
     * @return document snapshot containing all pages and metadata
     */
    public DocumentSnapshot getDocumentSnapshot() {
        return getDocumentSnapshotCached(null);
    }

    /**
     * Retrieves a complete snapshot of the entire PDF document with type filtering.
     * Only elements matching the specified types will be included in the snapshot.
     *
     * @param types comma-separated list of object types to include (e.g., "PARAGRAPH,IMAGE")
     * @return document snapshot containing filtered pages and metadata
     */
    public DocumentSnapshot getDocumentSnapshot(String types) {
        return getDocumentSnapshotCached(types);
    }

    /**
     * Retrieves a snapshot of a single PDF page.
     * This method returns the page metadata and all elements in a single response.
     *
     * @param pageIndex zero-based index of the page to retrieve
     * @return page snapshot containing page reference and all elements
     */
    public PageSnapshot getPageSnapshot(int pageIndex) {
        return getPageSnapshotCached(pageIndex, null);
    }

    /**
     * Retrieves a snapshot of a single PDF page with type filtering.
     * Only elements matching the specified types will be included in the snapshot.
     *
     * @param pageIndex zero-based index of the page to retrieve
     * @param types     comma-separated list of object types to include (e.g., "PARAGRAPH,IMAGE")
     * @return page snapshot containing page reference and filtered elements
     */
    public PageSnapshot getPageSnapshot(int pageIndex, String types) {
        return getPageSnapshotCached(pageIndex, types);
    }

    protected boolean changeFormField(FormFieldRef objectRef, String value) {
        String path = "/pdf/modify/formField";
        MutableHttpRequest<ChangeFormFieldRequest> request = HttpRequest.PUT(path, new ChangeFormFieldRequest(objectRef, value))
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(token)
                .header("X-Session-Id", sessionId);
        Boolean result = retrieve(request, Boolean.class);
        invalidateSnapshotCaches();
        return Boolean.TRUE.equals(result);
    }

    public List<TextParagraphReference> selectParagraphs() {
        TypedDocumentSnapshot<TextTypeObjectRef> snapshot = getTypedDocumentSnapshot(TextTypeObjectRef.class, TYPES_PARAGRAPH);
        List<TextTypeObjectRef> paragraphs = flattenTypedDocument(snapshot, TextTypeObjectRef.class);
        if (paragraphs.isEmpty() || paragraphs.stream().anyMatch(ref -> ref.getText() == null)) {
            paragraphs = findParagraphs(null);
        }
        return toTextObject(paragraphs);
    }

    public List<TextLineReference> selectTextLines() {
        TypedDocumentSnapshot<TextTypeObjectRef> snapshot = getTypedDocumentSnapshot(TextTypeObjectRef.class, TYPES_TEXT_LINE);
        List<TextTypeObjectRef> textLines = flattenTypedDocument(snapshot, TextTypeObjectRef.class);
        if (textLines.isEmpty() || textLines.stream().anyMatch(ref -> ref.getText() == null)) {
            textLines = findTextLines(null);
        }
        return toTextLineObject(textLines);
    }

    public List<PathReference> selectPaths() {
        DocumentSnapshot snapshot = getDocumentSnapshotCached(null);
        return toPathObject(collectObjectsByType(snapshot, Set.of(ObjectType.PATH)));
    }

    public PageClient page(int pageIndex) {
        return new PageClient(this, pageIndex);
    }

    private List<TextParagraphReference> toTextObject(List<TextTypeObjectRef> objectRefs) {
        return objectRefs.stream()
                .map(ref -> ensureTextType(ref, ObjectType.PARAGRAPH))
                .filter(Objects::nonNull)
                .map(ref -> new TextParagraphReference(ref, this))
                .collect(Collectors.toUnmodifiableList());
    }

    private List<TextLineReference> toTextLineObject(List<TextTypeObjectRef> objectRefs) {
        return objectRefs.stream()
                .map(ref -> ensureTextType(ref, ObjectType.TEXT_LINE))
                .filter(Objects::nonNull)
                .map(ref -> new TextLineReference(this, ref))
                .collect(Collectors.toUnmodifiableList());
    }

    private List<PathReference> toPathObject(List<ObjectRef> objectRefs) {
        return objectRefs.stream()
                .map(ref -> new PathReference(ref, this))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<ImageReference> selectImages() {
        DocumentSnapshot snapshot = getDocumentSnapshotCached(null);
        List<ObjectRef> images = collectObjectsByType(snapshot, Set.of(ObjectType.IMAGE));
        if (images.isEmpty()) {
            images = find(ObjectType.IMAGE, null);
        }
        return toImageObject(images);
    }

    private List<ImageReference> toImageObject(List<ObjectRef> objectRefs) {
        return objectRefs.stream()
                .map(ref -> new ImageReference(this, ref))
                .collect(Collectors.toUnmodifiableList());
    }

    private List<FormXObjectReference> toFormXObject(List<ObjectRef> objectRefs) {
        return objectRefs.stream()
                .map(ref -> new FormXObjectReference(this, ref))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<FormXObjectReference> selectForms() {
        DocumentSnapshot snapshot = getDocumentSnapshotCached(null);
        List<ObjectRef> forms = collectObjectsByType(snapshot, Set.of(ObjectType.FORM_X_OBJECT));
        boolean needsFallback = forms.isEmpty() || forms.stream().anyMatch(Objects::isNull)
                || forms.stream().allMatch(ref -> ref.getPosition() == null
                || (ref.getPosition().getX() == null && ref.getPosition().getY() == null));
        if (needsFallback) {
            forms = find(ObjectType.FORM_X_OBJECT, null);
        }
        return toFormXObject(forms);
    }

    public List<FormFieldReference> selectFormFields() {
        List<FormFieldRef> formFields = collectFormFieldRefsFromDocument();
        return toFormFieldObject(formFields);
    }

    private List<FormFieldReference> toFormFieldObject(List<FormFieldRef> formFields) {
        return formFields.stream()
                .map(ref -> new FormFieldReference(this, ref))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<FormFieldReference> selectFormFieldsByName(String elementName) {
        List<FormFieldRef> base = collectFormFieldRefsFromDocument();
        return toFormFieldObject(
                base.stream()
                        .filter(ref -> Objects.equals(ref.getName(), elementName))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public List<ObjectRef> selectElements() {
        List<ObjectRef> fallback = find(null, null);
        DocumentSnapshot snapshot = getDocumentSnapshotCached(null);
        List<ObjectRef> elements = collectAllElements(snapshot);
        if (fallback.size() > elements.size()) {
            return fallback;
        }
        return elements;
    }

    public PageRef addPage() {
        return addPage(null);
    }

    public PageRef addPage(AddPageRequest request) {
        String path = "/pdf/page/add";
        PageRef result = blockingClient.retrieve(
                HttpRequest.POST(path, request)
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                PageRef.class
        );
        invalidateSnapshotCaches();
        return result;
    }

    public PageBuilder page() {
        return new PageBuilder(this);
    }

    public boolean movePage(int fromPageIndex, int toPageIndex) {
        String path = "/pdf/page/move";
        Boolean result = blockingClient.retrieve(
                HttpRequest.PUT(path, Map.of("fromPageIndex", fromPageIndex, "toPageIndex", toPageIndex))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Boolean.class
        );
        invalidateSnapshotCaches();
        return Boolean.TRUE.equals(result);
    }

    /**
     * Gets the HTTP client used by this PDFDancer instance.
     * Useful for testing and creating new instances with the same client.
     *
     * @return the HTTP client
     */
    public PdfDancerHttpClient getHttpClient() {
        return httpClient;
    }

    private static final class TypedPageSnapshot<T extends ObjectRef> {
        private PageRef pageRef;
        private List<T> elements;

        TypedPageSnapshot() {
        }

        TypedPageSnapshot(PageRef pageRef, List<T> elements) {
            this.pageRef = pageRef;
            this.elements = elements;
        }

        public PageRef getPageRef() {
            return pageRef;
        }

        public void setPageRef(PageRef pageRef) {
            this.pageRef = pageRef;
        }

        public List<T> getElements() {
            return elements;
        }

        public void setElements(List<T> elements) {
            this.elements = elements;
        }
    }

    private static final class TypedDocumentSnapshot<T extends ObjectRef> {
        private int pageCount;
        private List<FontRecommendationDto> fonts;
        private List<TypedPageSnapshot<T>> pages;

        TypedDocumentSnapshot() {
        }

        TypedDocumentSnapshot(int pageCount, List<FontRecommendationDto> fonts, List<TypedPageSnapshot<T>> pages) {
            this.pageCount = pageCount;
            this.fonts = fonts;
            this.pages = pages;
        }

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public List<FontRecommendationDto> getFonts() {
            return fonts;
        }

        public void setFonts(List<FontRecommendationDto> fonts) {
            this.fonts = fonts;
        }

        public List<TypedPageSnapshot<T>> getPages() {
            return pages;
        }

        public void setPages(List<TypedPageSnapshot<T>> pages) {
            this.pages = pages;
        }
    }

    /**
     * Represents operations scoped to a single page of a PDF document.
     * Provides type-safe selection methods for text, images, and paths.
     */
    public static class PageClient {
        private final PDFDancer root;
        private final int pageIndex;

        PageClient(PDFDancer root, int pageIndex) {
            this.root = root;
            this.pageIndex = pageIndex;
        }

        /**
         * Selects all paragraph objects on this page.
         */
        public List<TextParagraphReference> selectParagraphs() {
            TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, TYPES_PARAGRAPH);
            List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
            return root.toTextObject(typed);
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public List<TextParagraphReference> selectParagraphsStartingWith(String text) {
            TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, TYPES_PARAGRAPH);
            List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
            return root.toTextObject(
                    typed.stream()
                            .filter(ref -> root.startsWithIgnoreCase(ref.getText(), text))
                            .collect(Collectors.toUnmodifiableList())
            );
        }

        public List<TextParagraphReference> selectParagraphsAt(double x, double y) {
            return selectParagraphsAt(x, y, DEFAULT_EPSILON);
        }

        public List<TextParagraphReference> selectParagraphsAt(double x, double y, double epsilon) {
            TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, TYPES_PARAGRAPH);
            List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
            return root.toTextObject(
                    typed.stream()
                            .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                            .collect(Collectors.toUnmodifiableList())
            );
        }

        public List<TextParagraphReference> selectParagraphsMatching(String pattern) {
            Pattern compiled = Pattern.compile(pattern, Pattern.DOTALL);
            TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, TYPES_PARAGRAPH);
            List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
            return root.toTextObject(
                    typed.stream()
                            .filter(ref -> ref.getText() != null && compiled.matcher(ref.getText()).matches())
                            .collect(Collectors.toUnmodifiableList())
            );
        }

        public List<PathReference> selectPathAt(double x, double y) {
            Position position = new PositionBuilder().onPage(pageIndex).atCoordinates(x, y).build();
            return root.toPathObject(root.find(ObjectType.PATH, position));
        }

        public List<TextLineReference> selectTextLinesStartingWith(String text) {
            TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, TYPES_TEXT_LINE);
            List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
            return root.toTextLineObject(
                    typed.stream()
                            .filter(ref -> root.startsWithIgnoreCase(ref.getText(), text))
                            .collect(Collectors.toUnmodifiableList())
            );
        }

        public List<TextLineReference> selectTextLineAt(double x, double y) {
            return selectTextLineAt(x, y, DEFAULT_EPSILON);
        }

        public List<TextLineReference> selectTextLines() {
            TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, TYPES_TEXT_LINE);
            List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
            return root.toTextLineObject(typed);
        }

        public List<TextLineReference> selectTextLineAt(double x, double y, double epsilon) {
            TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, TYPES_TEXT_LINE);
            List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
            return root.toTextLineObject(
                    typed.stream()
                            .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                            .collect(Collectors.toUnmodifiableList())
            );
        }

        public List<ImageReference> selectImages() {
            PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
            List<ObjectRef> images = root.collectObjectsByType(snapshot, Set.of(ObjectType.IMAGE));
            return root.toImageObject(images);
        }

        public List<ImageReference> selectImagesAt(double x, double y) {
            return selectImagesAt(x, y, DEFAULT_EPSILON);
        }

        public List<ImageReference> selectImagesAt(double x, double y, double epsilon) {
            PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
            List<ObjectRef> images = root.collectObjectsByType(snapshot, Set.of(ObjectType.IMAGE));
            List<ObjectRef> filtered = images.stream()
                    .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                    .collect(Collectors.toUnmodifiableList());
            return root.toImageObject(filtered);
        }

        public List<FormXObjectReference> selectForms() {
            PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
            List<ObjectRef> forms = root.collectObjectsByType(snapshot, Set.of(ObjectType.FORM_X_OBJECT));
            return root.toFormXObject(forms);
        }

        public List<PathReference> selectPaths() {
            PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
            List<ObjectRef> forms = root.collectObjectsByType(snapshot, Set.of(ObjectType.PATH));
            return root.toPathObject(forms);
        }

        public List<FormXObjectReference> selectFormsAt(double x, double y) {
            return selectFormsAt(x, y, DEFAULT_EPSILON);
        }

        public List<FormXObjectReference> selectFormsAt(double x, double y, double epsilon) {
            PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
            List<ObjectRef> forms = root.collectObjectsByType(snapshot, Set.of(ObjectType.FORM_X_OBJECT));
            List<ObjectRef> filtered = forms.stream()
                    .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                    .collect(Collectors.toUnmodifiableList());
            return root.toFormXObject(filtered);
        }

        public List<FormFieldReference> selectFormFields() {
            List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageIndex);
            return root.toFormFieldObject(formFields);
        }

        public List<FormFieldReference> selectFormFieldsAt(double x, double y) {
            return selectFormFieldsAt(x, y, DEFAULT_EPSILON);
        }

        public List<FormFieldReference> selectFormFieldsAt(double x, double y, double epsilon) {
            List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageIndex);
            return root.toFormFieldObject(
                    formFields.stream()
                            .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                            .collect(Collectors.toUnmodifiableList())
            );
        }

        public List<TextParagraphReference> selectTextStartingWith(String text) {
            return this.selectParagraphsStartingWith(text);
        }

        public BezierBuilder newBezier() {
            return new BezierBuilder(root, pageIndex);
        }

        public PathBuilder newPath() {
            return new PathBuilder(root, pageIndex);
        }

        public LineBuilder newLine() {
            return new LineBuilder(root, pageIndex);
        }
    }

    private static final class PageSnapshotKey {
        private final int pageIndex;
        private final String typesKey;

        private PageSnapshotKey(int pageIndex, String typesKey) {
            this.pageIndex = pageIndex;
            this.typesKey = typesKey;
        }

        public int pageIndex() {
            return pageIndex;
        }

        public String typesKey() {
            return typesKey;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PageSnapshotKey) obj;
            return this.pageIndex == that.pageIndex &&
                    Objects.equals(this.typesKey, that.typesKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pageIndex, typesKey);
        }

        @Override
        public String toString() {
            return "PageSnapshotKey[" +
                    "pageIndex=" + pageIndex + ", " +
                    "typesKey=" + typesKey + ']';
        }

    }

    private static final class DocumentSnapshotKey {
        private final Class<? extends ObjectRef> elementClass;
        private final String typesKey;

        private DocumentSnapshotKey(Class<? extends ObjectRef> elementClass, String typesKey) {
            this.elementClass = elementClass;
            this.typesKey = typesKey;
        }

        public Class<? extends ObjectRef> elementClass() {
            return elementClass;
        }

        public String typesKey() {
            return typesKey;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (DocumentSnapshotKey) obj;
            return Objects.equals(this.elementClass, that.elementClass) &&
                    Objects.equals(this.typesKey, that.typesKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(elementClass, typesKey);
        }

        @Override
        public String toString() {
            return "DocumentSnapshotKey[" +
                    "elementClass=" + elementClass + ", " +
                    "typesKey=" + typesKey + ']';
        }

    }

    private static final class TypedPageSnapshotKey {
        private final int pageIndex;
        private final Class<? extends ObjectRef> elementClass;
        private final String typesKey;

        private TypedPageSnapshotKey(int pageIndex, Class<? extends ObjectRef> elementClass, String typesKey) {
            this.pageIndex = pageIndex;
            this.elementClass = elementClass;
            this.typesKey = typesKey;
        }

        public int pageIndex() {
            return pageIndex;
        }

        public Class<? extends ObjectRef> elementClass() {
            return elementClass;
        }

        public String typesKey() {
            return typesKey;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TypedPageSnapshotKey) obj;
            return this.pageIndex == that.pageIndex &&
                    Objects.equals(this.elementClass, that.elementClass) &&
                    Objects.equals(this.typesKey, that.typesKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pageIndex, elementClass, typesKey);
        }

        @Override
        public String toString() {
            return "TypedPageSnapshotKey[" +
                    "pageIndex=" + pageIndex + ", " +
                    "elementClass=" + elementClass + ", " +
                    "typesKey=" + typesKey + ']';
        }

    }
}
