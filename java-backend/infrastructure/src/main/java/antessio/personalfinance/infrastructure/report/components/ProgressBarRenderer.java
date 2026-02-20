package antessio.personalfinance.infrastructure.report.components;

import antessio.personalfinance.infrastructure.report.CurrencyFormatter;
import antessio.personalfinance.infrastructure.report.PdfColorPalette;
import antessio.personalfinance.infrastructure.report.PdfLayoutHelper;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Renders visual progress bars for budget utilization.
 * Color-coded based on percentage: green (<80%), amber (80-100%), red (>100%).
 */
public class ProgressBarRenderer {

    private static final float DEFAULT_BAR_HEIGHT = 12f;
    private static final float DEFAULT_BAR_WIDTH = 100f;
    private static final float CORNER_RADIUS = 3f;

    /**
     * Draw a progress bar showing budget utilization.
     *
     * @param contentStream PDF content stream
     * @param x Starting X position
     * @param y Starting Y position (top of bar)
     * @param width Width of the progress bar
     * @param percentage Percentage value (0-100+)
     * @param showLabel Whether to show percentage label
     * @return Final Y position after bar
     * @throws IOException if drawing fails
     */
    public static float drawProgressBar(PDPageContentStream contentStream, float x, float y, float width,
                                       double percentage, boolean showLabel) throws IOException {
        return drawProgressBar(contentStream, x, y, width, DEFAULT_BAR_HEIGHT, percentage, showLabel);
    }

    /**
     * Draw a progress bar with custom height.
     *
     * @param contentStream PDF content stream
     * @param x Starting X position
     * @param y Starting Y position (top of bar)
     * @param width Width of the progress bar
     * @param height Height of the progress bar
     * @param percentage Percentage value (0-100+)
     * @param showLabel Whether to show percentage label
     * @return Final Y position after bar
     * @throws IOException if drawing fails
     */
    public static float drawProgressBar(PDPageContentStream contentStream, float x, float y, float width,
                                       float height, double percentage, boolean showLabel) throws IOException {
        // Draw background (light gray rounded rectangle)
        PdfLayoutHelper.drawRoundedRectangle(contentStream, x, y - height, width, height,
                                            CORNER_RADIUS, PdfColorPalette.LIGHT_GRAY, null);

        // Calculate fill width (cap at 100% visual width)
        double displayPercentage = Math.min(percentage, 100.0);
        float fillWidth = (float) (width * (displayPercentage / 100.0));

        // Determine color based on percentage
        Color fillColor = PdfColorPalette.getBudgetStatusColor(percentage);

        // Draw filled portion
        if (fillWidth > 0) {
            PdfLayoutHelper.drawRoundedRectangle(contentStream, x, y - height, fillWidth, height,
                                                CORNER_RADIUS, fillColor, null);
        }

        // Draw percentage label if requested
        if (showLabel) {
            String label = CurrencyFormatter.formatPercentage(percentage);
            float labelX = x + width + 5;
            float labelY = y - height + 3; // Vertically center the text

            contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.TINY_FONT_SIZE);

            // Color-code the label
            float[] labelColor = PdfColorPalette.toPdfColor(fillColor);
            contentStream.setNonStrokingColor(labelColor[0], labelColor[1], labelColor[2]);

            contentStream.beginText();
            contentStream.newLineAtOffset(labelX, labelY);
            contentStream.showText(label);
            contentStream.endText();

            contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset to black
        }

        return y - height;
    }

    /**
     * Draw a progress bar from actual and budget amounts.
     *
     * @param contentStream PDF content stream
     * @param x Starting X position
     * @param y Starting Y position
     * @param width Width of the progress bar
     * @param actual Actual spending amount
     * @param budget Budget amount
     * @param showLabel Whether to show percentage label
     * @return Final Y position after bar
     * @throws IOException if drawing fails
     */
    public static float drawProgressBar(PDPageContentStream contentStream, float x, float y, float width,
                                       BigDecimal actual, BigDecimal budget, boolean showLabel) throws IOException {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) == 0) {
            // No budget defined - draw N/A
            return drawNAProgressBar(contentStream, x, y, width);
        }

        BigDecimal actualValue = actual != null ? actual : BigDecimal.ZERO;
        double percentage = actualValue.divide(budget, 4, RoundingMode.HALF_UP)
                                      .multiply(BigDecimal.valueOf(100))
                                      .doubleValue();

        return drawProgressBar(contentStream, x, y, width, percentage, showLabel);
    }

    /**
     * Draw a disabled progress bar for "N/A" cases (no budget).
     *
     * @param contentStream PDF content stream
     * @param x Starting X position
     * @param y Starting Y position
     * @param width Width of the progress bar
     * @return Final Y position after bar
     * @throws IOException if drawing fails
     */
    private static float drawNAProgressBar(PDPageContentStream contentStream, float x, float y, float width) throws IOException {
        // Draw background only
        PdfLayoutHelper.drawRoundedRectangle(contentStream, x, y - DEFAULT_BAR_HEIGHT, width, DEFAULT_BAR_HEIGHT,
                                            CORNER_RADIUS, PdfColorPalette.LIGHT_GRAY, null);

        // Draw "N/A" text
        String label = "N/A";
        float labelX = x + width + 5;
        float labelY = y - DEFAULT_BAR_HEIGHT + 3;

        contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.TINY_FONT_SIZE);
        contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f); // Gray

        contentStream.beginText();
        contentStream.newLineAtOffset(labelX, labelY);
        contentStream.showText(label);
        contentStream.endText();

        contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset to black

        return y - DEFAULT_BAR_HEIGHT;
    }

    /**
     * Get visual indicator symbol for trend or status.
     *
     * @param value Value to evaluate (can be a difference or percentage)
     * @param isIncome Whether this is for income (true) or expense (false)
     * @return Symbol: "▲" (up), "▼" (down), or "●" (neutral)
     */
    public static String getTrendIndicator(BigDecimal value, boolean isIncome) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "●"; // Neutral
        }

        boolean isPositive = value.compareTo(BigDecimal.ZERO) > 0;

        // For income, positive is good (up arrow)
        // For expense, negative is good (down arrow)
        if (isIncome) {
            return isPositive ? "▲" : "▼";
        } else {
            return isPositive ? "▼" : "▲";
        }
    }

    /**
     * Get color for trend indicator based on context.
     *
     * @param value Value to evaluate
     * @param isIncome Whether this is for income (true) or expense (false)
     * @return Color for the indicator
     */
    public static Color getTrendColor(BigDecimal value, boolean isIncome) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return PdfColorPalette.MEDIUM_GRAY;
        }

        boolean isPositive = value.compareTo(BigDecimal.ZERO) > 0;

        // For income, positive is good (green)
        // For expense, negative is good (green)
        if ((isIncome && isPositive) || (!isIncome && !isPositive)) {
            return PdfColorPalette.SUCCESS_GREEN;
        } else {
            return PdfColorPalette.DANGER_RED;
        }
    }
}
