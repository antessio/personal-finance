package antessio.personalfinance.infrastructure.report.pages;

import antessio.personalfinance.domain.dto.CategorySpendingDTO;
import antessio.personalfinance.domain.dto.ReportDataDTO;
import antessio.personalfinance.infrastructure.report.CurrencyFormatter;
import antessio.personalfinance.infrastructure.report.PdfColorPalette;
import antessio.personalfinance.infrastructure.report.PdfLayoutHelper;
import antessio.personalfinance.infrastructure.report.components.FooterRenderer;
import antessio.personalfinance.infrastructure.report.components.ProgressBarRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Renders enhanced category breakdown page with tables, progress bars, and visual indicators.
 * Handles pagination for large category lists.
 */
public class CategoryBreakdownPageRenderer {

    private final FooterRenderer footerRenderer;
    private static final float MIN_Y_FOR_NEW_SECTION = 150f;
    private static final float ROW_HEIGHT = 25f;
    private static final float SECTION_HEADER_HEIGHT = 40f;
    private static final float PROGRESS_BAR_WIDTH = 80f;

    public CategoryBreakdownPageRenderer(String reportTitle) {
        this.footerRenderer = new FooterRenderer(reportTitle, LocalDate.now());
    }

    /**
     * Render category breakdown pages (may span multiple pages).
     *
     * @param document PDF document
     * @param data Report data
     * @param startPageNumber Starting page number
     * @param totalPages Total number of pages
     * @return Number of pages added
     * @throws IOException if rendering fails
     */
    public int render(PDDocument document, ReportDataDTO data, int startPageNumber, int totalPages) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        int pagesAdded = 1;
        int currentPageNumber = startPageNumber;

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        float yPosition = PdfLayoutHelper.getContentStartY(page);

        // Page title
        yPosition = drawPageTitle(contentStream, "Category Breakdown", yPosition);

        // Income section
        if (!data.incomeByCategory().isEmpty()) {
            float newY = drawCategorySection(contentStream, page, "Income", data.incomeByCategory(),
                                            yPosition, PdfColorPalette.INCOME_PRIMARY);
            if (newY < 0) {
                // Section didn't fit, need new page
                footerRenderer.drawFooter(contentStream, page, currentPageNumber, totalPages);
                contentStream.close();

                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                pagesAdded++;
                currentPageNumber++;

                contentStream = new PDPageContentStream(document, page);
                yPosition = PdfLayoutHelper.getContentStartY(page);
                yPosition = drawCategorySection(contentStream, page, "Income", data.incomeByCategory(),
                                              yPosition, PdfColorPalette.INCOME_PRIMARY);
            } else {
                yPosition = newY;
            }
            yPosition -= PdfLayoutHelper.PARAGRAPH_SPACING;
        }

        // Expenses section
        if (!data.expensesByCategory().isEmpty()) {
            if (yPosition < MIN_Y_FOR_NEW_SECTION) {
                // Start new page for expenses
                footerRenderer.drawFooter(contentStream, page, currentPageNumber, totalPages);
                contentStream.close();

                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                pagesAdded++;
                currentPageNumber++;

                contentStream = new PDPageContentStream(document, page);
                yPosition = PdfLayoutHelper.getContentStartY(page);
            }

            float newY = drawCategorySection(contentStream, page, "Expenses", data.expensesByCategory(),
                                            yPosition, PdfColorPalette.EXPENSE_PRIMARY);
            if (newY < 0) {
                footerRenderer.drawFooter(contentStream, page, currentPageNumber, totalPages);
                contentStream.close();

                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                pagesAdded++;
                currentPageNumber++;

                contentStream = new PDPageContentStream(document, page);
                yPosition = PdfLayoutHelper.getContentStartY(page);
                yPosition = drawCategorySection(contentStream, page, "Expenses", data.expensesByCategory(),
                                              yPosition, PdfColorPalette.EXPENSE_PRIMARY);
            } else {
                yPosition = newY;
            }
            yPosition -= PdfLayoutHelper.PARAGRAPH_SPACING;
        }

