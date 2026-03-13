package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.BoundingRect;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class PdfDrawInspector {
    private static final double DEFAULT_TOLERANCE = 0.5;
    private static final double TEXT_TOLERANCE = 2.0;
    private static final Set<String> PAINT_OPERATORS = Set.of("S", "s", "f", "F", "f*", "B", "B*", "b", "b*");
    private static final Set<String> PATH_OPERATORS = Set.of("m", "l", "c", "v", "y", "h", "re");

    private final PDFDancer pdf;
    private final File savedPdfFile;
    private final Map<Integer, DrawEvents> pageCache = new HashMap<>();

    PdfDrawInspector(PDFDancer pdf, File savedPdfFile) {
        this.pdf = pdf;
        this.savedPdfFile = savedPdfFile;
    }

    boolean pathHasClipping(String internalId, int pageNumber) {
        List<PathReference> paths = pdf.page(pageNumber).selectPaths();
        PathReference path = paths.stream()
                .filter(candidate -> Objects.equals(candidate.getInternalId(), internalId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No path found with id " + internalId));
        Double x = path.getPosition().getX();
        Double y = path.getPosition().getY();
        assertNotNull(x, "Path " + internalId + " has no x position");
        assertNotNull(y, "Path " + internalId + " has no y position");

        List<DrawEvent> matches = getPageDrawEvents(pageNumber).paths.stream()
                .filter(event -> containsPoint(event.bbox, x, y, DEFAULT_TOLERANCE))
                .collect(Collectors.toList());
        assertFalse(matches.isEmpty(), "No path draw event found near " + internalId + " at (" + x + ", " + y + ")");
        DrawEvent best = matches.stream()
                .min((left, right) -> Double.compare(area(left.bbox), area(right.bbox)))
                .orElseThrow();
        return best.clipped;
    }

    boolean imageHasClipping(String internalId, int pageNumber) {
        List<ImageReference> images = pdf.page(pageNumber).selectImages();
        ImageReference image = images.stream()
                .filter(candidate -> Objects.equals(candidate.getInternalId(), internalId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No image found with id " + internalId));
        BoundingRect bbox = image.getPosition().getBoundingRect();
        assertNotNull(bbox, "Image " + internalId + " has no bounding box");
        BBox target = new BBox(bbox.getX(), bbox.getY(), bbox.getX() + bbox.getWidth(), bbox.getY() + bbox.getHeight());

        List<DrawEvent> matches = getPageDrawEvents(pageNumber).images.stream()
                .filter(event -> intersectionArea(target, event.bbox) > 0)
                .collect(Collectors.toList());
        assertFalse(matches.isEmpty(), "No image draw event found for " + internalId);
        DrawEvent best = matches.stream()
                .max((left, right) -> Double.compare(intersectionArea(target, left.bbox), intersectionArea(target, right.bbox)))
                .orElseThrow();
        return best.clipped;
    }

    boolean textLineHasClipping(String text, int pageNumber) {
        List<TextLineReference> lines = pdf.page(pageNumber).selectTextLinesStartingWith(text);
        assertFalse(lines.isEmpty(), "No text line starting with " + text);
        return textAtHasClipping(lines.get(0).getPosition(), pageNumber);
    }

    boolean paragraphHasClipping(String text, int pageNumber) {
        List<TextParagraphReference> paragraphs = pdf.page(pageNumber).selectParagraphsStartingWith(text);
        assertFalse(paragraphs.isEmpty(), "No paragraph starting with " + text);
        return textAtHasClipping(paragraphs.get(0).getPosition(), pageNumber);
    }

    private boolean textAtHasClipping(com.pdfdancer.common.model.Position position, int pageNumber) {
        Double x = position.getX();
        Double y = position.getY();
        assertNotNull(x, "Text object has no x position");
        assertNotNull(y, "Text object has no y position");
        List<DrawEvent> matches = getPageDrawEvents(pageNumber).texts.stream()
                .filter(event -> containsPoint(event.bbox, x, y, TEXT_TOLERANCE))
                .collect(Collectors.toList());
        assertFalse(matches.isEmpty(), "No text draw event found near (" + x + ", " + y + ")");
        DrawEvent best = matches.stream()
                .min((left, right) -> Double.compare(centerDistance(left.bbox, x, y), centerDistance(right.bbox, x, y)))
                .orElseThrow();
        return best.clipped;
    }

    private DrawEvents getPageDrawEvents(int pageNumber) {
        return pageCache.computeIfAbsent(pageNumber, this::parsePageDrawEvents);
    }

    private DrawEvents parsePageDrawEvents(int pageNumber) {
        List<DrawEvent> pathEvents = new ArrayList<>();
        List<DrawEvent> imageEvents = new ArrayList<>();
        List<DrawEvent> textEvents = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(savedPdfFile)) {
            PDPage page = document.getPage(pageNumber - 1);
            ArrayDeque<GraphicsState> stateStack = new ArrayDeque<>();
            GraphicsState state = GraphicsState.initial();
            List<Point> currentPathPoints = new ArrayList<>();

            java.util.Iterator<PDStream> contentStreams = page.getContentStreams();
            while (contentStreams.hasNext()) {
                PDStream stream = contentStreams.next();
                PDFStreamParser parser = new PDFStreamParser(stream.createInputStream().readAllBytes());
                List<Object> tokens = parser.parse();
                List<COSBase> operands = new ArrayList<>();

                for (Object token : tokens) {
                    if (!(token instanceof Operator)) {
                        if (token instanceof COSBase) {
                            operands.add((COSBase) token);
                        }
                        continue;
                    }

                    Operator operator = (Operator) token;
                    String name = operator.getName();
                    switch (name) {
                        case "q":
                            stateStack.push(state.copy());
                            operands.clear();
                            break;
                        case "Q":
                            state = stateStack.isEmpty() ? GraphicsState.initial() : stateStack.pop();
                            currentPathPoints.clear();
                            operands.clear();
                            break;
                        case "cm": {
                            double[] cmValues = numberOperands(operands, 6);
                            if (cmValues != null) {
                                state.ctm = Matrix2D.multiply(Matrix2D.of(cmValues), state.ctm);
                            }
                            operands.clear();
                            break;
                        }
                        case "W":
                        case "W*":
                            state.pendingClip = true;
                            operands.clear();
                            break;
                        case "n":
                            if (state.pendingClip) {
                                state.hasClip = true;
                                state.pendingClip = false;
                            }
                            currentPathPoints.clear();
                            operands.clear();
                            break;
                        case "BT":
                            state.inText = true;
                            state.textMatrix = Matrix2D.identity();
                            state.textLineMatrix = Matrix2D.identity();
                            operands.clear();
                            break;
                        case "ET":
                            state.inText = false;
                            operands.clear();
                            break;
                        case "Tf": {
                            double[] tfValues = numberOperands(operands, 1);
                            if (tfValues != null) {
                                state.fontSize = Math.abs(tfValues[0]);
                            }
                            operands.clear();
                            break;
                        }
                        case "Tm": {
                            double[] tmValues = numberOperands(operands, 6);
                            if (tmValues != null) {
                                state.textMatrix = Matrix2D.of(tmValues);
                                state.textLineMatrix = state.textMatrix.copy();
                            }
                            operands.clear();
                            break;
                        }
                        case "Td":
                        case "TD": {
                            double[] tdValues = numberOperands(operands, 2);
                            if (tdValues != null) {
                                Matrix2D translation = new Matrix2D(1, 0, 0, 1, tdValues[0], tdValues[1]);
                                state.textLineMatrix = Matrix2D.multiply(translation, state.textLineMatrix);
                                state.textMatrix = state.textLineMatrix.copy();
                                if ("TD".equals(name)) {
                                    state.textLeading = -tdValues[1];
                                }
                            }
                            operands.clear();
                            break;
                        }
                        case "T*":
                            moveTextToNextLine(state);
                            operands.clear();
                            break;
                        case "'":
                        case "\"":
                            moveTextToNextLine(state);
                            textEvents.add(textEvent(state));
                            operands.clear();
                            break;
                        case "Tj":
                        case "TJ":
                            if (state.inText) {
                                textEvents.add(textEvent(state));
                            }
                            operands.clear();
                            break;
                        case "Do":
                            imageEvents.add(imageEvent(state));
                            operands.clear();
                            break;
                        default:
                            if (PATH_OPERATORS.contains(name)) {
                                applyPathOperator(name, operands, state.ctm, currentPathPoints);
                                operands.clear();
                            } else if (PAINT_OPERATORS.contains(name)) {
                                if (!currentPathPoints.isEmpty()) {
                                    pathEvents.add(pathEvent(currentPathPoints, state));
                                }
                                if (state.pendingClip) {
                                    state.hasClip = true;
                                    state.pendingClip = false;
                                }
                                currentPathPoints.clear();
                                operands.clear();
                            } else {
                                operands.clear();
                            }
                            break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to inspect saved PDF draw events", e);
        }

        return new DrawEvents(pathEvents, imageEvents, textEvents);
    }

    private static void moveTextToNextLine(GraphicsState state) {
        Matrix2D translation = new Matrix2D(1, 0, 0, 1, 0, -state.textLeading);
        state.textLineMatrix = Matrix2D.multiply(translation, state.textLineMatrix);
        state.textMatrix = state.textLineMatrix.copy();
    }

    private static DrawEvent textEvent(GraphicsState state) {
        Point origin = state.ctm.apply(state.textMatrix.e, state.textMatrix.f);
        double padding = Math.max(4.0, state.fontSize * 0.5);
        return new DrawEvent(
                new BBox(origin.x - padding, origin.y - padding, origin.x + padding, origin.y + padding),
                state.hasClip || state.pendingClip
        );
    }

    private static DrawEvent imageEvent(GraphicsState state) {
        Point p0 = state.ctm.apply(0, 0);
        Point p1 = state.ctm.apply(1, 0);
        Point p2 = state.ctm.apply(0, 1);
        Point p3 = state.ctm.apply(1, 1);
        double minX = Math.min(Math.min(p0.x, p1.x), Math.min(p2.x, p3.x));
        double minY = Math.min(Math.min(p0.y, p1.y), Math.min(p2.y, p3.y));
        double maxX = Math.max(Math.max(p0.x, p1.x), Math.max(p2.x, p3.x));
        double maxY = Math.max(Math.max(p0.y, p1.y), Math.max(p2.y, p3.y));
        return new DrawEvent(new BBox(minX, minY, maxX, maxY), state.hasClip || state.pendingClip);
    }

    private static DrawEvent pathEvent(List<Point> currentPathPoints, GraphicsState state) {
        double minX = currentPathPoints.stream().mapToDouble(point -> point.x).min().orElseThrow();
        double minY = currentPathPoints.stream().mapToDouble(point -> point.y).min().orElseThrow();
        double maxX = currentPathPoints.stream().mapToDouble(point -> point.x).max().orElseThrow();
        double maxY = currentPathPoints.stream().mapToDouble(point -> point.y).max().orElseThrow();
        return new DrawEvent(new BBox(minX, minY, maxX, maxY), state.hasClip || state.pendingClip);
    }

    private static void applyPathOperator(String name, List<COSBase> operands, Matrix2D ctm, List<Point> currentPathPoints) {
        switch (name) {
            case "m":
            case "l": {
                double[] lineValues = numberOperands(operands, 2);
                if (lineValues != null) {
                    currentPathPoints.add(ctm.apply(lineValues[0], lineValues[1]));
                }
                break;
            }
            case "c": {
                double[] curveValues = numberOperands(operands, 6);
                if (curveValues != null) {
                    currentPathPoints.add(ctm.apply(curveValues[0], curveValues[1]));
                    currentPathPoints.add(ctm.apply(curveValues[2], curveValues[3]));
                    currentPathPoints.add(ctm.apply(curveValues[4], curveValues[5]));
                }
                break;
            }
            case "v":
            case "y": {
                double[] shortCurveValues = numberOperands(operands, 4);
                if (shortCurveValues != null) {
                    currentPathPoints.add(ctm.apply(shortCurveValues[0], shortCurveValues[1]));
                    currentPathPoints.add(ctm.apply(shortCurveValues[2], shortCurveValues[3]));
                }
                break;
            }
            case "re": {
                double[] rectValues = numberOperands(operands, 4);
                if (rectValues != null) {
                    double x = rectValues[0];
                    double y = rectValues[1];
                    double width = rectValues[2];
                    double height = rectValues[3];
                    currentPathPoints.add(ctm.apply(x, y));
                    currentPathPoints.add(ctm.apply(x + width, y));
                    currentPathPoints.add(ctm.apply(x + width, y + height));
                    currentPathPoints.add(ctm.apply(x, y + height));
                }
                break;
            }
            default:
                break;
        }
    }

    private static double[] numberOperands(List<COSBase> operands, int count) {
        if (operands.size() < count) {
            return null;
        }
        double[] values = new double[count];
        int start = operands.size() - count;
        for (int i = 0; i < count; i++) {
            COSBase operand = operands.get(start + i);
            if (!(operand instanceof COSNumber)) {
                return null;
            }
            values[i] = ((COSNumber) operand).floatValue();
        }
        return values;
    }

    private static boolean containsPoint(BBox bbox, double x, double y, double tolerance) {
        return bbox.minX - tolerance <= x && x <= bbox.maxX + tolerance
                && bbox.minY - tolerance <= y && y <= bbox.maxY + tolerance;
    }

    private static double intersectionArea(BBox left, BBox right) {
        double intersectMinX = Math.max(left.minX, right.minX);
        double intersectMinY = Math.max(left.minY, right.minY);
        double intersectMaxX = Math.min(left.maxX, right.maxX);
        double intersectMaxY = Math.min(left.maxY, right.maxY);
        if (intersectMinX >= intersectMaxX || intersectMinY >= intersectMaxY) {
            return 0;
        }
        return (intersectMaxX - intersectMinX) * (intersectMaxY - intersectMinY);
    }

    private static double area(BBox bbox) {
        return Math.max(0, bbox.maxX - bbox.minX) * Math.max(0, bbox.maxY - bbox.minY);
    }

    private static double centerDistance(BBox bbox, double x, double y) {
        double centerX = (bbox.minX + bbox.maxX) / 2.0;
        double centerY = (bbox.minY + bbox.maxY) / 2.0;
        return Math.hypot(centerX - x, centerY - y);
    }

    private static final class DrawEvents {
        private final List<DrawEvent> paths;
        private final List<DrawEvent> images;
        private final List<DrawEvent> texts;

        private DrawEvents(List<DrawEvent> paths, List<DrawEvent> images, List<DrawEvent> texts) {
            this.paths = paths;
            this.images = images;
            this.texts = texts;
        }
    }

    private static final class DrawEvent {
        private final BBox bbox;
        private final boolean clipped;

        private DrawEvent(BBox bbox, boolean clipped) {
            this.bbox = bbox;
            this.clipped = clipped;
        }
    }

    private static final class BBox {
        private final double minX;
        private final double minY;
        private final double maxX;
        private final double maxY;

        private BBox(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    private static final class Point {
        private final double x;
        private final double y;

        private Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final class GraphicsState {
        private boolean hasClip;
        private boolean pendingClip;
        private Matrix2D ctm;
        private boolean inText;
        private Matrix2D textMatrix;
        private Matrix2D textLineMatrix;
        private double textLeading;
        private double fontSize;

        private static GraphicsState initial() {
            GraphicsState state = new GraphicsState();
            state.ctm = Matrix2D.identity();
            state.textMatrix = Matrix2D.identity();
            state.textLineMatrix = Matrix2D.identity();
            state.fontSize = 12.0;
            return state;
        }

        private GraphicsState copy() {
            GraphicsState copy = new GraphicsState();
            copy.hasClip = hasClip;
            copy.pendingClip = pendingClip;
            copy.ctm = ctm.copy();
            copy.inText = inText;
            copy.textMatrix = textMatrix.copy();
            copy.textLineMatrix = textLineMatrix.copy();
            copy.textLeading = textLeading;
            copy.fontSize = fontSize;
            return copy;
        }
    }

    private static final class Matrix2D {
        private final double a;
        private final double b;
        private final double c;
        private final double d;
        private final double e;
        private final double f;

        private Matrix2D(double a, double b, double c, double d, double e, double f) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
        }

        private static Matrix2D identity() {
            return new Matrix2D(1, 0, 0, 1, 0, 0);
        }

        private static Matrix2D of(double[] values) {
            return new Matrix2D(values[0], values[1], values[2], values[3], values[4], values[5]);
        }

        private static Matrix2D multiply(Matrix2D left, Matrix2D right) {
            return new Matrix2D(
                    left.a * right.a + left.b * right.c,
                    left.a * right.b + left.b * right.d,
                    left.c * right.a + left.d * right.c,
                    left.c * right.b + left.d * right.d,
                    left.e * right.a + left.f * right.c + right.e,
                    left.e * right.b + left.f * right.d + right.f
            );
        }

        private Point apply(double x, double y) {
            return new Point(a * x + c * y + e, b * x + d * y + f);
        }

        private Matrix2D copy() {
            return new Matrix2D(a, b, c, d, e, f);
        }
    }
}
