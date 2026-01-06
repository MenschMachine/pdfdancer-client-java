package com.pdfdancer.client.rest;

import com.pdfdancer.client.http.*;
import com.pdfdancer.client.rest.mutation.ModificationService;
import com.pdfdancer.client.rest.selection.SelectionService;
import com.pdfdancer.client.rest.session.SessionService;
import com.pdfdancer.common.model.*;
import com.pdfdancer.common.model.text.Paragraph;
import com.pdfdancer.common.request.*;
import com.pdfdancer.common.response.DocumentSnapshot;
import com.pdfdancer.common.response.PageSnapshot;
import com.pdfdancer.common.response.RedactResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pdfdancer.common.model.ObjectType.FORM_FIELD;
import static com.pdfdancer.common.util.ExceptionUtils.wrapCheckedException;
import static com.pdfdancer.common.util.FileUtils.writeBytesToFile;

/**
 * REST API client for interacting with the PDFDancer PDF manipulation service.
 * This client provides a convenient Java interface for performing PDF operations
 * including session management, object searching, manipulation, and retrieval.
 * Handles authentication, session lifecycle, and HTTP communication transparently.
 */
public class PDFDancer {
    public static final double DEFAULT_EPSILON = 0.01;
    private static final URI DEFAULT_BASE_URI = URI.create("https://api.pdfdancer.com");
    public static final String TYPES_PARAGRAPH = "PARAGRAPH";
    public static final String TYPES_TEXT_LINE = "TEXT_LINE";
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
    private final SnapshotCache snapshotCache;
    private final SelectionService selection;
    private final ModificationService modification;

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
        this.snapshotCache = new SnapshotCache(token, sessionId, this.blockingClient);
        this.selection = new SelectionService();
        this.modification = new ModificationService(token, sessionId, this.blockingClient);
    }

    /**
     * Creates a new PDFDancer client by uploading an existing PDF file.
     * Uses the default HTTP client configured for https://api.pdfdancer.com.
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
     * Uses the default HTTP client configured for https://api.pdfdancer.com.
     * Authentication:
     * - If PDFDANCER_API_TOKEN or PDFDANCER_TOKEN is set, uses it
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
     * Uses the default HTTP client configured for https://api.pdfdancer.com.
     * Authentication:
     * - If PDFDANCER_API_TOKEN or PDFDANCER_TOKEN is set, uses it
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
        return createSession(token, bytesPDF, PdfDancerHttpClient.create(httpClient, getBaseUrl()));
    }

    private static URI getBaseUrl() {
        String baseUrlValue = System.getProperty("pdfdancer.baseUrl",
                System.getenv().getOrDefault("PDFDANCER_BASE_URL", String.valueOf(DEFAULT_BASE_URI)));
        try {
            return new URI(baseUrlValue);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static PDFDancer createSession(String token, byte[] bytesPDF, HttpClient httpClient, URI baseUrl) {
        return createSession(token, bytesPDF, PdfDancerHttpClient.create(httpClient, baseUrl));
    }

    /**
     * Creates a new PDFDancer client with a blank PDF.
     * Uses default page size (A4), orientation (PORTRAIT), and page count (1).
     * Uses the default HTTP client configured for https://api.pdfdancer.com.
     * Authentication:
     * - If PDFDANCER_API_TOKEN or PDFDANCER_TOKEN is set, uses it
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
        String fingerprint = EnvironmentInfo.buildFingerprint();
        MutableHttpRequest<?> request = HttpRequest.POST("/keys/anon", null)
                .header("X-Fingerprint", fingerprint);
        AnonTokenResponse token = client.toBlocking().retrieve(request, AnonTokenResponse.class);
        return createSession(token.token(), testPdf, client);
    }

    @SuppressWarnings("unused")
    static PDFDancer createAnonSession(byte[] testPdf, HttpClient httpClient) {
        return createAnonSession(testPdf, PdfDancerHttpClient.create(httpClient, getBaseUrl()));
    }

    @SuppressWarnings("unused")
    static PDFDancer createAnonSession(byte[] testPdf, HttpClient httpClient, URI baseUrl) {
        return createAnonSession(testPdf, PdfDancerHttpClient.create(httpClient, baseUrl));
    }

    /**
     * Creates a new PDFDancer client with a blank PDF using custom parameters.
     * Uses the default HTTP client configured for https://api.pdfdancer.com.
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
     * Uses the default HTTP client configured for https://api.pdfdancer.com.
     * Authentication:
     * - If PDFDANCER_API_TOKEN or PDFDANCER_TOKEN is set, uses it
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
        return createNew(token, pageSize, orientation, initialPageCount, PdfDancerHttpClient.create(httpClient, getBaseUrl()));
    }

    @SuppressWarnings("unused")
    public static PDFDancer createNew(String token, PageSize pageSize,
                                      Orientation orientation,
                                      int initialPageCount, HttpClient httpClient, URI baseUrl) {
        return createNew(token, pageSize, orientation, initialPageCount, PdfDancerHttpClient.create(httpClient, baseUrl));
    }

    /**
     * Creates a default HTTP client configured for the hosted PDFDancer API.
     * This method provides a preconfigured client for connecting to https://api.pdfdancer.com
     * when no custom {@link java.net.http.HttpClient} is supplied.
     *
     * @return HTTP client configured for https://api.pdfdancer.com
     * @throws RuntimeException if URL creation fails
     */
    private static PdfDancerHttpClient getDefaultClient() {
        return PdfDancerHttpClient.createDefault(getBaseUrl());
    }

    private static String envTokenOrNull() {
        return EnvironmentInfo.envTokenOrNull();
    }

    private static String obtainAnonymousToken(PdfDancerHttpClient client) {
        return SessionService.obtainAnonymousToken(client);
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
        return SessionService.uploadPdfForSession(token, pdf, client);
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
    private static String createBlankPdfSession(String token, PageSize pageSize,
                                                Orientation orientation,
                                                int initialPageCount, PdfDancerHttpClient client) {
        return client.toBlocking().retrieve(
                HttpRequest.POST("/session/new",
                                new CreateBlankPdfRequest(pageSize, orientation, initialPageCount))
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
        Boolean result = modification.deletePage(pageRef);
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
    List<ObjectRef> find(ObjectType type, Position position) {
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
        Boolean result = modification.delete(objectRef);
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
     * @param pageNumber the page number to retrieve (1-based indexing, page 1 is first page)
     * @return object reference for the specified page, or null if not found
     * @throws IllegalArgumentException if pageNumber is less than 1
     */
    public ObjectRef getPage(int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be >= 1 (1-based indexing)");
        }
        String path = "/pdf/page/find?pageNumber=" + pageNumber;
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
        Boolean result = modification.move(objectRef, position);
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
        boolean result = modification.addImage(image, position);
        invalidateSnapshotCaches();
        return result;
    }

    protected Boolean addObject(PDFObject object) {
        Boolean result = modification.addObject(object);
        invalidateSnapshotCaches();
        return result;
    }

    private void invalidateSnapshotCaches() {
        snapshotCache.invalidate();
    }


    DocumentSnapshot getDocumentSnapshotCached(String types) {
        return snapshotCache.getDocumentSnapshotCached(types);
    }

    PageSnapshot getPageSnapshotCached(int pageNumber, String types) {
        return snapshotCache.getPageSnapshotCached(pageNumber, types);
    }


    public <T extends ObjectRef> TypedDocumentSnapshot<T> getTypedDocumentSnapshot(Class<T> elementClass, String types) {
        return snapshotCache.getTypedDocumentSnapshot(elementClass, types);
    }

    public <T extends ObjectRef> TypedPageSnapshot<T> getTypedPageSnapshot(int pageNumber,
                                                                           Class<T> elementClass,
                                                                           String types) {
        return snapshotCache.getTypedPageSnapshot(pageNumber, elementClass, types);
    }

    <T extends ObjectRef> List<T> getTypedElements(TypedPageSnapshot<T> page, Class<T> elementClass) {
        return selection.getTypedElements(page, elementClass);
    }

    private <T extends ObjectRef> List<T> flattenTypedDocument(TypedDocumentSnapshot<T> snapshot, Class<T> elementClass) {
        return selection.flattenTypedDocument(snapshot, elementClass);
    }

    private List<FormFieldRef> collectFormFieldRefsFromDocument() {
        return selection.collectFormFieldRefsFromDocument(this);
    }

    List<FormFieldRef> collectFormFieldRefsFromPage(int pageNumber) {
        return selection.collectFormFieldRefsFromPage(this, pageNumber);
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

    List<ObjectRef> collectAllElements(DocumentSnapshot snapshot) {
        return selection.collectAllElements(snapshot);
    }

    List<ObjectRef> collectObjectsByType(DocumentSnapshot snapshot, Set<ObjectType> types) {
        return selection.collectObjectsByType(snapshot, types);
    }

    List<ObjectRef> collectObjectsByType(PageSnapshot snapshot, Set<ObjectType> types) {
        return selection.collectObjectsByType(snapshot, types);
    }

    boolean containsPoint(ObjectRef ref, double x, double y, double epsilon) {
        return selection.containsPoint(ref, x, y, epsilon);
    }

    boolean startsWithIgnoreCase(String value, String prefix) {
        return selection.startsWithIgnoreCase(value, prefix);
    }

    protected boolean modifyParagraph(ObjectRef ref, Paragraph newParagraph) {
        boolean success = modification.modifyParagraph(ref, newParagraph);
        invalidateSnapshotCaches();
        return success;
    }

    protected boolean modifyTextLine(ObjectRef ref, String newTextLine) {
        boolean success = modification.modifyTextLine(ref, newTextLine);
        invalidateSnapshotCaches();
        return success;
    }

    protected boolean modifyTextLine(ObjectRef ref, com.pdfdancer.common.model.text.TextLine newTextLine) {
        boolean success = modification.modifyTextLine(ref, newTextLine);
        invalidateSnapshotCaches();
        return success;
    }

    protected boolean modifyParagraph(ObjectRef ref, String newText) {
        boolean success = modification.modifyParagraph(ref, newText);
        invalidateSnapshotCaches();
        return success;
    }

    protected boolean addParagaph(Paragraph newParagraph) {
        if (newParagraph.getPosition() == null) {
            throw new IllegalArgumentException("Paragraph getPosition is null");
        }
        if (newParagraph.getPosition().getPageNumber() == null) {
            throw new IllegalArgumentException("Paragraph getPosition page number is null");
        }
        if (newParagraph.getPosition().getPageNumber() < 0) {
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
     * @param pageNumber the page number to retrieve (1-based indexing, page 1 is first page)
     * @return page snapshot containing page reference and all elements
     * @throws IllegalArgumentException if pageNumber is less than 1
     */
    public PageSnapshot getPageSnapshot(int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be >= 1 (1-based indexing)");
        }
        return getPageSnapshotCached(pageNumber, null);
    }

    /**
     * Retrieves a snapshot of a single PDF page with type filtering.
     * Only elements matching the specified types will be included in the snapshot.
     *
     * @param pageNumber the page number to retrieve (1-based indexing, page 1 is first page)
     * @param types      comma-separated list of object types to include (e.g., "PARAGRAPH,IMAGE")
     * @return page snapshot containing page reference and filtered elements
     * @throws IllegalArgumentException if pageNumber is less than 1
     */
    public PageSnapshot getPageSnapshot(int pageNumber, String types) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be >= 1 (1-based indexing)");
        }
        return getPageSnapshotCached(pageNumber, types);
    }

    protected boolean changeFormField(FormFieldRef objectRef, String value) {
        Boolean result = modification.changeFormField(objectRef, value);
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

    /**
     * Creates a client for working with a specific page.
     *
     * @param pageNumber the page number (1-based indexing, page 1 is first page)
     * @return a PageClient for the specified page
     * @throws IllegalArgumentException if pageNumber is less than 1
     */
    public PageClient page(int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be >= 1 (1-based indexing)");
        }
        return new PageClient(this, pageNumber);
    }

    List<TextParagraphReference> toTextObject(List<TextTypeObjectRef> objectRefs) {
        return objectRefs.stream()
                .map(ref -> ensureTextType(ref, ObjectType.PARAGRAPH))
                .filter(Objects::nonNull)
                .map(ref -> new TextParagraphReference(ref, this))
                .collect(Collectors.toUnmodifiableList());
    }

    List<TextLineReference> toTextLineObject(List<TextTypeObjectRef> objectRefs) {
        return objectRefs.stream()
                .map(ref -> ensureTextType(ref, ObjectType.TEXT_LINE))
                .filter(Objects::nonNull)
                .map(ref -> new TextLineReference(this, ref))
                .collect(Collectors.toUnmodifiableList());
    }

    List<PathReference> toPathObject(List<ObjectRef> objectRefs) {
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

    List<ImageReference> toImageObject(List<ObjectRef> objectRefs) {
        return objectRefs.stream()
                .map(ref -> new ImageReference(this, ref))
                .collect(Collectors.toUnmodifiableList());
    }

    List<FormXObjectReference> toFormXObject(List<ObjectRef> objectRefs) {
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

    List<FormFieldReference> toFormFieldObject(List<FormFieldRef> formFields) {
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

    /**
     * Selects a single form field with the specified name.
     *
     * @param elementName the name of the form field to find
     * @return Optional containing the first form field with the given name, or empty if none found
     */
    public java.util.Optional<FormFieldReference> selectFormFieldByName(String elementName) {
        List<FormFieldReference> formFields = selectFormFieldsByName(elementName);
        return formFields.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(formFields.get(0));
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
        PageRef result = modification.addPage(request);
        invalidateSnapshotCaches();
        return result;
    }

    /**
     * Creates a new page builder for fluent page creation.
     *
     * @return a new PageBuilder instance
     */
    public PageBuilder newPage() {
        return new PageBuilder(this);
    }

    /**
     * @deprecated Use {@link #newPage()} instead. This method will be removed in a future release.
     */
    @Deprecated
    public PageBuilder page() {
        return newPage();
    }

    /**
     * Moves a page from one position to another within the PDF document.
     *
     * @param fromPage the source page number (1-based indexing, page 1 is first page)
     * @param toPage   the target page number (1-based indexing)
     * @return true if the page was successfully moved
     * @throws IllegalArgumentException if fromPage or toPage is less than 1
     */
    public boolean movePage(int fromPage, int toPage) {
        if (fromPage < 1) {
            throw new IllegalArgumentException("fromPage must be >= 1 (1-based indexing)");
        }
        if (toPage < 1) {
            throw new IllegalArgumentException("toPage must be >= 1 (1-based indexing)");
        }
        Boolean result = modification.movePage(fromPage, toPage);
        invalidateSnapshotCaches();
        return Boolean.TRUE.equals(result);
    }

    /**
     * Redacts multiple objects from the PDF document using default replacement text.
     *
     * @param objects the objects to redact
     * @return response containing the count of redacted items and any warnings
     * @throws IllegalArgumentException if objects is null or empty
     */
    public RedactResponse redact(List<? extends BaseReference> objects) {
        return redact(objects, "[REDACTED]", Color.BLACK);
    }

    /**
     * Redacts multiple objects from the PDF document with custom replacement text.
     *
     * @param objects     the objects to redact
     * @param replacement the replacement text for text content
     * @return response containing the count of redacted items and any warnings
     * @throws IllegalArgumentException if objects is null or empty
     */
    public RedactResponse redact(List<? extends BaseReference> objects, String replacement) {
        return redact(objects, replacement, Color.BLACK);
    }

    /**
     * Redacts multiple objects from the PDF document with custom placeholder color.
     *
     * @param objects          the objects to redact
     * @param placeholderColor the color for image/path placeholders
     * @return response containing the count of redacted items and any warnings
     * @throws IllegalArgumentException if objects is null or empty
     */
    public RedactResponse redact(List<? extends BaseReference> objects, Color placeholderColor) {
        return redact(objects, "[REDACTED]", placeholderColor);
    }

    /**
     * Redacts multiple objects from the PDF document.
     * Text content is replaced with the replacement string, while images and paths
     * are replaced with solid color placeholder rectangles.
     *
     * @param objects          the objects to redact
     * @param replacement      the replacement text for text content
     * @param placeholderColor the color for image/path placeholders
     * @return response containing the count of redacted items and any warnings
     * @throws IllegalArgumentException if objects is null or empty
     */
    public RedactResponse redact(List<? extends BaseReference> objects, String replacement, Color placeholderColor) {
        if (objects == null || objects.isEmpty()) {
            throw new IllegalArgumentException("At least one object is required");
        }
        RedactRequest.Builder builder = RedactRequest.builder()
                .defaultReplacement(replacement)
                .placeholderColor(placeholderColor);
        for (BaseReference obj : objects) {
            builder.addTargetById(obj.getInternalId());
        }
        return redact(builder.build());
    }

    /**
     * Redacts content from the PDF document based on the provided request.
     * Text content is replaced with a replacement string, while images and paths
     * are replaced with solid color placeholder rectangles.
     *
     * @param request the redaction request containing targets and options
     * @return response containing the count of redacted items and any warnings
     */
    RedactResponse redact(RedactRequest request) {
        RedactResponse result = modification.redact(request);
        invalidateSnapshotCaches();
        return result;
    }

    /**
     * Transforms an image in the PDF document.
     *
     * @param request the transformation request
     * @return true if the transformation was successful
     */
    protected boolean transformImage(ImageTransformRequest request) {
        boolean result = modification.transformImage(request);
        invalidateSnapshotCaches();
        return result;
    }

    /**
     * Replaces template placeholders in the PDF document.
     * Finds exact text matches for placeholders and replaces them with specified content.
     * All placeholders must be found or the operation fails atomically.
     *
     * @param request the template replacement request
     * @return true if all replacements were successful
     */
    public boolean replaceTemplates(TemplateReplaceRequest request) {
        boolean result = modification.replaceTemplates(request);
        invalidateSnapshotCaches();
        return result;
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


    /**
     * Represents operations scoped to a single page of a PDF document.
     * Provides type-safe selection methods for text, images, and paths.
     */
    public static class PageClient extends PageClientImpl {
        PageClient(PDFDancer root, int pageNumber) {
            super(root, pageNumber);
        }
    }

}
