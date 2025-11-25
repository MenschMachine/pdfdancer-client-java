package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.Position;
import com.pdfdancer.common.model.TextTypeObjectRef;

public class TextParagraphReference extends BaseReference {

    public TextParagraphReference(TextTypeObjectRef objectRef,
                                  PDFDancer client) {
        super(client, objectRef);
    }

    private TextTypeObjectRef ref() {
        return (TextTypeObjectRef) this.objectRef;
    }

    /**
     * Gets the text content of this paragraph.
     *
     * @return the text content
     */
    public String getText() {
        return (ref()).getText();
    }

    /**
     * Gets the font name used in this paragraph.
     *
     * @return the font name
     */
    public String getFontName() {
        return (ref()).getFontName();
    }

    /**
     * Gets the font size used in this paragraph.
     *
     * @return the font size
     */
    public Double getFontSize() {
        return (ref()).getFontSize();
    }

    /**
     * Gets the text color of this paragraph.
     *
     * @return the color
     */
    public Color getColor() {
        return (ref()).getColor();
    }

    public TextEdit edit() {
        return new TextEdit(client, ref());
    }

    public static class TextEdit {

        private final PDFDancer client;
        private final TextTypeObjectRef ref;
        private String newText;
        private Font newFont;
        private Double newLineSpacing;
        private Position newPosition;
        private Color newColor;

        public TextEdit(PDFDancer client, TextTypeObjectRef ref) {
            this.client = client;
            this.ref = ref;
        }

        public TextEdit replace(String newText) {
            this.newText = newText;
            return this;
        }

        public TextEdit font(String fontName, double fontSize) {
            this.newFont = new Font(fontName, fontSize);
            return this;
        }

        /**
         * Sets the line spacing factor for the paragraph.
         *
         * @param spacing spacing factor (not absolute distance), e.g., 1.2 means 1.2 * fontSize pixels between baselines
         * @return this TextEdit for method chaining
         */
        public TextEdit lineSpacing(double spacing) {
            this.newLineSpacing = spacing;
            return this;
        }

        public TextEdit moveTo(double x, double y) {
            this.newPosition = Position.atPageCoordinates(ref.getPosition().getPageNumber(), x, y);
            return this;
        }

        public TextEdit color(Color color) {
            this.newColor = color;
            return this;
        }

        public boolean apply() {
            if (newPosition == null && newLineSpacing == null && newFont == null && newColor == null) {
                // only text changed, just replace everything
                String textToUse = newText != null ? newText : ref.getText();
                return client.modifyParagraph(ref, textToUse);
            } else {
                // Build complete paragraph with all properties
                ParagraphBuilder builder = new ParagraphBuilder(client);

                if (newText == null) {
                    // reuse original text lines
                    // Use the paragraph's position (anchor/lower-left) as the baseline for delta calculation
                    builder.setOriginalParagraphPosition(ref.getPosition());
                    ref.getChildren().forEach(child -> {
                        if (child != null) {
                            builder.addTextLine(child);
                        }
                    });
                } else {
                    // create new textLines
                    builder.text(newText);
                }

                // Font
                boolean fontExplicitlyChanged = newFont != null;
                if (fontExplicitlyChanged) {
                    builder.font(newFont);
                    builder.setFontExplicitlyChanged(true);
                } else if (ref.getFontName() != null && ref.getFontSize() != null) {
                    // Set font for paragraph-level metadata, but don't override per-element fonts
                    builder.font(ref.getFontName(), ref.getFontSize());
                }

                // Line spacing
                if (newLineSpacing != null) {
                    builder.lineSpacing(newLineSpacing);
                } else if (newText == null && ref.getLineSpacings() != null && !ref.getLineSpacings().isEmpty()) {
                    // Only preserve original spacing when reusing original text lines
                    double avgSpacing = ref.getLineSpacings().stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(1.2);
                    builder.lineSpacing(avgSpacing);
                }

                // Position
                if (newPosition != null) {
                    builder.at(newPosition);
                } else if (ref.getPosition() != null) {
                    builder.at(ref.getPosition());
                }

                // Color
                if (newColor != null) {
                    builder.color(newColor);
                } else if (ref.getColor() != null) {
                    builder.color(ref.getColor());
                }

                return client.modifyParagraph(ref, builder.finalizeParagraph());
            }
        }
    }
}
