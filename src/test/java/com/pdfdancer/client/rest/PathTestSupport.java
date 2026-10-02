package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.BoundingRect;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Geometry based selectors for fixture paths whose IDs can change after a mutation. */
final class PathTestSupport {
    private static final double BOUNDS_EPSILON = 0.1;

    private PathTestSupport() {
    }

    static PathReference pathWithBounds(List<PathReference> paths, BoundingRect expectedBounds) {
        List<PathReference> matches = paths.stream()
                .filter(path -> boundsMatch(path.getPosition().getBoundingRect(), expectedBounds))
                .toList();
        assertEquals(1, matches.size(), "Expected exactly one path with bounds " + expectedBounds);
        return matches.get(0);
    }

    static List<String> idsForPathsWithBounds(List<PathReference> paths, BoundingRect... expectedBounds) {
        return java.util.Arrays.stream(expectedBounds)
                .map(bounds -> pathWithBounds(paths, bounds).getInternalId())
                .toList();
    }

    static boolean boundsMatch(BoundingRect actual, BoundingRect expected) {
        return actual != null
                && close(actual.getX(), expected.getX())
                && close(actual.getY(), expected.getY())
                && close(actual.getWidth(), expected.getWidth())
                && close(actual.getHeight(), expected.getHeight());
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) <= BOUNDS_EPSILON;
    }
}
