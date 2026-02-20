package antessio.personalfinance.infrastructure.report.pages;

import antessio.personalfinance.domain.dto.MonthlyDataDTO;
import antessio.personalfinance.domain.dto.ReportDataDTO;
import antessio.personalfinance.infrastructure.report.CurrencyFormatter;
import antessio.personalfinance.infrastructure.report.PdfChartGenerator;
import antessio.personalfinance.infrastructure.report.PdfLayoutHelper;
import antessio.personalfinance.infrastructure.report.components.FooterRenderer;
import antessio.personalfinance.infrastructure.report.components.TableRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Renders monthly trends page with line chart and summary statistics.
 * Only included in annual reports (not monthly reports).
 */
public class MonthlyTrendsPageRenderer {

    private final FooterRenderer footerRenderer;
    private final TableRenderer tableRenderer;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    public MonthlyTrendsPageRenderer(String reportTitle) {
        this.footerRenderer = new FooterRenderer(reportTitle, LocalDate.now());
        this.tableRenderer = new TableRenderer();
    }

    /**
     * Render the monthly trends page.
     *
     * @param document PDF document
     * @param data Report data
     * @param pageNumber Current page number
     * @param totalPages Total number of pages
     * @throws IOException if rendering fails
     */
    public void render(PDDocument document, ReportDataDTO data, int pageNumber, int totalPages) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float yPosition = PdfLayoutHelper.getContentStartY(page);

            // Page title
            yPosition = drawPageTitle(contentStream, "Monthly Trends", yPosition);

            if (data.monthlyTrends().isEmpty()) {
                // No data available
                contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.NORMAL_FONT_SIZE);
                contentStream.beginText();
                contentStream.newLineAtOffset(PdfLayoutHelper.PAGE_MARGIN, yPosition);
                contentStream.showText("No data available for the selected period.");
                contentStream.endText();

                footerRenderer.drawFooter(contentStream, page, pageNumber, totalPages);
                return;
            }

            // Line chart
            yPosition = drawTrendsChart(contentStream, document, data.monthlyTrends(), pageWidth, yPosition);
            yPosition -= PdfLayoutHelper.SECTION_SPACING;

            // Summary statistics table
            yPosition = drawSummaryStatistics(contentStream, data.monthlyTrends(), yPosition);

            // Footer
            footerRenderer.drawFooter(contentStream, page, pageNumber, totalPages);
        }
    }

    /**
     * Draw page title.
     */
    private float drawPageTitle(PDPageContentStream contentStream, String title, float yPosition) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, PdfLayoutHelper.HEADING_FONT_SIZE);
        contentStream.setNonStrokingColor(0.2f, 0.2f, 0.2f);

        contentStream.beginText();
        contentStream.newLineAtOffset(PdfLayoutHelper.PAGE_MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();

        contentStream.setNonStrokingColor(0f, 0f, 0f);
        return yPosition - 35;
    }

    /**
     * Draw line chart showing income, expenses, and savings trends.
     */
    private float drawTrendsChart(PDPageContentStream contentStream, PDDocument document,
                                 List<MonthlyDataDTO> monthlyData, float pageWidth, float yPosition) throws IOException {
        // Generate chart
        BufferedImage chart = PdfChartGenerator.createMonthlyTrendsLineChart(
            monthlyData,
            "Income, Expenses, and Savings Trends"
        );

        // Embed chart in PDF (centered)
        float chartWidth = 450;
        float chartHeight = 280;
        float chartX = (pageWidth - chartWidth) / 2;

        PdfChartGenerator.embedChartInPdf(
            contentStream,
            document,
            chart,
            chartX,
            yPosition,
            chartWidth,
            chartHeight
        );

        return yPosition - chartHeight;
    }

    /**
     * Draw summary statistics table.
     */
    private float drawSummaryStatistics(PDPageContentStream contentStream, List<MonthlyDataDTO> monthlyData,
                                       float yPosition) throws IOException {
        // Section heading
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, PdfLayoutHelper.SUBHEADING_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(PdfLayoutHelper.PAGE_MARGIN, yPosition);
        contentStream.showText("Monthly Summary");
        contentStream.endText();

        yPosition -= 25;

        // Prepare table data
        String[] headers = {"Month", "Income", "Expenses", "Savings", "Net Balance"};
        String[][] tableData = new String[monthlyData.size()][5];

        for (int i = 0; i < monthlyData.size(); i++) {
            MonthlyDataDTO monthly = monthlyData.get(i);

            BigDecimal income = monthly.totalIncome() != null ? monthly.totalIncome() : BigDecimal.ZERO;
            BigDecimal expenses = monthly.totalExpenses() != null ? monthly.totalExpenses() : BigDecimal.ZERO;
            BigDecimal savings = monthly.totalSavings() != null ? monthly.totalSavings() : BigDecimal.ZERO;
            BigDecimal netBalance = income.subtract(expenses).subtract(savings);

            tableData[i] = new String[]{
                monthly.yearMonth().format(MONTH_FORMATTER),
                CurrencyFormatter.formatCurrency(income),
                CurrencyFormatter.formatCurrency(expenses),
                CurrencyFormatter.formatCurrency(savings),
                CurrencyFormatter.formatCurrencyWithSign(netBalance)
            };
        }

        float[] columnWidths = {90f, 100f, 100f, 100f, 105f};
        TableRenderer.Alignment[] alignments = {
            TableRenderer.Alignment.LEFT,
            TableRenderer.Alignment.RIGHT,
            TableRenderer.Alignment.RIGHT,
            TableRenderer.Alignment.RIGHT,
            TableRenderer.Alignment.RIGHT
        };

        return tableRenderer.drawTable(contentStream, headers, tableData,
                                      PdfLayoutHelper.PAGE_MARGIN, yPosition, columnWidths, alignments);
    }
}
