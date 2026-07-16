package com.pdfdancer.client.rest;

/** Invalid client input detected before a request is sent. */
public class ValidationException extends PdfDancerException {
    public ValidationException(String message) { super(message); }
}
