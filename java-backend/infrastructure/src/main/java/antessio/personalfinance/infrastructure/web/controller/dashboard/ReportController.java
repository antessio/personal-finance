package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.dto.ReportDataDTO;
import antessio.personalfinance.domain.service.ReportDataService;
import antessio.personalfinance.infrastructure.report.PdfReportGenerator;
import antessio.personalfinance.infrastructure.security.persistence.User;
import antessio.personalfinance.infrastructure.security.service.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportDataService reportDataService;
    private final PdfReportGenerator pdfReportGenerator;
    private final SecurityUtils securityUtils;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam int year,
            @RequestParam(required = false) Integer month
    ) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Validate parameters
            if (year < 1900 || year > 2100) {
                logger.warn("Invalid year parameter: {}", year);
                return ResponseEntity.badRequest().build();
            }
            if (month != null && (month < 1 || month > 12)) {
                logger.warn("Invalid month parameter: {}", month);
                return ResponseEntity.badRequest().build();
            }

            // Get report data
            ReportDataDTO reportData = (month == null)
                    ? reportDataService.getAnnualReportData(user.getUsername(), year)
                    : reportDataService.getMonthlyReportData(user.getUsername(), year, month);

            // Generate PDF
            byte[] pdfBytes = pdfReportGenerator.generateReport(reportData);

            // Prepare filename
            String filename = (month == null)
                    ? String.format("annual-report-%d.pdf", year)
                    : String.format("monthly-report-%d-%02d.pdf", year, month);

            // Return PDF with proper headers
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for report generation", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error generating PDF report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
