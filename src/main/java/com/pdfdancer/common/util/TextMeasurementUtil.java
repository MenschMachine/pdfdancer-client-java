package com.pdfdancer.common.util;

import com.pdfdancer.common.model.Font;
import com.pdfdancer.common.model.Position;

public class TextMeasurementUtil {

    /**
     * Default baseline-to-baseline distance as a factor of font size.
     * For example, 1.2 means the baseline of the next line is 1.2 * fontSize below the current baseline.
     */
    public static final double DEFAULT_LINE_SPACING_FACTOR = 1.2;

    /**
     * Calculates the absolute baseline-to-baseline distance for a given font and spacing factor.
     *
     * @param font          the font to use for calculation
     * @param spacingFactor the factor to multiply by font size (e.g., 1.2)
     * @return absolute baseline-to-baseline distance in points
     */
    public static double calculateBaselineDistance(Font font, double spacingFactor) {
        if (font == null) {
            return 12.0 * DEFAULT_LINE_SPACING_FACTOR;
        }
        double factor = spacingFactor > 0 ? spacingFactor : DEFAULT_LINE_SPACING_FACTOR;
        return font.getSize() * factor;
    }

    /**
     * Calculates the position for a line at a given index within a paragraph.
     *
     * @param paragraphPosition the starting position of the paragraph (first line baseline)
     * @param lineIndex         the index of the line (0-based)
     * @param font              the font used for the text
     * @param spacingFactor     the factor to multiply by font size for baseline-to-baseline distance
     * @return the position for the line
     */
    public static Position calculateLinePosition(Position paragraphPosition, int lineIndex, Font font, double spacingFactor) {
        if (paragraphPosition == null) {
            return null;
        }

        double baselineDistance = calculateBaselineDistance(font, spacingFactor);
        double yOffset = lineIndex * baselineDistance;

        Double paragraphX = paragraphPosition.getX();
        Double paragraphY = paragraphPosition.getY();

        if (paragraphX == null || paragraphY == null) {
            return new Position(0, yOffset);
        }

        return new Position(paragraphX, paragraphY + yOffset);
    }
}