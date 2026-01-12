package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.ReflowPreset;
import com.pdfdancer.common.request.TemplateReplacement;
import com.pdfdancer.common.request.TemplateReplaceRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for template placeholder replacements.
 * <p>Example usage:
 * <pre>{@code
 * // Simple replacement
 * client.replace("{{name}}", "John").apply();
 *
 * // With formatting
 * client.replace("{{name}}", "John")
 *     .withFont("Helvetica-Bold", 14)
 *     .withColor(255, 0, 0)
 *     .apply();
 *
 * // Multiple replacements
 * client.replace("{{name}}", "John")
 *     .withFont("Helvetica-Bold", 14)
 *     .replace("{{title}}", "Manager")
 *     .apply();
 *
 * // Page-specific
 * client.page(1).replace("{{header}}", "Title").apply();
 * }</pre>
 */
public class ReplaceBuilder {
    private final PDFDancer client;
    private final List<Entry> entries = new ArrayList<>();
    private Integer pageIndex;
    private ReflowPreset reflowPreset;

    // Current entry being built
    private String currentPlaceholder;
    private String currentText;
    private Font currentFont;
    private Color currentColor;

    ReplaceBuilder(PDFDancer client, String placeholder, String text) {
        this.client = client;
        this.currentPlaceholder = placeholder;
        this.currentText = text;
    }

    ReplaceBuilder(PDFDancer client, int pageNumber, String placeholder, String text) {
        this(client, placeholder, text);
        this.pageIndex = pageNumber - 1; // Convert 1-based page number to 0-based index
    }

    /**
     * Sets the font for the current replacement.
     */
    public ReplaceBuilder withFont(String name, double size) {
        this.currentFont = new Font(name, size);
        return this;
    }

    /**
     * Sets the font for the current replacement.
     */
    public ReplaceBuilder withFont(Font font) {
        this.currentFont = font;
        return this;
    }

    /**
     * Sets the color for the current replacement.
     */
    public ReplaceBuilder withColor(int r, int g, int b) {
        this.currentColor = new Color(r, g, b);
        return this;
    }

    /**
     * Sets the color for the current replacement.
     */
    public ReplaceBuilder withColor(Color color) {
        this.currentColor = color;
        return this;
    }

    /**
     * Sets the reflow preset for text fitting behavior.
     */
    public ReplaceBuilder withReflow(ReflowPreset preset) {
        this.reflowPreset = preset;
        return this;
    }

    /**
     * Restricts replacements to a specific page (1-based page number).
     */
    public ReplaceBuilder onPage(int pageNumber) {
        this.pageIndex = pageNumber - 1; // Convert 1-based to 0-based
        return this;
    }

    /**
     * Adds another replacement to the batch.
     */
    public ReplaceBuilder replace(String placeholder, String text) {
        commitCurrent();
        this.currentPlaceholder = placeholder;
        this.currentText = text;
        this.currentFont = null;
        this.currentColor = null;
        return this;
    }

    /**
     * Applies all replacements to the PDF.
     *
     * @return true if all replacements were successful
     */
    public boolean apply() {
        commitCurrent();

        List<TemplateReplacement> replacements = new ArrayList<>();
        for (Entry e : entries) {
            replacements.add(new TemplateReplacement(e.placeholder, e.text, e.font, e.color));
        }

        TemplateReplaceRequest request = new TemplateReplaceRequest(replacements, pageIndex, reflowPreset);
        return client.applyReplacements(request);
    }

    private void commitCurrent() {
        if (currentPlaceholder != null) {
            entries.add(new Entry(currentPlaceholder, currentText, currentFont, currentColor));
            currentPlaceholder = null; // Prevent double-commit
        }
    }

    private static class Entry {
        final String placeholder;
        final String text;
        final Font font;
        final Color color;

        Entry(String placeholder, String text, Font font, Color color) {
            this.placeholder = placeholder;
            this.text = text;
            this.font = font;
            this.color = color;
        }
    }
}