        // Savings section
        if (!data.savingsByCategory().isEmpty()) {
            if (yPosition < MIN_Y_FOR_NEW_SECTION) {
                // Start new page for savings
                footerRenderer.drawFooter(contentStream, page, currentPageNumber, totalPages);
                contentStream.close();

                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                pagesAdded++;
                currentPageNumber++;

                contentStream = new PDPageContentStream(document, page);
                yPosition = PdfLayoutHelper.getContentStartY(page);
            }

            drawCategorySection(contentStream, page, "Savings", data.savingsByCategory(),
                              yPosition, PdfColorPalette.SAVINGS_PRIMARY);
        }

        // Footer on last page
        footerRenderer.drawFooter(contentStream, page, currentPageNumber, totalPages);
        contentStream.close();

        return pagesAdded;
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
     * Draw a category section (Income, Expenses, or Savings).
     * Returns negative Y if section doesn't fit on current page.
     */
    private float drawCategorySection(PDPageContentStream contentStream, PDPage page,
                                     String sectionTitle, List<CategorySpendingDTO> categories,
                                     float yPosition, Color sectionColor) throws IOException {
        // Check if we have space for section header + at least one row
        float neededSpace = SECTION_HEADER_HEIGHT + ROW_HEIGHT + PdfLayoutHelper.PAGE_MARGIN + 40; // 40 for footer
        if (yPosition < neededSpace) {
            return -1; // Signal that we need a new page
        }

        // Section header
        yPosition = drawSectionHeader(contentStream, sectionTitle, yPosition, sectionColor);

        // Table header
        yPosition = drawTableHeader(contentStream, yPosition);

        // Category rows
        for (CategorySpendingDTO category : categories) {
            // Check if we have space for this row
            if (yPosition < ROW_HEIGHT + PdfLayoutHelper.PAGE_MARGIN + 40) {
                // Not enough space, caller should handle pagination
                break;
            }

            yPosition = drawCategoryRow(contentStream, category, yPosition);
        }

        return yPosition;
    }

    /**
     * Draw section header with colored background bar.
     */
    private float drawSectionHeader(PDPageContentStream contentStream, String title,
                                   float yPosition, Color color) throws IOException {
        float pageWidth = 595; // A4 width
        float barWidth = pageWidth - (PdfLayoutHelper.PAGE_MARGIN * 2);
        float barHeight = 30;

        // Draw colored bar
        PdfLayoutHelper.drawColoredRectangle(
            contentStream,
            PdfLayoutHelper.PAGE_MARGIN,
            yPosition - barHeight + 5,
            barWidth,
            barHeight,
            color
        );

        // Draw title text in white
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, PdfLayoutHelper.SUBHEADING_FONT_SIZE);
        contentStream.setNonStrokingColor(1f, 1f, 1f);

        contentStream.beginText();
        contentStream.newLineAtOffset(PdfLayoutHelper.PAGE_MARGIN + 10, yPosition - 20);
        contentStream.showText(title);
        contentStream.endText();

