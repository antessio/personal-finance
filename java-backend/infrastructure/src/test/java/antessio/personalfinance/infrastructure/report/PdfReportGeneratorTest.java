package antessio.personalfinance.infrastructure.report;

import antessio.personalfinance.domain.dto.CategoryDTO;
import antessio.personalfinance.domain.dto.CategorySpendingDTO;
import antessio.personalfinance.domain.dto.MonthlyDataDTO;
import antessio.personalfinance.domain.dto.ReportDataDTO;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MacroCategoryEnum;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfReportGeneratorTest {

    private PdfReportGenerator pdfReportGenerator;

    @BeforeEach
    void setUp() {
        pdfReportGenerator = new PdfReportGenerator();
    }

    @Test
    void shouldGenerateValidPdfFromAnnualReportData() throws IOException {
        // Given
        CategoryDTO testCategory = new CategoryDTO(new CategoryId(1L), "Groceries", MacroCategoryEnum.EXPENSE,
                null, "🛒", "testuser", Collections.emptySet(), null, null);

        ReportDataDTO testData = new ReportDataDTO(
                "Annual Report 2024",
                "testuser",
                2024,
                null,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(30000),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(48000),
                BigDecimal.valueOf(32000),
                BigDecimal.valueOf(12000),
                Collections.emptyList(),
                List.of(new CategorySpendingDTO(testCategory, BigDecimal.valueOf(5000), BigDecimal.valueOf(6000))),
                Collections.emptyList(),
                List.of(new MonthlyDataDTO(YearMonth.of(2024, 1), 0,
                        BigDecimal.valueOf(4000), BigDecimal.valueOf(2500), BigDecimal.valueOf(800),
                        BigDecimal.valueOf(4000), BigDecimal.valueOf(2667), BigDecimal.valueOf(1000)))
        );

        // When
        byte[] pdfBytes = pdfReportGenerator.generateReport(testData);

        // Then
        assertThat(pdfBytes).isNotEmpty();

        // Verify it's a valid PDF by loading it
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(0);
            assertThat(doc.getNumberOfPages()).isEqualTo(4); // Cover, Summary, Category, Monthly Trends
        }
    }

    @Test
    void shouldGenerateValidPdfFromMonthlyReportData() throws IOException {
        // Given
        CategoryDTO testCategory = new CategoryDTO(new CategoryId(1L), "Dining", MacroCategoryEnum.EXPENSE,
                null, "🍽️", "testuser", Collections.emptySet(), null, null);

        ReportDataDTO testData = new ReportDataDTO(
                "Monthly Report June 2024",
                "testuser",
                2024,
                6,
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(800),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(2667),
                BigDecimal.valueOf(1000),
                Collections.emptyList(),
                List.of(new CategorySpendingDTO(testCategory, BigDecimal.valueOf(300), BigDecimal.valueOf(400))),
                Collections.emptyList(),
                Collections.emptyList() // No monthly trends for monthly report
        );

        // When
        byte[] pdfBytes = pdfReportGenerator.generateReport(testData);

        // Then
        assertThat(pdfBytes).isNotEmpty();

        // Verify it's a valid PDF
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(0);
            assertThat(doc.getNumberOfPages()).isEqualTo(3); // Cover, Summary, Category (no monthly trends)
        }
    }

    @Test
    void shouldHandleEmptyDataGracefully() throws IOException {
        // Given
        ReportDataDTO emptyData = new ReportDataDTO(
                "Annual Report 2024",
                "testuser",
                2024,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        // When
        byte[] pdfBytes = pdfReportGenerator.generateReport(emptyData);

        // Then
        assertThat(pdfBytes).isNotEmpty();

        // Verify it's a valid PDF
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(0);
        }
    }

    @Test
    void shouldHandleNullBudgetAmounts() throws IOException {
        // Given
        CategoryDTO testCategory = new CategoryDTO(new CategoryId(1L), "Other", MacroCategoryEnum.EXPENSE,
                null, null, "testuser", Collections.emptySet(), null, null);

        ReportDataDTO testData = new ReportDataDTO(
                "Monthly Report June 2024",
                "testuser",
                2024,
                6,
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(800),
                null, // null budget
                null,
                null,
                Collections.emptyList(),
                List.of(new CategorySpendingDTO(testCategory, BigDecimal.valueOf(300), null)), // null budget
                Collections.emptyList(),
                Collections.emptyList()
        );

        // When
        byte[] pdfBytes = pdfReportGenerator.generateReport(testData);

        // Then
        assertThat(pdfBytes).isNotEmpty();

        // Verify it's a valid PDF
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(0);
        }
    }

    @Test
    void shouldHandleMultipleCategories() throws IOException {
        // Given
        CategoryDTO category1 = new CategoryDTO(new CategoryId(1L), "Groceries", MacroCategoryEnum.EXPENSE,
                null, "🛒", "testuser", Collections.emptySet(), null, null);
        CategoryDTO category2 = new CategoryDTO(new CategoryId(2L), "Transportation", MacroCategoryEnum.EXPENSE,
                null, "🚗", "testuser", Collections.emptySet(), null, null);
        CategoryDTO category3 = new CategoryDTO(new CategoryId(3L), "Utilities", MacroCategoryEnum.EXPENSE,
                null, "💡", "testuser", Collections.emptySet(), null, null);

        ReportDataDTO testData = new ReportDataDTO(
                "Annual Report 2024",
                "testuser",
                2024,
                null,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(30000),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(48000),
                BigDecimal.valueOf(32000),
                BigDecimal.valueOf(12000),
                Collections.emptyList(),
                List.of(
                        new CategorySpendingDTO(category1, BigDecimal.valueOf(5000), BigDecimal.valueOf(6000)),
                        new CategorySpendingDTO(category2, BigDecimal.valueOf(3000), BigDecimal.valueOf(4000)),
                        new CategorySpendingDTO(category3, BigDecimal.valueOf(2000), BigDecimal.valueOf(2500))
                ),
                Collections.emptyList(),
                Collections.emptyList()
        );

        // When
        byte[] pdfBytes = pdfReportGenerator.generateReport(testData);

        // Then
        assertThat(pdfBytes).isNotEmpty();

        // Verify it's a valid PDF
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(0);
        }
    }
}
