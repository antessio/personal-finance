package antessio.personalfinance.infrastructure.report;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.Color;
import java.io.IOException;

/**
 * Helper class for consistent PDF layout, spacing, and positioning.
 * Provides utilities for margins, dividers, and layout calculations.
 */
public class PdfLayoutHelper {

    // Page margins
    public static final float PAGE_MARGIN = 60f;
    public static final float SECTION_SPACING = 30f;
    public static final float PARAGRAPH_SPACING = 15f;
    public static final float LINE_SPACING = 5f;

    // Font sizes
    public static final float TITLE_FONT_SIZE = 28f;
    public static final float SUBTITLE_FONT_SIZE = 18f;
    public static final float HEADING_FONT_SIZE = 16f;
    public static final float SUBHEADING_FONT_SIZE = 14f;
    public static final float NORMAL_FONT_SIZE = 12f;
    public static final float SMALL_FONT_SIZE = 10f;
    public static final float TINY_FONT_SIZE = 8f;

    /**
     * Calculate centered X position for text.
     *
     * @param pageWidth Width of the page
     * @param text Text to center
     * @param font Font used for text
     * @param fontSize Font size
     * @return X coordinate for centered text
     * @throws IOException if font width calculation fails
     */
    public static float calculateCenteredX(float pageWidth, String text, PDFont font, float fontSize) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        return (pageWidth - textWidth) / 2f;
    }

    /**
     * Calculate right-aligned X position for text.
     *
     * @param pageWidth Width of the page
     * @param text Text to align
     * @param font Font used for text
     * @param fontSize Font size
     * @param rightMargin Right margin
     * @return X coordinate for right-aligned text
     * @throws IOException if font width calculation fails
     */
    public static float calculateRightAlignedX(float pageWidth, String text, PDFont font, float fontSize, float rightMargin) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        return pageWidth - rightMargin - textWidth;
    }

    /**
     * Draw a horizontal divider line.
     *
     * @param contentStream PDF content stream
     * @param x Starting X position
     * @param y Y position
     * @param width Width of the line
     * @param color Color of the line
     * @param thickness Line thickness
     * @throws IOException if drawing fails
     */
    public static void drawHorizontalDivider(PDPageContentStream contentStream, float x, float y, float width, Color color, float thickness) throws IOException {
        float[] pdfColor = PdfColorPalette.toPdfColor(color);
        contentStream.setStrokingColor(pdfColor[0], pdfColor[1], pdfColor[2]);
        contentStream.setLineWidth(thickness);
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + width, y);
        contentStream.stroke();
        contentStream.setStrokingColor(0f, 0f, 0f); // Reset to black
        contentStream.setLineWidth(1f); // Reset to default
    }

    /**
     * Draw a section divider with default styling.
     *
     * @param contentStream PDF content stream
     * @param x Starting X position
     * @param y Y position
     * @param width Width of the divider
     * @throws IOException if drawing fails
     */
    public static void drawSectionDivider(PDPageContentStream contentStream, float x, float y, float width) throws IOException {
        drawHorizontalDivider(contentStream, x, y, width, PdfColorPalette.MEDIUM_GRAY, 1f);
    }

    /**
     * Draw a colored rectangle (useful for backgrounds, headers, etc.).
     *
     * @param contentStream PDF content stream
     * @param x Starting X position
     * @param y Starting Y position
     * @param width Rectangle width
     * @param height Rectangle height
     * @param color Fill color
     * @throws IOException if drawing fails
     */
    public static void drawColoredRectangle(PDPageContentStream contentStream, float x, float y, float width, float height, Color color) throws IOException {
        float[] pdfColor = PdfColorPalette.toPdfColor(color);
        contentStream.setNonStrokingColor(pdfColor[0], pdfColor[1], pdfColor[2]);
        contentStream.addRect(x, y, width, height);
        contentStream.fill();
        contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset to black
    }

    /**
     * Draw a rounded rectangle.
     *
     * @param contentStream PDF content stream
     * @param x Starting X position
     * @param y Starting Y position
     * @param width Rectangle width
     * @param height Rectangle height
     * @param radius Corner radius
     * @param fillColor Fill color (null for no fill)
     * @param strokeColor Stroke color (null for no stroke)
     * @throws IOException if drawing fails
     */
    public static void drawRoundedRectangle(PDPageContentStream contentStream, float x, float y, float width, float height, float radius, Color fillColor, Color strokeColor) throws IOException {
        // Simplified rounded rectangle using Bezier curves
        float kappa = 0.5522847498f; // Control point offset for circle approximation
        float radiusOffset = radius * kappa;

        // Draw path
        contentStream.moveTo(x + radius, y);
        contentStream.lineTo(x + width - radius, y);
        contentStream.curveTo(x + width - radius + radiusOffset, y, x + width, y + radius - radiusOffset, x + width, y + radius);
        contentStream.lineTo(x + width, y + height - radius);
        contentStream.curveTo(x + width, y + height - radius + radiusOffset, x + width - radius + radiusOffset, y + height, x + width - radius, y + height);
        contentStream.lineTo(x + radius, y + height);
        contentStream.curveTo(x + radius - radiusOffset, y + height, x, y + height - radius + radiusOffset, x, y + height - radius);
        contentStream.lineTo(x, y + radius);
        contentStream.curveTo(x, y + radius - radiusOffset, x + radius - radiusOffset, y, x + radius, y);

        if (fillColor != null) {
            float[] pdfColor = PdfColorPalette.toPdfColor(fillColor);
            contentStream.setNonStrokingColor(pdfColor[0], pdfColor[1], pdfColor[2]);
            if (strokeColor != null) {
                contentStream.fillAndStroke();
            } else {
                contentStream.fill();
            }
            contentStream.setNonStrokingColor(0f, 0f, 0f);
        } else if (strokeColor != null) {
            float[] pdfColor = PdfColorPalette.toPdfColor(strokeColor);
            contentStream.setStrokingColor(pdfColor[0], pdfColor[1], pdfColor[2]);
            contentStream.stroke();
            contentStream.setStrokingColor(0f, 0f, 0f);
        }
    }

    /**
     * Calculate the width of text for a given font and size.
     *
     * @param text Text to measure
     * @param font Font to use
     * @param fontSize Font size
     * @return Width of the text in points
     * @throws IOException if font width calculation fails
     */
    public static float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
        return font.getStringWidth(text) / 1000f * fontSize;
    }

    /**
     * Get the starting Y position for content on a page.
     *
     * @param page PDF page
     * @return Y position accounting for top margin
     */
    public static float getContentStartY(PDPage page) {
        return page.getMediaBox().getHeight() - PAGE_MARGIN;
    }

    /**
     * Get the usable width of a page (accounting for margins).
     *
     * @param page PDF page
     * @return Usable width
     */
    public static float getUsableWidth(PDPage page) {
        return page.getMediaBox().getWidth() - (PAGE_MARGIN * 2);
    }
}
