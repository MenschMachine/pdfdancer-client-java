package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ReflowPreset;
import com.pdfdancer.common.request.TemplateReplaceRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateIntegrationOATest extends BaseTest {

    @Test
    public void testReplaceMultipleTemplatesDefaultReflowPreset() {
        // Given: Showcase.pdf with multiple placeholders
        PDFDancer client = createClient("ObviouslyAwesome.pdf");

        // When: Replacing multiple placeholders
        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .replace("Complete", "Entire")
                        .replace("Awesome", "Amazing")
                        .build()
        );

        // Then: All replacements worked
        assertTrue(success, "Template replacement should succeed");
        new PDFAssertions(client)
                .assertTextlineDoesNotExist("Complete", 1)
                .assertTextlineDoesNotExist("Awesome", 1)
                .assertTextlineExists("Entire", 1)
                .assertTextlineExists("Amazing", 1);
    }

    @Test
    public void testReplaceMultipleTemplatesNoReflowPreset() {
        // Given: Showcase.pdf with multiple placeholders
        PDFDancer client = createClient("ObviouslyAwesome.pdf");

        // When: Replacing multiple placeholders
        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.NONE)
                        .replace("Complete", "Entire")
                        .replace("Awesome", "Amazing")
                        .build()
        );

        // Then: All replacements worked
        assertTrue(success, "Template replacement should succeed");
        new PDFAssertions(client)
                .assertTextlineDoesNotExist("Complete", 1)
                .assertTextlineDoesNotExist("Awesome", 1)
                .assertTextlineExists("Entire", 1)
                .assertTextlineExists("Amazing", 1);
    }
}
