package com.pdfdancer.client.rest.session;

import com.pdfdancer.client.http.HttpRequest;
import com.pdfdancer.client.http.MediaType;
import com.pdfdancer.client.http.MutableHttpRequest;
import com.pdfdancer.client.rest.AnonTokenResponse;
import com.pdfdancer.client.rest.EnvironmentInfo;
import com.pdfdancer.client.rest.PdfDancerHttpClient;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageSize;
import com.pdfdancer.common.request.CreateBlankPdfRequest;


/**
 * Session lifecycle utilities: issuing anonymous token and creating sessions.
 */
public final class SessionService {
    private SessionService() {}

    public static String obtainAnonymousToken(PdfDancerHttpClient client) {
        String fingerprint = EnvironmentInfo.buildFingerprint();
        MutableHttpRequest<?> request = HttpRequest.POST("/keys/anon", null)
                .header("X-Fingerprint", fingerprint);
        AnonTokenResponse token = client.toBlocking().retrieve(request, AnonTokenResponse.class);
        return token.token();
    }

    public static String uploadPdfForSession(String token, byte[] pdf, PdfDancerHttpClient client) {
        com.pdfdancer.client.http.MultipartBody body = com.pdfdancer.client.http.MultipartBody.builder()
                .addPart("pdf", "test.pdf", MediaType.APPLICATION_PDF_TYPE, pdf)
                .build();
        return client.toBlocking().retrieve(
                HttpRequest.POST("/session/create", body)
                        .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
                        .bearerAuth(token),
                String.class
        );
    }

    public static String createBlankPdfSession(String token, PageSize pageSize,
                                               Orientation orientation,
                                               int initialPageCount,
                                               PdfDancerHttpClient client) {
        return client.toBlocking().retrieve(
                HttpRequest.POST("/session/new",
                                new CreateBlankPdfRequest(pageSize, orientation, initialPageCount))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token),
                String.class
        );
    }
}
