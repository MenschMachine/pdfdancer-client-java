package com.tfc.pdf.pdfdancer.api.common.util;

import com.tfc.pdf.pdfdancer.api.common.model.Color;
import com.tfc.pdf.pdfdancer.api.common.model.Font;
import com.tfc.pdf.pdfdancer.api.common.model.Position;
import com.tfc.pdf.pdfdancer.api.common.model.TextStatus;
import com.tfc.pdf.pdfdancer.api.common.model.text.Paragraph;
import com.tfc.pdf.pdfdancer.api.common.model.text.TextLine;

import java.util.ArrayList;
import java.util.List;

public class ParagraphUtil {

    /**
     * Finalizes paragraph text by splitting it into lines and calculating positions and spacings.
     *
     * @param text              the text content to split into lines
     * @param paragraph         the paragraph to populate
     * @param color             the text color
     * @param lineSpacingFactor the factor to multiply by font size for baseline-to-baseline distance (e.g., 1.2)
     * @param font              the font to use
     */
    public static void finalizeText(String text, Paragraph paragraph, Color color, double lineSpacingFactor, Font font, TextStatus status) {
        paragraph.clearLines();
        paragraph.setFont(font);

        if (text != null && !text.trim().isEmpty()) {
            String[] lines = text.split("\n");
            List<Double> lineSpacings = new ArrayList<>();

            for (int i = 0; i < lines.length; i++) {
                Position linePosition = TextMeasurementUtil.calculateLinePosition(
                        paragraph.getPosition(),
                        i,
                        font,
                        lineSpacingFactor
                );

                paragraph.addLine(
                        TextLine.fromText(
                                lines[i],
                                linePosition,
                                color,
                                font,
                                status
                        )
                );

                // Add spacing factor for all lines except the last
                if (i < lines.length - 1) {
                    lineSpacings.add(lineSpacingFactor);
                }
            }

            // Set the spacing factors
            paragraph.setLineSpacings(lineSpacings);
        }
    }
}