        contentStream.setNonStrokingColor(0f, 0f, 0f);
        return yPosition - barHeight - 5;
    }

    /**
     * Draw table header.
     */
    private float drawTableHeader(PDPageContentStream contentStream, float yPosition) throws IOException {
        float pageWidth = 595;
        float tableWidth = pageWidth - (PdfLayoutHelper.PAGE_MARGIN * 2);

        // Background
        PdfLayoutHelper.drawColoredRectangle(
            contentStream,
            PdfLayoutHelper.PAGE_MARGIN,
            yPosition - ROW_HEIGHT + 5,
            tableWidth,
            ROW_HEIGHT,
            PdfColorPalette.DARK_GRAY
        );

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, PdfLayoutHelper.SMALL_FONT_SIZE);
        contentStream.setNonStrokingColor(1f, 1f, 1f);

        float x = PdfLayoutHelper.PAGE_MARGIN + 10;
        float y = yPosition - 15;

        // Headers
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText("Category");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(x + 200, y);
        contentStream.showText("Amount");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(x + 300, y);
        contentStream.showText("Budget");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(x + 400, y);
        contentStream.showText("Utilization");
        contentStream.endText();

        contentStream.setNonStrokingColor(0f, 0f, 0f);
        return yPosition - ROW_HEIGHT;
    }

    /**
     * Draw a single category row with progress bar.
     */
    private float drawCategoryRow(PDPageContentStream contentStream, CategorySpendingDTO category,
                                 float yPosition) throws IOException {
        float pageWidth = 595;
        float tableWidth = pageWidth - (PdfLayoutHelper.PAGE_MARGIN * 2);
        float x = PdfLayoutHelper.PAGE_MARGIN + 10;
        float y = yPosition - 15;

        // Zebra striping would be handled by caller tracking row index
        // For simplicity, using alternating colors isn't applied here

        contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.SMALL_FONT_SIZE);

        // Category name (truncate if needed)
        String categoryName = truncateText(category.category().getName(), 180);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(categoryName);
        contentStream.endText();

        // Amount
        String amountText = CurrencyFormatter.formatCurrency(category.totalSpent());
        contentStream.beginText();
        contentStream.newLineAtOffset(x + 200, y);
        contentStream.showText(amountText);
        contentStream.endText();

        // Budget
        String budgetText = category.budgetAmount() != null
                          ? CurrencyFormatter.formatCurrency(category.budgetAmount())
                          : "N/A";
        contentStream.beginText();
        contentStream.newLineAtOffset(x + 300, y);
        contentStream.showText(budgetText);
        contentStream.endText();

        // Progress bar
        if (category.budgetAmount() != null && category.budgetAmount().compareTo(BigDecimal.ZERO) > 0) {
            double percentage = category.totalSpent()
                                       .divide(category.budgetAmount(), 4, RoundingMode.HALF_UP)
                                       .multiply(BigDecimal.valueOf(100))
                                       .doubleValue();

            ProgressBarRenderer.drawProgressBar(
                contentStream,
                x + 400,
                yPosition - 5,
                PROGRESS_BAR_WIDTH,
                percentage,
                true
            );
        } else {
            // No budget - show N/A
            contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.TINY_FONT_SIZE);
            contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            contentStream.beginText();
            contentStream.newLineAtOffset(x + 400, y);
            contentStream.showText("No budget");
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
        }

        // Draw bottom border
        contentStream.setStrokingColor(0.9f, 0.9f, 0.9f);
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(PdfLayoutHelper.PAGE_MARGIN, yPosition - ROW_HEIGHT);
        contentStream.lineTo(PdfLayoutHelper.PAGE_MARGIN + tableWidth, yPosition - ROW_HEIGHT);
        contentStream.stroke();
        contentStream.setStrokingColor(0f, 0f, 0f);

        return yPosition - ROW_HEIGHT;
    }

    /**
     * Truncate text to fit within a width.
     */
    private String truncateText(String text, float maxWidthInPoints) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        try {
            float textWidth = PdfLayoutHelper.getTextWidth(text, PDType1Font.HELVETICA, PdfLayoutHelper.SMALL_FONT_SIZE);
            if (textWidth <= maxWidthInPoints) {
                return text;
            }

            // Estimate characters that fit
            int estimatedChars = (int) ((maxWidthInPoints / textWidth) * text.length());
            if (estimatedChars < 3) {
                return "...";
            }

            String truncated = text.substring(0, Math.min(estimatedChars - 3, text.length()));
            return truncated + "...";
        } catch (IOException e) {
            return text.length() > 25 ? text.substring(0, 22) + "..." : text;
        }
    }
}
