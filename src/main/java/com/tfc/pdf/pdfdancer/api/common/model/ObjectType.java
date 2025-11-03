package com.tfc.pdf.pdfdancer.api.common.model;
/**
 * Enumeration of PDF object types supported by the PDFDancer API.
 * This enum categorizes the different types of objects that can exist within
 * a PDF document, enabling type-safe operations and filtering across the API.
 * Each type represents a distinct category of PDF content with specific
 * manipulation capabilities and properties.
 */
public enum ObjectType {
    /**
     * Root PDF document object containing all pages and metadata
     */
    PDF,
    /**
     * Individual page within the PDF document
     */
    PAGE,
    /**
     * Single character or glyph element
     */
    TEXT_ELEMENT,
    /**
     * Text paragraph or block element
     */
    PARAGRAPH,
    /**
     * Embedded image or graphics element
     */
    IMAGE,
    /**
     * Vector path or shape element
     */
    PATH,
    /**
     * Linear path segment
     */
    LINE,
    /**
     * Rectangular shape element
     */
    RECTANGLE,
    /**
     * Bezier curve path element
     */
    BEZIER,
    /**
     * Clipping region or mask
     */
    CLIPPING,
    /**
     * FormXObject container
     */
    FORM_X_OBJECT,
    /**
     * Interactive acroform field element
     */
    FORM_FIELD,
    WORD,
    TEXT_LINE,
    TEXT_FIELD,
    RADIO_BUTTON,
    BUTTON,
    DROPDOWN,
    CHECKBOX;
}
