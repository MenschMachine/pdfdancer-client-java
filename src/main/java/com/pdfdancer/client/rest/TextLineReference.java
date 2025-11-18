package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.TextTypeObjectRef;

public class TextLineReference extends BaseReference {
    public TextLineReference(PDFDancer client, TextTypeObjectRef objectRef) {
        super(client, objectRef);
    }

    private TextTypeObjectRef ref() {
        return (TextTypeObjectRef) this.objectRef;
    }

    /**
     * Gets the text content of this line.
     *
     * @return the text content
     */
    public String getText() {
        return (ref()).getText();
    }

    /**
     * Gets the font name used in this line.
     *
     * @return the font name
     */
    public String getFontName() {
        return (ref()).getFontName();
    }

    /**
     * Gets the font size used in this line.
     *
     * @return the font size
     */
    public Double getFontSize() {
        return (ref()).getFontSize();
    }

    /**
     * Gets the text color of this line.
     *
     * @return the color
     */
    public Color getColor() {
        return (ref()).getColor();
    }

    public TextLineEdit edit() {
        return new TextLineEdit(client, objectRef);
    }

    public static class TextLineEdit {

        private final PDFDancer client;
        private final TextTypeObjectRef ref;
        private String newText;
        private Font newFont;
        private Position newPosition;
        private Color newColor;

        public TextLineEdit(PDFDancer client, ObjectRef ref) {
            this.client = client;
            this.ref = (TextTypeObjectRef) ref;
        }

        /**
         * Sets the new text content for this text line.
         * @param newText the replacement text
         * @return this TextLineEdit for method chaining
         */
        public TextLineEdit replace(String newText) {
            this.newText = newText;
            return this;
        }

        /**
         * Sets the font for this text line.
         * @param fontName the font name
         * @param fontSize the font size
         * @return this TextLineEdit for method chaining
         */
        public TextLineEdit font(String fontName, double fontSize) {
            this.newFont = new Font(fontName, fontSize);
            return this;
        }

        /**
         * Sets the font for this text line.
         * @param font the font to use
         * @return this TextLineEdit for method chaining
         */
        public TextLineEdit font(Font font) {
            this.newFont = font;
            return this;
        }

        /**
         * Moves this text line to a new position.
         * @param x the new x coordinate
         * @param y the new y coordinate
         * @return this TextLineEdit for method chaining
         */
        public TextLineEdit moveTo(double x, double y) {
            this.newPosition = Position.atPageCoordinates(ref.getPosition().getPageIndex(), x, y);
            return this;
        }

        /**
         * Sets the color for this text line.
         * @param color the new color
         * @return this TextLineEdit for method chaining
         */
        public TextLineEdit color(Color color) {
            this.newColor = color;
            return this;
        }

        /**
         * Applies all the modifications to the text line.
         * If only text is changed, uses the simple text line modification.
         * If font, position, or color are changed, converts to a single-line paragraph for rich modification.
         * @return true if the modification was successful
         */
        public boolean apply() {
            // Simple case: only text changed
            if (newFont == null && newPosition == null && newColor == null) {
                String textToUse = newText != null ? newText : ref.getText();
                return client.modifyTextLine(ref, textToUse);
            }

            // Complex case: need to modify font, position, or color
            // Convert to a single-line paragraph
            ParagraphBuilder builder = new ParagraphBuilder(client);

            // Text
            if (newText != null) {
                builder.text(newText);
            } else if (ref.getText() != null) {
                builder.text(ref.getText());
            }

            // Font
            if (newFont != null) {
                builder.font(newFont);
            } else if (ref.getFontName() != null && ref.getFontSize() != null) {
                builder.font(ref.getFontName(), ref.getFontSize());
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

            // Modify as a paragraph (which is just a single line)
            return client.modifyParagraph(ref, builder.finalizeParagraph());
        }

    }
}
