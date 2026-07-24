package com.pdfdancer.client.rest;

/** Base runtime exception for all PDFDancer client failures. */
public class PdfDancerException extends RuntimeException {
    public PdfDancerException(String message) { super(message); }
    public PdfDancerException(String message, Throwable cause) { super(message, cause); }
    public PdfDancerException(Throwable cause) { super(cause); }
}
