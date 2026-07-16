package com.pdfdancer.common.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardFontsTest {

    @Test
    void exposesAllFourteenStandardPdfFontsWithUniqueNames() {
        Set<String> names = Arrays.stream(StandardFonts.values())
                .map(StandardFonts::getFontName)
                .collect(Collectors.toSet());

        assertEquals(14, StandardFonts.values().length);
        assertEquals(14, names.size());
        assertEquals("Times-Roman", StandardFonts.TIMES_ROMAN.getFontName());
        assertEquals("Helvetica-BoldOblique", StandardFonts.HELVETICA_BOLD_OBLIQUE.getFontName());
        assertEquals("Courier-BoldOblique", StandardFonts.COURIER_BOLD_OBLIQUE.getFontName());
        assertEquals("Symbol", StandardFonts.SYMBOL.getFontName());
        assertEquals("ZapfDingbats", StandardFonts.ZAPF_DINGBATS.getFontName());
        assertTrue(names.stream().noneMatch(name -> name.isBlank() || name.contains(" ")));
    }

    @Test
    void recognizesNamesCaseInsensitivelyAndWithSubsetPrefixes() {
        assertTrue(StandardFonts.isStandardFont("Helvetica"));
        assertTrue(StandardFonts.isStandardFont("helvetica"));
        assertTrue(StandardFonts.isStandardFont("ABCDEF+Helvetica-Bold"));
        assertFalse(StandardFonts.isStandardFont("InvalidFont"));
        assertFalse(StandardFonts.isStandardFont(null));
    }
}
