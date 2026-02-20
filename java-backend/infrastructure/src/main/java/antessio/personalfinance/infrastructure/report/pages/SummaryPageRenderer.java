package antessio.personalfinance.infrastructure.report.pages;

import antessio.personalfinance.domain.dto.ReportDataDTO;
import antessio.personalfinance.infrastructure.report.CurrencyFormatter;
import antessio.personalfinance.infrastructure.report.PdfChartGenerator;
import antessio.personalfinance.infrastructure.report.PdfColorPalette;
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
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Renders enhanced summary page with financial overview, charts, and key insights.
 */
public class SummaryPageRenderer {

    private final FooterRenderer footerRenderer;
    private final TableRenderer tableRenderer;

    public SummaryPageRenderer(String reportTitle) {
        this.footerRenderer = new FooterRenderer(reportTitle, LocalDate.now());
        this.tableRenderer = new TableRenderer();
    }

    /**
     * Render the summary page.
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
            yPosition = drawPageTitle(contentStream, "Financial Summary", pageWidth, yPosition);

            // Financial overview table
            yPosition = drawFinancialOverview(contentStream, data, yPosition);
            yPosition -= PdfLayoutHelper.SECTION_SPACING;

            // Charts section
            if (!data.expensesByCategory().isEmpty()) {
                yPosition = drawChartsSection(contentStream, document, data, yPosition, pageWidth);
            }

            yPosition -= PdfLayoutHelper.SECTION_SPACING;

            // Key insights box
            drawKeyInsights(contentStream, data, pageWidth, yPosition);

            // Footer
            footerRenderer.drawFooter(contentStream, page, pageNumber, totalPages);
        }
    }

    /**
     * Draw page title.
     */
    private float drawPageTitle(PDPageContentStream contentStream, String title, float pageWidth, float yPosition) throws IOException {
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
     * Draw financial overview table.
     */
    private float drawFinancialOverview(PDPageContentStream contentStream, ReportDataDTO data, float yPosition) throws IOException {
        String[] headers = {"Category", "Actual", "Budget", "Difference", "% of Budget"};

        String[][] tableData = new String[3][5];

        // Income row
        tableData[0] = buildOverviewRow("Income", data.totalIncome(), data.totalIncomeBudget());

        // Expenses row
        tableData[1] = buildOverviewRow("Expenses", data.totalExpenses(), data.totalExpensesBudget());

        // Savings row
        tableData[2] = buildOverviewRow("Savings", data.totalSavings(), data.totalSavingsBudget());

        float[] columnWidths = {140f, 100f, 100f, 100f, 85f};
        TableRenderer.Alignment[] alignments = {
            TableRenderer.Alignment.LEFT,
            TableRenderer.Alignment.RIGHT,
            TableRenderer.Alignment.RIGHT,
            TableRenderer.Alignment.RIGHT,
            TableRenderer.Alignment.CENTER
        };

        return tableRenderer.drawTable(contentStream, headers, tableData,
                                      PdfLayoutHelper.PAGE_MARGIN, yPosition, columnWidths, alignments);
    }

    /**
     * Build a single row for overview table.
     */
    private String[] buildOverviewRow(String category, BigDecimal actual, BigDecimal budget) {
        String actualStr = CurrencyFormatter.formatCurrency(actual);
        String budgetStr = budget != null ? CurrencyFormatter.formatCurrency(budget) : "N/A";
        String differenceStr = CurrencyFormatter.formatDifference(actual, budget);
        String percentageStr = CurrencyFormatter.formatPercentage(actual, budget);

        return new String[]{category, actualStr, budgetStr, differenceStr, percentageStr};
    }

    /**
     * Draw charts section with pie chart and bar chart.
     */
    private float drawChartsSection(PDPageContentStream contentStream, PDDocument document,
                                   ReportDataDTO data, float yPosition, float pageWidth) throws IOException {
        // Section heading
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, PdfLayoutHelper.SUBHEADING_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(PdfLayoutHelper.PAGE_MARGIN, yPosition);
        contentStream.showText("Spending Analysis");
        contentStream.endText();

        yPosition -= 30;

        // Left: Pie chart (expense breakdown)
        if (!data.expensesByCategory().isEmpty()) {
            BufferedImage pieChart = PdfChartGenerator.createCategoryPieChart(
                data.expensesByCategory(),
                "Expense Distribution",
                7
            );

            float chartWidth = 220;
            float chartHeight = 165;
            PdfChartGenerator.embedChartInPdf(
                contentStream,
                document,
                pieChart,
                PdfLayoutHelper.PAGE_MARGIN,
                yPosition,
                chartWidth,
                chartHeight
            );
        }

        // Right: Bar chart (budget vs actual)
        if (!data.expensesByCategory().isEmpty()) {
            BufferedImage barChart = PdfChartGenerator.createBudgetComparisonBarChart(
                data.expensesByCategory(),
                "Top Categories: Budget vs Actual",
                5
            );

            float chartWidth = 240;
            float chartHeight = 165;
            float chartX = pageWidth - PdfLayoutHelper.PAGE_MARGIN - chartWidth;
            PdfChartGenerator.embedChartInPdf(
                contentStream,
                document,
                barChart,
                chartX,
                yPosition,
                chartWidth,
                chartHeight
            );
        }

        return yPosition - 175;
    }

    /**
     * Draw key insights box.
     */
    private void drawKeyInsights(PDPageContentStream contentStream, ReportDataDTO data,
                                 float pageWidth, float yPosition) throws IOException {
        // Calculate insights
        BigDecimal income = data.totalIncome() != null ? data.totalIncome() : BigDecimal.ZERO;
        BigDecimal expenses = data.totalExpenses() != null ? data.totalExpenses() : BigDecimal.ZERO;
        BigDecimal savings = data.totalSavings() != null ? data.totalSavings() : BigDecimal.ZERO;
        BigDecimal netBalance = income.subtract(expenses).subtract(savings);

        // Calculate savings rate
        String savingsRateText = "N/A";
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savingsRate = savings.divide(income, 4, RoundingMode.HALF_UP)
                                           .multiply(BigDecimal.valueOf(100));
            savingsRateText = CurrencyFormatter.formatPercentage(savingsRate.doubleValue());
        }

        // Budget status
        String budgetStatus = calculateBudgetStatus(data);

        // Box dimensions
        float boxWidth = pageWidth - (PdfLayoutHelper.PAGE_MARGIN * 2);
        float boxHeight = 90;
        float boxY = yPosition;

        // Draw box background
        PdfLayoutHelper.drawRoundedRectangle(
            contentStream,
            PdfLayoutHelper.PAGE_MARGIN,
            boxY - boxHeight,
            boxWidth,
            boxHeight,
            8f,
            PdfColorPalette.LIGHT_GRAY,
            null
        );

        // Heading
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, PdfLayoutHelper.SUBHEADING_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(PdfLayoutHelper.PAGE_MARGIN + 15, boxY - 25);
        contentStream.showText("Key Insights");
        contentStream.endText();

        float textY = boxY - 45;
        float leftX = PdfLayoutHelper.PAGE_MARGIN + 15;
        float rightX = pageWidth / 2 + 20;

        contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.NORMAL_FONT_SIZE);

