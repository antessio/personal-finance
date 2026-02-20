package antessio.personalfinance.infrastructure.report.components;

import antessio.personalfinance.infrastructure.report.PdfColorPalette;
import antessio.personalfinance.infrastructure.report.PdfLayoutHelper;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Enhanced table renderer with alternating row colors, alignment, and color-coded values.
 */
public class TableRenderer {

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    private final float cellPadding;
    private final float rowHeight;
    private final PDFont headerFont;
    private final PDFont bodyFont;
    private final float headerFontSize;
    private final float bodyFontSize;
    private final boolean zebraStriping;
    private final boolean colorCodeNumbers;

    public TableRenderer() {
        this(5f, 20f, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA,
             PdfLayoutHelper.SMALL_FONT_SIZE, PdfLayoutHelper.SMALL_FONT_SIZE, true, true);
    }

    public TableRenderer(float cellPadding, float rowHeight, PDFont headerFont, PDFont bodyFont,
                        float headerFontSize, float bodyFontSize, boolean zebraStriping, boolean colorCodeNumbers) {
        this.cellPadding = cellPadding;
        this.rowHeight = rowHeight;
        this.headerFont = headerFont;
        this.bodyFont = bodyFont;
        this.headerFontSize = headerFontSize;
        this.bodyFontSize = bodyFontSize;
        this.zebraStriping = zebraStriping;
        this.colorCodeNumbers = colorCodeNumbers;
    }

    /**
     * Draw a table with headers and data.
     *
     * @param contentStream PDF content stream
     * @param headers Table headers
     * @param data Table data (2D array)
     * @param x Starting X position
     * @param y Starting Y position
     * @param columnWidths Array of column widths
     * @param alignments Array of column alignments (optional, defaults to LEFT)
     * @return Final Y position after table
     * @throws IOException if drawing fails
     */
    public float drawTable(PDPageContentStream contentStream, String[] headers, String[][] data,
                          float x, float y, float[] columnWidths, Alignment[] alignments) throws IOException {

        if (alignments == null) {
            alignments = new Alignment[headers.length];
            for (int i = 0; i < alignments.length; i++) {
                alignments[i] = Alignment.LEFT;
            }
        }

        float tableWidth = 0;
        for (float width : columnWidths) {
            tableWidth += width;
        }

        float yPosition = y;

        // Draw header row
        yPosition = drawHeaderRow(contentStream, headers, x, yPosition, columnWidths, tableWidth, alignments);

        // Draw data rows
        for (int i = 0; i < data.length; i++) {
            yPosition = drawDataRow(contentStream, data[i], x, yPosition, columnWidths, alignments, i);
        }

        // Draw final bottom border
        contentStream.setStrokingColor(0f, 0f, 0f);
        contentStream.setLineWidth(1f);
        contentStream.moveTo(x, yPosition);
        contentStream.lineTo(x + tableWidth, yPosition);
        contentStream.stroke();

        return yPosition;
    }

    private float drawHeaderRow(PDPageContentStream contentStream, String[] headers, float x, float y,
                               float[] columnWidths, float tableWidth, Alignment[] alignments) throws IOException {
        float yPosition = y;

        // Draw header background
        PdfLayoutHelper.drawColoredRectangle(contentStream, x, yPosition - rowHeight + 5, tableWidth, rowHeight, PdfColorPalette.PRIMARY_DARK);

        // Draw header text in white
        contentStream.setNonStrokingColor(1f, 1f, 1f);
        contentStream.setFont(headerFont, headerFontSize);

        float xPosition = x;
        for (int i = 0; i < headers.length && i < columnWidths.length; i++) {
            String text = headers[i] != null ? headers[i] : "";
            float textX = calculateTextX(xPosition, columnWidths[i], text, headerFont, headerFontSize, alignments[i]);

            contentStream.beginText();
            contentStream.newLineAtOffset(textX, yPosition - rowHeight + 7);
            contentStream.showText(text);
            contentStream.endText();

            xPosition += columnWidths[i];
        }

        contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset to black
        yPosition -= rowHeight;

        // Draw header border
        contentStream.setStrokingColor(0f, 0f, 0f);
        contentStream.setLineWidth(1.5f);
        contentStream.moveTo(x, yPosition);
        contentStream.lineTo(x + tableWidth, yPosition);
        contentStream.stroke();

        return yPosition;
    }

