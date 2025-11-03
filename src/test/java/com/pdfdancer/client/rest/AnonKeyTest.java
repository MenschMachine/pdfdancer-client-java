package com.pdfdancer.client.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnonKeyTest extends BaseTest {

    @Test
    void testCreateSession() {
        PDFDancer pdf = createAnonClient();
        String token = pdf.getToken();
        assertNotNull(token);
    }

}
