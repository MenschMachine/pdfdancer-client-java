package com.pdfdancer.common.model;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelBehaviorTest {

    @Test
    void objectTypeContainsEveryPublicObjectCategory() {
        assertEquals(Set.of(
                "PDF", "PAGE", "TEXT_ELEMENT", "IMAGE", "PATH", "LINE", "RECTANGLE", "BEZIER",
                "CLIPPING", "FORM_X_OBJECT", "FORM_FIELD", "WORD", "TEXT_LINE", "TEXT_FIELD",
                "RADIO_BUTTON", "BUTTON", "DROPDOWN", "CHECKBOX"),
                Stream.of(ObjectType.values()).map(Enum::name).collect(Collectors.toSet()));
    }

    @Test
    void positionFactoriesAndMovementExposeEquivalentCoordinates() {
        Position page = Position.atPage(2);
        assertEquals(2, page.getPageNumber());
        assertEquals(Position.PositionMode.CONTAINS, page.getMode());
        assertNull(page.getBoundingRect());

        Position point = Position.atPageCoordinates(1, 100.5, 200.75);
        assertEquals(100.5, point.getX());
        assertEquals(200.75, point.getY());
        assertEquals(Position.ShapeType.POINT, point.getShape());
        point.moveX(25);
        point.moveY(-50);
        assertEquals(125.5, point.getX());
        assertEquals(150.75, point.getY());
    }

    @Test
    void colorConstructionExposesComponentsAndRejectsOutOfRangeValues() {
        Color color = new Color(255, 128, 64, 32);
        assertEquals(255, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(64, color.getBlue());
        assertEquals(32, color.getAlpha());

        assertThrows(IllegalArgumentException.class, () -> new Color(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Color(0, 256, 0));
        assertThrows(IllegalArgumentException.class, () -> new Color(0, 0, 300));
    }

    @Test
    void fontConstructionExposesNameAndSizeAndRejectsNonpositiveSizes() {
        Font font = new Font("Helvetica", 12.5);
        assertEquals("Helvetica", font.getName());
        assertEquals(12.5, font.getSize());

        assertThrows(IllegalArgumentException.class, () -> new Font("Helvetica", 0));
        assertThrows(IllegalArgumentException.class, () -> new Font("Helvetica", -1));
        assertThrows(IllegalArgumentException.class, () -> new Font("Helvetica", Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new Font("Helvetica", Double.POSITIVE_INFINITY));

        assertThrows(IllegalArgumentException.class, () -> font.setSize(0));
        assertThrows(IllegalArgumentException.class, () -> font.setSize(Double.NEGATIVE_INFINITY));
        assertEquals(12.5, font.getSize());
    }

    @Test
    void boundingRectangleExposesAllComponents() {
        BoundingRect rectangle = new BoundingRect(10.5, 20.75, 100, 50);
        assertEquals(10.5, rectangle.getX());
        assertEquals(20.75, rectangle.getY());
        assertEquals(100, rectangle.getWidth());
        assertEquals(50, rectangle.getHeight());
    }
}
