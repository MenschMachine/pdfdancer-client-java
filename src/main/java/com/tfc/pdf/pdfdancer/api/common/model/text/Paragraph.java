package com.tfc.pdf.pdfdancer.api.common.model.text;
import com.tfc.pdf.pdfdancer.api.common.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Represents a paragraph or text block within a PDF document.
 * This class encapsulates paragraph-level text content with unified formatting
 * properties and access to individual character elements for detailed manipulation.
 * Provides both block-level text operations and character-level granularity.
 */
public class Paragraph extends PDFObject {
    private final List<TextLine> lines = new ArrayList<>();
    /**
     * Line spacing factors (not absolute distances) applied between consecutive lines.
     * Each value is a multiplication factor of the font size (e.g., 1.2 means 1.2 * fontSize).
     * The list size should be lines.size() - 1 (one spacing value between each pair of lines).
     * These factors are converted to absolute baseline-to-baseline distances internally.
     */
    private List<Double> lineSpacings;
    private Font font;
    private String text;
    /**
     * Default constructor for serialization frameworks.
     */
    public Paragraph() {
        super();
    }
    /**
     * Creates a paragraph with specified text content and formatting.
     *
     * @param id       unique identifier for the paragraph
     * @param lines    the lines of text this paragraph consists of
     * @param position location within the PDF document
     */
    public Paragraph(String id, List<TextLine> lines, Position position) {
        super(id, position);
        this.lines.addAll(lines);
    }
    /**
     * Returns the object type for this paragraph.
     *
     * @return ObjectType.PARAGRAPH indicating this is a paragraph object
     */
    @Override
    protected ObjectType getObjectType() {
        return ObjectType.PARAGRAPH;
    }
    public List<TextLine> getLines() {
        return lines;
    }
    public void setLines(List<TextLine> lines) {
        this.lines.clear();
        this.lines.addAll(lines);
    }
    /**
     * Gets the line spacing factors between consecutive lines.
     *
     * @return list of spacing factors (e.g., 1.2, 1.5) that multiply the font size to determine baseline-to-baseline distance
     */
    public List<Double> getLineSpacings() {
        return lineSpacings;
    }
    /**
     * Sets the line spacing factors between consecutive lines.
     *
     * @param lineSpacings list of spacing factors (not absolute distances).
     *                     Each value multiplies the font size to determine baseline-to-baseline distance.
     *                     For example, 1.2 means 1.2 * fontSize pixels between baselines.
     */
    public void setLineSpacings(List<Double> lineSpacings) {
        this.lineSpacings = lineSpacings;
    }
    public void setFont(Font font) {
        this.font = font;
    }
    public Font getFont() {
        return font;
    }
    public void clearLines() {
        lines.clear();
    }
    public void addLine(TextLine textLine) {
        this.lines.add(textLine);
    }
    @Override
    public TextTypeObjectRef toObjectRef() {
        return new TextTypeObjectRef(this.getId(),
                getPosition(),
                this.getObjectType(),
                this.getObjectType(),
                font != null ? font.getName() : null,
                font != null ? font.getSize() : null,
                this.getText(),
                this.lineSpacings,
                this.lines.stream().filter(l -> l.getColor() != null).findFirst().map(TextLine::getColor).orElse(null),
                TextStatus.fromParagraph(this),
                this.lines.stream().map(TextLine::toObjectRef).toList()
        );
    }
    public String getText() {
        if (text != null) {
            return text;
        } else {
            return this.lines.stream().map(TextLine::getText).collect(Collectors.joining("\n"));
        }
    }
    public void setText(String text) {
        this.text = text;
    }
}
