package antessio.personalfinance.infrastructure.report;

import antessio.personalfinance.domain.dto.ReportDataDTO;
import antessio.personalfinance.infrastructure.report.pages.CategoryBreakdownPageRenderer;
import antessio.personalfinance.infrastructure.report.pages.CoverPageRenderer;
import antessio.personalfinance.infrastructure.report.pages.MonthlyTrendsPageRenderer;
import antessio.personalfinance.infrastructure.report.pages.SummaryPageRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Enhanced PDF report generator with professional design, charts, and data visualizations.
 * Orchestrates specialized page renderers to create multi-page reports.
 *
 * <p>Report structure:
 * <ul>
 *   <li>Page 1: Cover page with key metrics</li>
 *   <li>Page 2: Financial summary with charts</li>
 *   <li>Page 3+: Category breakdown with progress bars (may span multiple pages)</li>
 *   <li>Last page: Monthly trends (annual reports only)</li>
 * </ul>
 */
@Component
public class PdfReportGenerator {

    /**
     * Generate a complete PDF report from financial data.
     *
     * @param data Report data including transactions, categories, budgets, and trends
     * @return PDF file as byte array
     * @throws IOException if PDF generation fails
     */
    public byte[] generateReport(ReportDataDTO data) throws IOException {
        PDDocument document = new PDDocument();

        try {
            // Initialize page renderers
            CoverPageRenderer coverPageRenderer = new CoverPageRenderer();
            SummaryPageRenderer summaryPageRenderer = new SummaryPageRenderer(data.reportTitle());
            CategoryBreakdownPageRenderer categoryPageRenderer = new CategoryBreakdownPageRenderer(data.reportTitle());
            MonthlyTrendsPageRenderer trendsPageRenderer = new MonthlyTrendsPageRenderer(data.reportTitle());

            // Calculate total pages (estimate)
            int estimatedTotalPages = calculateEstimatedPages(data);

            // Page 1: Cover page (no footer)
            coverPageRenderer.render(document, data);

            int currentPage = 2;

            // Page 2: Summary page
            summaryPageRenderer.render(document, data, currentPage, estimatedTotalPages);
            currentPage++;

            // Page 3+: Category breakdown (may span multiple pages)
            int categoryPages = categoryPageRenderer.render(document, data, currentPage, estimatedTotalPages);
            currentPage += categoryPages;

            // Last page: Monthly trends (annual reports only)
            if (data.month() == null && !data.monthlyTrends().isEmpty()) {
                trendsPageRenderer.render(document, data, currentPage, estimatedTotalPages);
            }

            // Convert to byte array
            return toByteArray(document);

        } finally {
            document.close();
        }
    }

    /**
     * Estimate total number of pages in the report.
     * Used for page numbering (page X of Y).
     *
     * @param data Report data
     * @return Estimated number of pages
     */
    private int calculateEstimatedPages(ReportDataDTO data) {
        int pages = 1; // Cover page

        pages++; // Summary page

        // Category breakdown pages (estimate ~20 categories per page)
        int totalCategories = data.incomeByCategory().size()
                            + data.expensesByCategory().size()
                            + data.savingsByCategory().size();
        int categoryPages = Math.max(1, (totalCategories + 19) / 20);
        pages += categoryPages;

        // Monthly trends page (annual reports only)
        if (data.month() == null && !data.monthlyTrends().isEmpty()) {
            pages++;
        }

        return pages;
    }

    /**
     * Convert PDDocument to byte array.
     *
     * @param document PDF document
     * @return PDF as byte array
     * @throws IOException if conversion fails
     */
    private byte[] toByteArray(PDDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return baos.toByteArray();
    }
}
