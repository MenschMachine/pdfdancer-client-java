package com.pdfdancer.client.rest;

public class SessionNotFoundException extends PdfDancerClientException {

    public SessionNotFoundException(String message) {
        super(404, message);
    }
}
