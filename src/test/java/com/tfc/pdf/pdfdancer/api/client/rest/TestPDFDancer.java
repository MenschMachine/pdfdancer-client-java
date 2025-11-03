package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.common.model.Orientation;
import com.tfc.pdf.pdfdancer.api.common.model.PageSize;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.tfc.pdf.pdfdancer.api.common.util.ExceptionUtils.wrapCheckedException;

public class TestPDFDancer {

    public static PDFDancer create(String validToken, PdfDancerHttpClient httpClient, String pdfFile) {
        return PDFDancer.createSession(validToken, getTestPdf(pdfFile), httpClient);
    }

    public static PDFDancer createAnon(PdfDancerHttpClient httpClient, String pdfFile) {
        return PDFDancer.createAnonSession(getTestPdf(pdfFile), httpClient);
    }

    public static byte[] getTestPdf(String pdfFile) {
        try (InputStream resourceAsStream = PDFDancer.class.getClassLoader().getResourceAsStream("fixtures/" + pdfFile)) {
            if (Objects.isNull(resourceAsStream)) {
                throw new FileNotFoundException("Resource not found: " + pdfFile);
            }
            try {
                return Objects.requireNonNull(resourceAsStream).readAllBytes();
            } catch (IOException e) {
                throw wrapCheckedException(e);
            }
        } catch (IOException e) {
            throw wrapCheckedException(e);
        }
    }

    public static PDFDancer newPdf(String validToken, PdfDancerHttpClient httpClient) {
        return PDFDancer.createNew(validToken, PageSize.A4, Orientation.PORTRAIT, 1, httpClient);
    }

}
