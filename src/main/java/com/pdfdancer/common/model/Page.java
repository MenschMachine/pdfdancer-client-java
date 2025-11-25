package com.pdfdancer.common.model;

import com.pdfdancer.common.model.path.PathSegment;
import com.pdfdancer.common.model.text.Paragraph;
import com.pdfdancer.common.model.text.TextElement;

import java.util.List;

/**
 * Represents a single page within a PDF document, containing all content elements.
 * This class encapsulates a complete page structure including text, images, forms,
 * and vector graphics, providing a comprehensive view of page content for
 * manipulation and analysis operations.
 */
public class Page extends PDFObject {
    /**
     * Unique identifier for this page within the PDF document context.
     */
    private String id;
    /**
     * Sequential page number within the document (1-based).
     */
    private int pageNumber;
    /**
     * Physical dimensions of the page including width and height.
     */
    private PageSize size;
    /**
     * Individual character elements present on this page.
     */
    private List<TextElement> PDFChars;
    /**
     * Paragraph-level text blocks identified on this page.
     */
    private List<Paragraph> paragraphs;
    /**
     * Image elements embedded in or overlaid on this page.
     */
    private List<Image> images;
    /**
     * Vector path segments representing shapes and graphics on this page.
     */
    private List<PathSegment> paths;
    /**
     * Interactive form elements present on this page.
     */
    private List<Form> forms;

    /**
     * Default constructor for serialization frameworks.
     * Creates an uninitialized page that should be populated with content.
     */
    public Page() {
    }

    /**
     * Creates a page with essential identification and dimensional properties.
     * This constructor initializes the fundamental page characteristics,
     * with content elements to be added separately.
     *
     * @param id        unique identifier for the page
     * @param pageNumber sequential number of this page in the document
     * @param size      physical dimensions of the page
     */
    public Page(String id, int pageNumber, PageSize size) {
        this.id = id;
        this.pageNumber = pageNumber;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public PageSize getSize() {
        return size;
    }

    public void setSize(PageSize size) {
        this.size = size;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<PathSegment> getPaths() {
        return paths;
    }

    public void setPaths(List<PathSegment> paths) {
        this.paths = paths;
    }

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    /**
     * Creates an object reference for this page.
     * This method generates a lightweight reference that can be used
     * in API operations to identify this page without transferring
     * its complete content structure.
     *
     * @return an ObjectRef representing this page
     */
    public PageRef toObjectRef() {
        return new PageRef(this.getId(), Position.atPage(this.getPageNumber()), this.getObjectType(), this.getObjectType(), this.size, Orientation.PORTRAIT); // TODO
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.PAGE;
    }

    @Override
    public Position getPosition() {
        return Position.atPage(this.getPageNumber());
    }

    @Override
    public void setPosition(Position position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
