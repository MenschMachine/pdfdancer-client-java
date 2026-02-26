package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.Image;

import java.util.Objects;

/**
 * Represents a single template placeholder replacement.
 * Specifies the placeholder text to find and the replacement content,
 * with optional font and color overrides. The replacement can be either
 * text-based or image-based (but not both).
 */
public final class TemplateReplacement {
    @JsonProperty("placeholder")
    private final String placeholder;
    @JsonProperty("text")
    private final String text;
    @JsonProperty("font")
    private final Font font;
    @JsonProperty("color")
    private final Color color;
    @JsonProperty("image")
    private final Image image;

    @JsonCreator
    public TemplateReplacement(
            @JsonProperty("placeholder") String placeholder,
            @JsonProperty("text") String text,
            @JsonProperty("font") Font font,
            @JsonProperty("color") Color color,
            @JsonProperty("image") Image image
    ) {
        this.placeholder = Objects.requireNonNull(placeholder, "placeholder is required");
        if (text == null && image == null) {
            throw new IllegalArgumentException("either text or image must be provided");
        }
        this.text = text;
        this.font = font;
        this.color = color;
        this.image = image;
    }

    public TemplateReplacement(String placeholder, String text, Font font, Color color) {
        this(placeholder, text, font, color, null);
    }

    /**
     * Creates a simple replacement without formatting overrides.
     */
    public static TemplateReplacement of(String placeholder, String text) {
        return new TemplateReplacement(placeholder, text, null, null);
    }

    /**
     * Creates a replacement with an image instead of text.
     */
    public static TemplateReplacement withImage(String placeholder, Image image) {
        return new TemplateReplacement(placeholder, null, null, null, image);
    }

    /**
     * Creates a replacement with a font override.
     */
    public static TemplateReplacement withFont(String placeholder, String text, Font font) {
        return new TemplateReplacement(placeholder, text, font, null);
    }

    /**
     * Creates a replacement with a color override.
     */
    public static TemplateReplacement withColor(String placeholder, String text, Color color) {
        return new TemplateReplacement(placeholder, text, null, color);
    }

    /**
     * Creates a replacement with font and color overrides.
     */
    public static TemplateReplacement withFormatting(String placeholder, String text, Font font, Color color) {
        return new TemplateReplacement(placeholder, text, font, color);
    }

    public String placeholder() {
        return placeholder;
    }

    public String text() {
        return text;
    }

    public Font font() {
        return font;
    }

    public Color color() {
        return color;
    }

    public Image image() {
        return image;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        TemplateReplacement that = (TemplateReplacement) obj;
        return Objects.equals(this.placeholder, that.placeholder) &&
                Objects.equals(this.text, that.text) &&
                Objects.equals(this.font, that.font) &&
                Objects.equals(this.color, that.color) &&
                Objects.equals(this.image, that.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeholder, text, font, color, image);
    }

    @Override
    public String toString() {
        return "TemplateReplacement[" +
                "placeholder=" + placeholder + ", " +
                "text=" + text + ", " +
                "font=" + font + ", " +
                "color=" + color + ", " +
                "image=" + image + ']';
    }
}
