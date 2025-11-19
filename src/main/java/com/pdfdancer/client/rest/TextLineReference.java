package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.Position;
import com.pdfdancer.common.model.TextTypeObjectRef;
import com.pdfdancer.common.model.text.TextLine;

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
         * If font, position, or color are changed, creates a TextLine object for rich modification.
         * @return true if the modification was successful
         */
        public boolean apply() {
            // Simple case: only text changed
            if (newFont == null && newPosition == null && newColor == null) {
                String textToUse = newText != null ? newText : ref.getText();
                return client.modifyTextLine(ref, textToUse);
            }

            // Complex case: need to modify font, position, or color
            // Build a TextLine object with all properties
            String textToUse = newText != null ? newText : ref.getText();
            Font fontToUse = newFont != null ? newFont : new Font(ref.getFontName(), ref.getFontSize());
            Position positionToUse = newPosition != null ? newPosition : ref.getPosition();
            Color colorToUse = newColor != null ? newColor : ref.getColor();

            TextLine textLine = TextLine.fromText(textToUse, positionToUse, colorToUse, fontToUse, ref.getStatus());

            return client.modifyTextLine(ref, textLine);
        }

    }
}
