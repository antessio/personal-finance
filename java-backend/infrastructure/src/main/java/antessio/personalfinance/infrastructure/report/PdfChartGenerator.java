package antessio.personalfinance.infrastructure.report;

import antessio.personalfinance.domain.dto.CategorySpendingDTO;
import antessio.personalfinance.domain.dto.MonthlyDataDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates charts using JFreeChart and embeds them in PDF reports.
 * Supports pie charts, bar charts, and line charts with professional styling.
 */
public class PdfChartGenerator {

    private static final int DEFAULT_CHART_WIDTH = 500;
    private static final int DEFAULT_CHART_HEIGHT = 350;
    private static final int PIE_CHART_WIDTH = 400;
    private static final int PIE_CHART_HEIGHT = 300;
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 11);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yy");

    /**
     * Create a pie chart showing category spending breakdown.
     *
     * @param categories List of category spending data
     * @param title Chart title
     * @param maxCategories Maximum number of categories to show (rest go into "Other")
     * @return BufferedImage of the chart
     */
    public static BufferedImage createCategoryPieChart(List<CategorySpendingDTO> categories, String title, int maxCategories) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        // Sort by total spent (descending)
        List<CategorySpendingDTO> sorted = categories.stream()
            .sorted((a, b) -> b.totalSpent().compareTo(a.totalSpent()))
            .toList();

        // Add top N categories
        BigDecimal otherTotal = BigDecimal.ZERO;
        for (int i = 0; i < sorted.size(); i++) {
            CategorySpendingDTO cat = sorted.get(i);
            if (i < maxCategories) {
                dataset.setValue(cat.category().getName(), cat.totalSpent().doubleValue());
            } else {
                otherTotal = otherTotal.add(cat.totalSpent());
            }
        }

        // Add "Other" if needed
        if (otherTotal.compareTo(BigDecimal.ZERO) > 0) {
            dataset.setValue("Other", otherTotal.doubleValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
            title,
            dataset,
            false,  // legend
            true,   // tooltips
            false   // URLs
        );

        stylePieChart(chart);
        return chart.createBufferedImage(PIE_CHART_WIDTH, PIE_CHART_HEIGHT);
    }

    /**
     * Create a horizontal bar chart comparing budget vs actual spending.
     *
     * @param categories List of category spending data
     * @param title Chart title
     * @param maxCategories Maximum number of categories to show
     * @return BufferedImage of the chart
     */
    public static BufferedImage createBudgetComparisonBarChart(List<CategorySpendingDTO> categories, String title, int maxCategories) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Sort by budget amount (descending) and take top N
        List<CategorySpendingDTO> sorted = categories.stream()
            .filter(c -> c.budgetAmount() != null && c.budgetAmount().compareTo(BigDecimal.ZERO) > 0)
            .sorted((a, b) -> b.budgetAmount().compareTo(a.budgetAmount()))
            .limit(maxCategories)
            .toList();

        for (CategorySpendingDTO cat : sorted) {
            String categoryName = cat.category().getName();
            dataset.addValue(cat.budgetAmount().doubleValue(), "Budget", categoryName);
            dataset.addValue(cat.totalSpent().doubleValue(), "Actual", categoryName);
        }

        JFreeChart chart = ChartFactory.createBarChart(
            title,
            "Amount (€)",
            "Category",
            dataset,
            PlotOrientation.HORIZONTAL,
            true,  // legend
            true,  // tooltips
            false  // URLs
        );

        styleBarChart(chart);
        return chart.createBufferedImage(DEFAULT_CHART_WIDTH, DEFAULT_CHART_HEIGHT);
    }

    /**
     * Create a line chart showing monthly trends for income, expenses, and savings.
     *
     * @param monthlyData List of monthly data
     * @param title Chart title
     * @return BufferedImage of the chart
     */
    public static BufferedImage createMonthlyTrendsLineChart(List<MonthlyDataDTO> monthlyData, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (MonthlyDataDTO data : monthlyData) {
            String month = data.yearMonth().format(MONTH_FORMATTER);

            if (data.totalIncome() != null) {
                dataset.addValue(data.totalIncome().doubleValue(), "Income", month);
            }
            if (data.totalExpenses() != null) {
                dataset.addValue(data.totalExpenses().abs().doubleValue(), "Expenses", month);
            }
            if (data.totalSavings() != null) {
                dataset.addValue(data.totalSavings().doubleValue(), "Savings", month);
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
            title,
            "Month",
            "Amount (€)",
            dataset,
            PlotOrientation.VERTICAL,
            true,  // legend
            true,  // tooltips
            false  // URLs
        );

        styleLineChart(chart);
        return chart.createBufferedImage(DEFAULT_CHART_WIDTH, DEFAULT_CHART_HEIGHT);
    }

    /**
     * Apply professional styling to pie chart.
     */
    private static void stylePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.getTitle().setFont(TITLE_FONT);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(LABEL_FONT);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setShadowPaint(null);

        // Apply color palette
        for (int i = 0; i < PdfColorPalette.CHART_COLORS.length; i++) {
            plot.setSectionPaint(i, PdfColorPalette.CHART_COLORS[i]);
        }

        // Format labels to show percentages
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
            "{0}: {2}", new DecimalFormat("#,##0"), new DecimalFormat("0.0%")));
    }

    /**
     * Apply professional styling to bar chart.
     */
    private static void styleBarChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.getTitle().setFont(TITLE_FONT);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(PdfColorPalette.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(false);

        // Style renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PdfColorPalette.PRIMARY_DARK);   // Budget
        renderer.setSeriesPaint(1, PdfColorPalette.PRIMARY_TEAL);   // Actual
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.1);

        // Format axis labels
        plot.getRangeAxis().setLabelFont(LABEL_FONT);
        plot.getDomainAxis().setLabelFont(LABEL_FONT);
        plot.getRangeAxis().setTickLabelFont(LABEL_FONT);
        plot.getDomainAxis().setTickLabelFont(LABEL_FONT);
    }

    /**
     * Apply professional styling to line chart.
     */
    private static void styleLineChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.getTitle().setFont(TITLE_FONT);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(PdfColorPalette.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(false);

        // Style renderer
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PdfColorPalette.INCOME_PRIMARY);     // Income
        renderer.setSeriesPaint(1, PdfColorPalette.EXPENSE_PRIMARY);    // Expenses
        renderer.setSeriesPaint(2, PdfColorPalette.SAVINGS_PRIMARY);    // Savings
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        renderer.setSeriesStroke(2, new BasicStroke(2.5f));
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);

        // Format axis labels
        plot.getRangeAxis().setLabelFont(LABEL_FONT);
        plot.getDomainAxis().setLabelFont(LABEL_FONT);
        plot.getRangeAxis().setTickLabelFont(LABEL_FONT);
        plot.getDomainAxis().setTickLabelFont(LABEL_FONT);
    }

    /**
     * Embed a chart image into a PDF at specified position.
     *
     * @param contentStream PDF content stream
     * @param document PDF document
     * @param chartImage Chart image to embed
     * @param x X position
     * @param y Y position (top-left of image)
     * @param width Display width
     * @param height Display height
     * @return Final Y position after image
     * @throws IOException if embedding fails
     */
    public static float embedChartInPdf(PDPageContentStream contentStream, PDDocument document,
                                       BufferedImage chartImage, float x, float y, float width, float height) throws IOException {
        // Convert BufferedImage to PDImageXObject
        PDImageXObject pdImage = LosslessFactory.createFromImage(document, chartImage);

        // Draw image at specified position (y is top-left)
        contentStream.drawImage(pdImage, x, y - height, width, height);

        return y - height;
    }
}
