package com.pdfdancer.common.model;

import com.pdfdancer.common.model.text.Paragraph;
import com.pdfdancer.common.model.text.TextElement;
import com.pdfdancer.common.model.text.TextLine;

import java.util.Objects;
import java.util.stream.Stream;

public class TextStatus {
    private boolean isModified = false;
    private boolean isEncodable = true;
    private FontType fontType;
    private DocumentFontInfoDto fontInfo;

    public TextStatus(boolean isModified,
                      boolean isEncodable,
                      FontType fontType,
                      DocumentFontInfoDto fontInfo) {
        this();
        this.isModified = isModified;
        this.isEncodable = isEncodable;
        this.fontType = fontType;
        this.fontInfo = fontInfo;
    }

    public TextStatus() {
        super();
    }

    public static TextStatus fromParagraph(Paragraph paragraph) {
        return accumulateTextStatus(
                paragraph.getLines().stream().flatMap(line -> line.getTextElements().stream())
        );
    }

    public static TextStatus fromTextLine(TextLine textLine) {
        return accumulateTextStatus(
                textLine.getTextElements().stream()
        );
    }

    private static TextStatus accumulateTextStatus(Stream<TextElement> elements) {
        return elements.reduce(
                new TextStatus(),
                (status, elem) -> mergeStatus(status, elem.getStatus()),
                TextStatus::combineStatuses
        );
    }

    public static TextStatus mergeStatus(TextStatus base, TextStatus update) {
        if (update == null) {
            return base;
        }
        if (update.isModified()) {
            base.isModified = true;
        }
        if (!update.isEncodable()) {
            base.isEncodable = false;
        }
        if (base.getFontInfoDto() == null) {
            base.fontInfo = update.getFontInfoDto();
        }
        if (base.getFontType() == null) {
            base.fontType = update.getFontType();
        }
        return base;
    }

    public static TextStatus combineStatuses(TextStatus s1, TextStatus s2) {
        if (s2 == null) {
            return s1;
        }
        if (s1 == null) {
            return s2;
        }
        if (s2.isModified()) {
            s1.isModified = true;
        }
        if (!s2.isEncodable()) {
            s1.isEncodable = false;
        }
        if (s1.fontInfo == null) {
            s1.fontInfo = s2.fontInfo;
        }
        if (s1.fontType == null) {
            s1.fontType = s2.fontType;
        }
        return s1;
    }

    public boolean isModified() {
        return isModified;
    }

    public boolean isEncodable() {
        return isEncodable;
    }

    public FontType getFontType() {
        return fontType;
    }

    public DocumentFontInfoDto getFontInfoDto() {
        return fontInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextStatus)) return false;
        TextStatus that = (TextStatus) o;
        return isModified == that.isModified
                && isEncodable == that.isEncodable
                && fontType == that.fontType
                && Objects.equals(fontInfo, that.fontInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isModified, isEncodable, fontType, fontInfo);
    }

    @Override
    public String toString() {
        return "TextStatus[" +
                "isModified=" + isModified +
                ", isEncodable=" + isEncodable +
                ", fontType=" + fontType +
                ", fontInfo=" + fontInfo +
                ']';
    }

    public String getWarning() {
        if (!isEncodable() && fontInfo != null) {
            return "Text is not encodable with your current font, we are using'" + getFontInfoDto().systemFontName() + "' as a fallback font instead.";
        }
        if (isModified() && fontType.equals(FontType.EMBEDDED)) {
            if (fontInfo != null && fontInfo.systemFontName() != null) {
                return null;
            }
            return "You are using an embedded font and modified the text. Even though the font reports to be able to render the new text, this is not guaranteed.\nPlease read https://docs.pdfdancer.com/notes/embedded-font-warning for more information and how to deal with this.";
        }
        return null;
    }
}
