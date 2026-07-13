package com.pdfdancer.client.rest.text;

import com.pdfdancer.client.http.HttpRequest;
import com.pdfdancer.client.http.MediaType;
import com.pdfdancer.client.rest.PdfDancerHttpClient;
import com.pdfdancer.common.request.TextDeleteRequest;
import com.pdfdancer.common.request.TextInsertRequest;
import com.pdfdancer.common.request.TextReplaceRequest;
import com.pdfdancer.common.request.TextStyleRequest;
import com.pdfdancer.common.response.TextEditResponse;

import java.util.Objects;

public final class TextEditingService {
    private final String token;
    private final String sessionId;
    private final PdfDancerHttpClient.Blocking blocking;

    public TextEditingService(String token, String sessionId, PdfDancerHttpClient.Blocking blocking) {
        this.token = token;
        this.sessionId = sessionId;
        this.blocking = blocking;
    }

    public TextEditResponse replace(TextReplaceRequest request) {
        Objects.requireNonNull(request, "request");
        return blocking.retrieve(
                HttpRequest.POST("/pdf/text/replace", request.validated())
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                TextEditResponse.class
        );
    }

    public TextEditResponse delete(TextDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return blocking.retrieve(
                HttpRequest.POST("/pdf/text/delete", request.validated())
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                TextEditResponse.class
        );
    }

    public TextEditResponse insert(TextInsertRequest request) {
        Objects.requireNonNull(request, "request");
        return blocking.retrieve(
                HttpRequest.POST("/pdf/text/insert", request.validated())
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                TextEditResponse.class
        );
    }

    public TextEditResponse style(TextStyleRequest request) {
        Objects.requireNonNull(request, "request");
        return blocking.retrieve(
                HttpRequest.POST("/pdf/text/style", request.validated())
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                TextEditResponse.class
        );
    }
}