        // Left column
        drawInsightLine(contentStream, "Net Balance:", CurrencyFormatter.formatCurrency(netBalance),
                       leftX, textY, netBalance.compareTo(BigDecimal.ZERO) >= 0);

        textY -= 18;
        drawInsightLine(contentStream, "Savings Rate:", savingsRateText, leftX, textY, true);

        // Right column
        textY = boxY - 45;
        contentStream.beginText();
        contentStream.newLineAtOffset(rightX, textY);
        contentStream.showText("Budget Status: " + budgetStatus);
        contentStream.endText();
    }

    /**
     * Draw a single insight line with label and value.
     */
    private void drawInsightLine(PDPageContentStream contentStream, String label, String value,
                                 float x, float y, boolean isPositive) throws IOException {
        // Label
        contentStream.setNonStrokingColor(0.3f, 0.3f, 0.3f);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label);
        contentStream.endText();

        // Value (color-coded)
        float[] color = isPositive
                      ? PdfColorPalette.toPdfColor(PdfColorPalette.SUCCESS_GREEN)
                      : PdfColorPalette.toPdfColor(PdfColorPalette.DANGER_RED);
        contentStream.setNonStrokingColor(color[0], color[1], color[2]);

        float labelWidth = PdfLayoutHelper.getTextWidth(label, PDType1Font.HELVETICA, PdfLayoutHelper.NORMAL_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(x + labelWidth + 5, y);
        contentStream.showText(value);
        contentStream.endText();

        contentStream.setNonStrokingColor(0f, 0f, 0f);
    }

    /**
     * Calculate overall budget status summary.
     */
    private String calculateBudgetStatus(ReportDataDTO data) {
        BigDecimal totalActual = BigDecimal.ZERO;
        BigDecimal totalBudget = BigDecimal.ZERO;

        if (data.totalExpenses() != null) {
            totalActual = totalActual.add(data.totalExpenses());
        }
        if (data.totalExpensesBudget() != null) {
            totalBudget = totalBudget.add(data.totalExpensesBudget());
        }

        if (totalBudget.compareTo(BigDecimal.ZERO) == 0) {
            return "No budget defined";
        }

        BigDecimal percentage = totalActual.divide(totalBudget, 4, RoundingMode.HALF_UP)
                                          .multiply(BigDecimal.valueOf(100));

        if (percentage.doubleValue() < 80.0) {
            return "On track (\u2713)";
        } else if (percentage.doubleValue() <= 100.0) {
            return "Near limit (!)";
        } else {
            return "Over budget (X)";
        }
    }
}
