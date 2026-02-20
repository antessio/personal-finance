package antessio.personalfinance.infrastructure.report.pages;

import antessio.personalfinance.domain.dto.ReportDataDTO;
import antessio.personalfinance.infrastructure.report.CurrencyFormatter;
import antessio.personalfinance.infrastructure.report.PdfColorPalette;
import antessio.personalfinance.infrastructure.report.PdfLayoutHelper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Renders an enhanced cover page with professional design, key metrics, and branding.
 */
public class CoverPageRenderer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final float DECORATIVE_BAND_HEIGHT = 120f;
    private static final float METRICS_BOX_SIZE = 200f;
    private static final float METRICS_SPACING = 20f;

    /**
     * Render the cover page.
     *
     * @param document PDF document
     * @param data Report data
     * @throws IOException if rendering fails
     */
    public void render(PDDocument document, ReportDataDTO data) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // Draw decorative header band
            drawDecorativeBand(contentStream, pageWidth, pageHeight);

            // Draw title and subtitle
            float yPosition = drawTitle(contentStream, data, pageWidth, pageHeight);

            // Draw generated date and user info
            yPosition = drawMetadata(contentStream, data, pageWidth, yPosition);

            // Draw key metrics grid
            drawKeyMetrics(contentStream, data, pageWidth, yPosition);

            // Draw footer branding
            drawFooterBranding(contentStream, pageWidth);
        }
    }

    /**
     * Draw decorative header band with gradient effect.
     */
    private void drawDecorativeBand(PDPageContentStream contentStream, float pageWidth, float pageHeight) throws IOException {
        // Main band
        PdfLayoutHelper.drawColoredRectangle(
            contentStream,
            0,
            pageHeight - DECORATIVE_BAND_HEIGHT,
            pageWidth,
            DECORATIVE_BAND_HEIGHT,
            PdfColorPalette.PRIMARY_DARK
        );

        // Accent stripe
        PdfLayoutHelper.drawColoredRectangle(
            contentStream,
            0,
            pageHeight - DECORATIVE_BAND_HEIGHT,
            pageWidth,
            10,
            PdfColorPalette.PRIMARY_TEAL
        );
    }

    /**
     * Draw main title and subtitle.
     */
    private float drawTitle(PDPageContentStream contentStream, ReportDataDTO data, float pageWidth, float pageHeight) throws IOException {
        float yPosition = pageHeight - 200;

        // Main title
        String title = data.reportTitle();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, PdfLayoutHelper.TITLE_FONT_SIZE);
        float titleX = PdfLayoutHelper.calculateCenteredX(pageWidth, title, PDType1Font.HELVETICA_BOLD, PdfLayoutHelper.TITLE_FONT_SIZE);

        contentStream.setNonStrokingColor(0.2f, 0.2f, 0.2f);
        contentStream.beginText();
        contentStream.newLineAtOffset(titleX, yPosition);
        contentStream.showText(title);
        contentStream.endText();

        yPosition -= 40;

        // Subtitle (period)
        String subtitle = buildSubtitle(data);
        contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.SUBTITLE_FONT_SIZE);
        float subtitleX = PdfLayoutHelper.calculateCenteredX(pageWidth, subtitle, PDType1Font.HELVETICA, PdfLayoutHelper.SUBTITLE_FONT_SIZE);

        contentStream.setNonStrokingColor(0.4f, 0.4f, 0.4f);
        contentStream.beginText();
        contentStream.newLineAtOffset(subtitleX, yPosition);
        contentStream.showText(subtitle);
        contentStream.endText();

        contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset
        return yPosition - 50;
    }

    /**
     * Draw metadata (generated date and username).
     */
    private float drawMetadata(PDPageContentStream contentStream, ReportDataDTO data, float pageWidth, float yPosition) throws IOException {
        // Generated date box
        String dateText = "Generated: " + LocalDate.now().format(DATE_FORMATTER);
        drawMetadataBox(contentStream, dateText, pageWidth / 2 - 150, yPosition, 300);

        yPosition -= 50;

        // Username box
        String userText = "Prepared for: " + data.username();
        drawMetadataBox(contentStream, userText, pageWidth / 2 - 150, yPosition, 300);

        return yPosition - 60;
    }

    /**
     * Draw a styled metadata box.
     */
    private void drawMetadataBox(PDPageContentStream contentStream, String text, float x, float y, float width) throws IOException {
        float height = 30;

        // Draw box background
        PdfLayoutHelper.drawRoundedRectangle(
            contentStream,
            x,
            y - height,
            width,
            height,
            5f,
            PdfColorPalette.LIGHT_GRAY,
            null
        );

        // Draw text
        contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.NORMAL_FONT_SIZE);
        float textX = PdfLayoutHelper.calculateCenteredX(width + x * 2, text, PDType1Font.HELVETICA, PdfLayoutHelper.NORMAL_FONT_SIZE);

        contentStream.beginText();
        contentStream.newLineAtOffset(textX, y - height + 10);
        contentStream.showText(text);
        contentStream.endText();
    }

    /**
     * Draw 2x2 grid of key metrics.
     */
    private void drawKeyMetrics(PDPageContentStream contentStream, ReportDataDTO data, float pageWidth, float yPosition) throws IOException {
        float gridStartX = (pageWidth - (METRICS_BOX_SIZE * 2 + METRICS_SPACING)) / 2;

        // Calculate metrics
        BigDecimal income = data.totalIncome() != null ? data.totalIncome() : BigDecimal.ZERO;
        BigDecimal expenses = data.totalExpenses() != null ? data.totalExpenses() : BigDecimal.ZERO;
        BigDecimal savings = data.totalSavings() != null ? data.totalSavings() : BigDecimal.ZERO;
        BigDecimal netBalance = income.subtract(expenses).subtract(savings);

        // Row 1
        drawMetricBox(contentStream, "Total Income", income, PdfColorPalette.INCOME_PRIMARY,
                     gridStartX, yPosition);
        drawMetricBox(contentStream, "Total Expenses", expenses, PdfColorPalette.EXPENSE_PRIMARY,
                     gridStartX + METRICS_BOX_SIZE + METRICS_SPACING, yPosition);

        yPosition -= METRICS_BOX_SIZE + METRICS_SPACING;

        // Row 2
        drawMetricBox(contentStream, "Total Savings", savings, PdfColorPalette.SAVINGS_PRIMARY,
                     gridStartX, yPosition);

        // Net balance with color coding
        Color balanceColor = netBalance.compareTo(BigDecimal.ZERO) >= 0
                           ? PdfColorPalette.SUCCESS_GREEN
                           : PdfColorPalette.DANGER_RED;
        drawMetricBox(contentStream, "Net Balance", netBalance, balanceColor,
                     gridStartX + METRICS_BOX_SIZE + METRICS_SPACING, yPosition);
    }

    /**
     * Draw a single metric box.
     */
    private void drawMetricBox(PDPageContentStream contentStream, String label, BigDecimal value,
                              Color color, float x, float y) throws IOException {
        float boxHeight = METRICS_BOX_SIZE;

        // Draw box with colored border
        PdfLayoutHelper.drawRoundedRectangle(
            contentStream,
            x,
            y - boxHeight,
            METRICS_BOX_SIZE,
            boxHeight,
            8f,
            Color.WHITE,
            color
        );

        contentStream.setLineWidth(3f);
        float[] colorRgb = PdfColorPalette.toPdfColor(color);
        contentStream.setStrokingColor(colorRgb[0], colorRgb[1], colorRgb[2]);

        // Draw label
        contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.NORMAL_FONT_SIZE);
        float labelX = PdfLayoutHelper.calculateCenteredX(METRICS_BOX_SIZE + x * 2, label,
                                                         PDType1Font.HELVETICA, PdfLayoutHelper.NORMAL_FONT_SIZE);
        contentStream.setNonStrokingColor(0.4f, 0.4f, 0.4f);
        contentStream.beginText();
        contentStream.newLineAtOffset(labelX, y - 50);
        contentStream.showText(label);
        contentStream.endText();

        // Draw value
        String valueText = CurrencyFormatter.formatCurrency(value);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22);
        float valueX = PdfLayoutHelper.calculateCenteredX(METRICS_BOX_SIZE + x * 2, valueText,
                                                         PDType1Font.HELVETICA_BOLD, 22);
        contentStream.setNonStrokingColor(colorRgb[0], colorRgb[1], colorRgb[2]);
        contentStream.beginText();
        contentStream.newLineAtOffset(valueX, y - 110);
        contentStream.showText(valueText);
        contentStream.endText();

        contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset
        contentStream.setStrokingColor(0f, 0f, 0f);
        contentStream.setLineWidth(1f);
    }

    /**
     * Draw footer branding.
     */
    private void drawFooterBranding(PDPageContentStream contentStream, float pageWidth) throws IOException {
        String branding = "Personal Finance Management System";
        contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.SMALL_FONT_SIZE);
        contentStream.setNonStrokingColor(0.6f, 0.6f, 0.6f);

        float brandingX = PdfLayoutHelper.calculateCenteredX(pageWidth, branding,
                                                            PDType1Font.HELVETICA, PdfLayoutHelper.SMALL_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(brandingX, 40);
        contentStream.showText(branding);
        contentStream.endText();

        contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset
    }

    /**
     * Build subtitle text based on report period.
     */
    private String buildSubtitle(ReportDataDTO data) {
        if (data.month() != null) {
            return String.format("%s %d", getMonthName(data.month()), data.year());
        } else {
            return String.format("Year %d", data.year());
        }
    }

    /**
     * Get month name from month number.
     */
    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }
}
