package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.*;
import com.pdfdancer.common.model.text.Paragraph;
import com.pdfdancer.common.model.text.TextLine;
import com.pdfdancer.common.util.ParagraphUtil;
import com.pdfdancer.common.util.TextMeasurementUtil;

import java.io.File;
import java.util.stream.Collectors;

public class ParagraphBuilder {

    private final static double DEFAULT_LINE_SPACING_FACTOR = TextMeasurementUtil.DEFAULT_LINE_SPACING_FACTOR;
    private final static Color DEFAULT_TEXT_COLOR = new Color(0, 0, 0);

    private final Paragraph paragraph = new Paragraph();
    private final PDFDancer client;
    private Double lineSpacing;
    private Color textColor;
    private String text;
    private File ttfFile;
    private Font font;
    private boolean fontExplicitlyChanged = false;
    private Position originalParagraphPosition;

    public ParagraphBuilder(PDFDancer client) {
        this.client = client;
    }

    public boolean onlyTextChanged() {
        return this.textColor == null && ttfFile == null && font == null && lineSpacing == null;
    }

    public ParagraphBuilder text(String text) {
        this.text = text;
        return this;
    }

    @SuppressWarnings("unused")
    public ParagraphBuilder text(String text, Color color) {
        this.text = text;
        this.textColor = color;
        return this;
    }

    public ParagraphBuilder font(Font font) {
        this.font = font;
        this.ttfFile = null;
        return this;
    }

    public ParagraphBuilder font(String fontName, double fontSize) {
        return this.font(new Font(fontName, fontSize));
    }

    public void setFontExplicitlyChanged(boolean fontExplicitlyChanged) {
        this.fontExplicitlyChanged = fontExplicitlyChanged;
    }

    public void setOriginalParagraphPosition(Position originalPosition) {
        this.originalParagraphPosition = originalPosition;
    }

    /**
     * Sets the line spacing factor for the paragraph.
     *
     * @param spacing spacing factor (not absolute distance), e.g., 1.2 means 1.2 * fontSize pixels between baselines.
     *                Values less than 1.0 reduce spacing, greater than 1.0 increase spacing.
     * @return this ParagraphBuilder for method chaining
     */
    public ParagraphBuilder lineSpacing(double spacing) {
        this.lineSpacing = spacing;
        return this;
    }

    public ParagraphBuilder color(Color color) {
        this.textColor = color;
        return this;
    }

    public ParagraphBuilder at(Position pos) {
        paragraph.setPosition(pos);
        return this;
    }

    public ParagraphBuilder at(int pageIndex, double x, double y) {
        paragraph.setPosition(Position.atPageCoordinates(pageIndex, x, y));
        return this;
    }

    public boolean add() {
        return client.addParagaph(finalizeParagraph());
    }

    protected Paragraph finalizeParagraph() {
        if (this.text != null) {
            // new text was set, calculate lines
            if (this.textColor == null) {
                this.textColor = DEFAULT_TEXT_COLOR;
            }
            if (this.lineSpacing == null) {
                this.lineSpacing = DEFAULT_LINE_SPACING_FACTOR;
            }
            ParagraphUtil.finalizeText(this.text, this.paragraph, this.textColor, this.lineSpacing, this.font, TextStatus.fromParagraph(paragraph));
        } else {
            if (this.font != null) {
                paragraph.setFont(this.font);
            }
            if (this.lineSpacing != null) {
                paragraph.setLineSpacings(paragraph.getLines().stream().map(l -> this.lineSpacing).collect(Collectors.toUnmodifiableList()));
            }
            // apply styles to original lines
            this.paragraph.getLines().forEach(line -> {
                if (this.textColor != null) {
                    line.setColor(this.textColor);
                }
                if (this.font != null && this.fontExplicitlyChanged) {
                    // Only update fonts if explicitly changed by user
                    line.setFontName(this.font.getName());
                    line.setFontSize(this.font.getSize());
                    // Also update text elements
                    line.getTextElements().forEach(elem -> elem.setFont(this.font));
                }
            });
        }
        // Compute deltas from original paragraph position to new paragraph position, then shift all lines
        Position pPos = paragraph.getPosition();
        if (pPos != null && !paragraph.getLines().isEmpty()) {
            // Use original paragraph position if available, otherwise use first line position
            double baseX, baseY;
            if (originalParagraphPosition != null) {
                baseX = originalParagraphPosition.getX();
                baseY = originalParagraphPosition.getY();
            } else if (paragraph.getLines().get(0).getPosition() != null) {
                baseX = paragraph.getLines().get(0).getPosition().getX();
                baseY = paragraph.getLines().get(0).getPosition().getY();
            } else {
                return paragraph;
            }

            double dx = pPos.getX() - baseX, dy = pPos.getY() - baseY;
            this.paragraph.getLines().forEach(line -> {
                if (line.getPosition() != null) {
                    line.getPosition().atPosition(line.getPosition().getX() + dx, line.getPosition().getY() + dy);
                }
            });
        }
        return paragraph;
    }

    public ParagraphBuilder font(File ttfFile, double fontSize) {
        if (!ttfFile.exists()) throw new IllegalArgumentException("TTF file does not exist");
        if (!ttfFile.isFile()) throw new IllegalArgumentException("TTF file is not a file");
        if (!ttfFile.canRead()) throw new IllegalArgumentException("TTF file is not readable");
        this.ttfFile = ttfFile;
        this.font = registerTTF(ttfFile, fontSize);
        return this;
    }

    private Font registerTTF(File ttfFile, double fontSize) {
        return new Font(this.client.registerFont(ttfFile), fontSize);
    }

    public String getText() {
        return this.text;
    }

    public void addTextLine(TextTypeObjectRef text) {
        TextLine textLine = TextLine.fromObjectRef(text);
        this.paragraph.addLine(textLine);
    }
}
