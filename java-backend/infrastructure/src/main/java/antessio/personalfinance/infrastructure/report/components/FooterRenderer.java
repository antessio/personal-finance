package antessio.personalfinance.infrastructure.report.components;

import antessio.personalfinance.infrastructure.report.PdfColorPalette;
import antessio.personalfinance.infrastructure.report.PdfLayoutHelper;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Renders page footers with page numbers, title, and generation date.
 * Applied to all pages except the cover page.
 */
public class FooterRenderer {

    private static final float FOOTER_HEIGHT = 30f;
    private static final float FOOTER_Y_OFFSET = 20f;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final String reportTitle;
    private final LocalDate generationDate;

    public FooterRenderer(String reportTitle, LocalDate generationDate) {
        this.reportTitle = reportTitle;
        this.generationDate = generationDate != null ? generationDate : LocalDate.now();
    }

    /**
     * Draw footer on a page.
     *
     * @param contentStream PDF content stream
     * @param page Current page
     * @param pageNumber Current page number (1-based)
     * @param totalPages Total number of pages
     * @throws IOException if drawing fails
     */
    public void drawFooter(PDPageContentStream contentStream, PDPage page, int pageNumber, int totalPages) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float footerY = PdfLayoutHelper.PAGE_MARGIN - FOOTER_Y_OFFSET;

        // Draw horizontal separator line
        PdfLayoutHelper.drawHorizontalDivider(
            contentStream,
            PdfLayoutHelper.PAGE_MARGIN,
            footerY + 15,
            pageWidth - (PdfLayoutHelper.PAGE_MARGIN * 2),
            PdfColorPalette.MEDIUM_GRAY,
            0.5f
        );

        // Set font and color for footer text
        contentStream.setFont(PDType1Font.HELVETICA, PdfLayoutHelper.TINY_FONT_SIZE);
        contentStream.setNonStrokingColor(0.4f, 0.4f, 0.4f); // Dark gray

        // Left: Report title (truncated if too long)
        String leftText = truncateTitle(reportTitle, 150);
        contentStream.beginText();
        contentStream.newLineAtOffset(PdfLayoutHelper.PAGE_MARGIN, footerY);
        contentStream.showText(leftText);
        contentStream.endText();

        // Center: Page number
        String centerText = String.format("Page %d of %d", pageNumber, totalPages);
        float centerX = PdfLayoutHelper.calculateCenteredX(pageWidth, centerText, PDType1Font.HELVETICA, PdfLayoutHelper.TINY_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(centerX, footerY);
        contentStream.showText(centerText);
        contentStream.endText();

        // Right: Generation date
        String rightText = "Generated: " + generationDate.format(DATE_FORMATTER);
        float rightX = PdfLayoutHelper.calculateRightAlignedX(
            pageWidth,
            rightText,
            PDType1Font.HELVETICA,
            PdfLayoutHelper.TINY_FONT_SIZE,
            PdfLayoutHelper.PAGE_MARGIN
        );
        contentStream.beginText();
        contentStream.newLineAtOffset(rightX, footerY);
        contentStream.showText(rightText);
        contentStream.endText();

        // Reset color
        contentStream.setNonStrokingColor(0f, 0f, 0f);
    }

    /**
     * Truncate title if it exceeds maximum width.
     *
     * @param title Title to truncate
     * @param maxWidth Maximum width in points
     * @return Truncated title
     */
    private String truncateTitle(String title, float maxWidth) {
        if (title == null) {
            return "";
        }

        try {
            float textWidth = PdfLayoutHelper.getTextWidth(title, PDType1Font.HELVETICA, PdfLayoutHelper.TINY_FONT_SIZE);
            if (textWidth <= maxWidth) {
                return title;
            }

            // Truncate and add ellipsis
            String ellipsis = "...";
            int estimatedChars = (int) ((maxWidth / textWidth) * title.length());
            String truncated = title.substring(0, Math.min(estimatedChars, title.length()));

            while (PdfLayoutHelper.getTextWidth(truncated + ellipsis, PDType1Font.HELVETICA, PdfLayoutHelper.TINY_FONT_SIZE) > maxWidth
                   && truncated.length() > 1) {
                truncated = truncated.substring(0, truncated.length() - 1);
            }

            return truncated + ellipsis;
        } catch (IOException e) {
            return title.length() > 30 ? title.substring(0, 27) + "..." : title;
        }
    }
}
