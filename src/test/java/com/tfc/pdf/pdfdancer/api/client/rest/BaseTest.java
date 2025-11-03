package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.client.http.HttpRequest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Base class for REST client integration tests. Configures a {@link PdfDancerHttpClient}
 * pointing at the target API instance and skips tests automatically if the server
 * cannot be reached.
 */
public abstract class BaseTest {

    private static final String DEFAULT_BASE_URI = "http://localhost:8080/";
    private static final String DEFAULT_TOKEN = "42";

    protected static PdfDancerHttpClient httpClient;
    protected static URI baseUri;

    private static boolean availabilityChecked;
    private static boolean serverAvailable;

    @BeforeAll
    static void initClient() {
        String baseUriValue = System.getProperty("pdfdancer.baseUri",
                System.getenv().getOrDefault("PDFDANCER_BASE_URI", DEFAULT_BASE_URI));
        baseUri = URI.create(baseUriValue);

        HttpClient delegate = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        httpClient = PdfDancerHttpClient.create(delegate, baseUri);
    }

    @BeforeEach
    void ensureServerAvailable() {
        Assumptions.assumeTrue(isServerAvailable(),
                () -> "PDFDancer API not available at " + baseUri
                        + ". Set PDFDANCER_TESTS_ENABLED=true (and PDFDANCER_BASE_URI / PDFDANCER_TOKEN) to run integration tests.");
    }

    private static synchronized boolean isServerAvailable() {
        if (!availabilityChecked) {
            availabilityChecked = true;
            serverAvailable = probeServer();
        }
        return serverAvailable;
    }

    private static boolean probeServer() {
        try {
            httpClient.toBlocking().retrieve(HttpRequest.GET("/ping"), String.class);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    protected String getValidToken() {
        return System.getProperty("pdfdancer.token",
                System.getenv().getOrDefault("PDFDANCER_TOKEN", DEFAULT_TOKEN));
    }

    protected PDFDancer createClient() {
        return TestPDFDancer.create(getValidToken(), httpClient, getPdfFile());
    }

    protected PDFDancer createClient(String pdfFixture) {
        return TestPDFDancer.create(getValidToken(), httpClient, pdfFixture);
    }

    protected PDFDancer newPdf() {
        return TestPDFDancer.newPdf(getValidToken(), httpClient);
    }

    protected String getPdfFile() {
        return "ObviouslyAwesome.pdf";
    }

    protected PDFDancer createAnonClient() {
        return TestPDFDancer.createAnon(httpClient, getPdfFile());
    }
}
