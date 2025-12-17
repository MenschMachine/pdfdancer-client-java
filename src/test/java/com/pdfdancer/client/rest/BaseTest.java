package com.pdfdancer.client.rest;

import org.junit.jupiter.api.BeforeAll;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
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
    protected static URI baseUrl;

    @BeforeAll
    static void initClient() {
        String baseUrlValue = System.getProperty("pdfdancer.baseUrl",
                System.getenv().getOrDefault("PDFDANCER_BASE_URL", DEFAULT_BASE_URI));
        baseUrl = URI.create(baseUrlValue);

        HttpClient delegate = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .build();
        httpClient = PdfDancerHttpClient.create(delegate, baseUrl);
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

    protected void saveTo(PDFDancer client, String fileName) {
        Path out = Path.of(System.getProperty("java.io.tmpdir"), fileName);
        client.save(out.toString());
        System.out.println("Saved file: " + out);
    }
}
