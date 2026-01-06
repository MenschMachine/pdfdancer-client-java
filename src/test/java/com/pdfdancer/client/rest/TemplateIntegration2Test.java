package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ReflowPreset;
import com.pdfdancer.common.request.TemplateReplaceRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateIntegration2Test extends BaseTest {

    @Test
    public void testReplaceMultipleTemplatesDefaultReflowPreset() throws IOException {
        // Given: Showcase.pdf with multiple placeholders
        PDFDancer client = createClient("Showcase.pdf");

        // When: Replacing multiple placeholders
        boolean success = client.replaceTemplates(
                TemplateReplaceRequest.builder()
                        .replace("PDFDancer", "TestApp")
                        .replace("Engine", "System")
                        .build()
        );

        // Then: All replacements worked
        assertTrue(success, "Template replacement should succeed");
        new PDFAssertions(client)
                .assertTextlineDoesNotExist("PDFDancer", 1)
                .assertTextlineDoesNotExist("Engine", 1)
                .assertTextlineExists("TestApp", 1)
                .assertTextlineExists("System", 1);
    }

    @Test
    public void testReplaceMultipleTemplatesNoReflowPreset() throws IOException {
        // Given: Showcase.pdf with multiple placeholders
        PDFDancer client = createClient("Showcase.pdf");

        // When: Replacing multiple placeholders
        boolean success = client.replaceTemplates(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.NONE)
                        .replace("PDFDancer", "TestApp")
                        .replace("Engine", "System")
                        .build()
        );

        // Then: All replacements worked
        assertTrue(success, "Template replacement should succeed");
        new PDFAssertions(client)
                .assertTextlineDoesNotExist("PDFDancer", 1)
                .assertTextlineDoesNotExist("Engine", 1)
                .assertTextlineExists("TestApp", 1)
                .assertTextlineExists("System", 1);
    }
}