    private float drawDataRow(PDPageContentStream contentStream, String[] rowData, float x, float y,
                             float[] columnWidths, Alignment[] alignments, int rowIndex) throws IOException {
        float yPosition = y;

        // Draw alternating row background
        if (zebraStriping && rowIndex % 2 == 1) {
            float tableWidth = 0;
            for (float width : columnWidths) {
                tableWidth += width;
            }
            PdfLayoutHelper.drawColoredRectangle(contentStream, x, yPosition - rowHeight + 5, tableWidth, rowHeight, PdfColorPalette.LIGHT_GRAY);
        }

        // Draw row text
        contentStream.setFont(bodyFont, bodyFontSize);

        float xPosition = x;
        for (int i = 0; i < rowData.length && i < columnWidths.length; i++) {
            String text = rowData[i] != null ? rowData[i] : "";

            // Truncate if too long
            text = truncateText(text, columnWidths[i], bodyFont, bodyFontSize);

            // Color code if it's a currency value
            if (colorCodeNumbers && isCurrencyValue(text)) {
                setCurrencyColor(contentStream, text);
            }

            float textX = calculateTextX(xPosition, columnWidths[i], text, bodyFont, bodyFontSize, alignments[i]);

            contentStream.beginText();
            contentStream.newLineAtOffset(textX, yPosition - rowHeight + 7);
            contentStream.showText(text);
            contentStream.endText();

            contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset to black

            xPosition += columnWidths[i];
        }

        yPosition -= rowHeight;

        // Draw row border (light)
        contentStream.setStrokingColor(0.8f, 0.8f, 0.8f);
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(x, yPosition);
        contentStream.lineTo(x + xPosition - x, yPosition);
        contentStream.stroke();

        return yPosition;
    }

    private float calculateTextX(float columnX, float columnWidth, String text, PDFont font, float fontSize, Alignment alignment) throws IOException {
        float textWidth = PdfLayoutHelper.getTextWidth(text, font, fontSize);

        return switch (alignment) {
            case CENTER -> columnX + (columnWidth - textWidth) / 2;
            case RIGHT -> columnX + columnWidth - textWidth - cellPadding;
            default -> columnX + cellPadding; // LEFT
        };
    }

    private String truncateText(String text, float columnWidth, PDFont font, float fontSize) throws IOException {
        if (text == null || text.isEmpty()) {
            return "";
        }

        float availableWidth = columnWidth - (cellPadding * 2);
        float textWidth = PdfLayoutHelper.getTextWidth(text, font, fontSize);

        if (textWidth <= availableWidth) {
            return text;
        }

        // Truncate and add ellipsis
        String ellipsis = "...";
        float ellipsisWidth = PdfLayoutHelper.getTextWidth(ellipsis, font, fontSize);
        float targetWidth = availableWidth - ellipsisWidth;

        int estimatedChars = (int) ((targetWidth / textWidth) * text.length());
        String truncated = text.substring(0, Math.max(1, Math.min(estimatedChars, text.length() - 1)));

        while (PdfLayoutHelper.getTextWidth(truncated + ellipsis, font, fontSize) > availableWidth && truncated.length() > 1) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }

        return truncated + ellipsis;
    }

    private boolean isCurrencyValue(String text) {
        return text.matches("^[+\\-]?€.*") || text.matches("^[+\\-]?\\d+.*");
    }

    private void setCurrencyColor(PDPageContentStream contentStream, String text) throws IOException {
        Color color;
        if (text.startsWith("-")) {
            color = PdfColorPalette.DANGER_RED;
        } else if (text.startsWith("+")) {
            color = PdfColorPalette.SUCCESS_GREEN;
        } else if (text.contains("€") && !text.equals("€ 0,00") && !text.equals("€0.00")) {
            // Positive currency value (no explicit sign)
            color = PdfColorPalette.SUCCESS_GREEN;
        } else {
            return; // Keep default black
        }

        float[] pdfColor = PdfColorPalette.toPdfColor(color);
        contentStream.setNonStrokingColor(pdfColor[0], pdfColor[1], pdfColor[2]);
    }
}
