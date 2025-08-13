package antessio.personalfinance.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
@AllArgsConstructor
@Getter
@Builder
public class TransactionExportDTO {
    private LocalDate date;
    private String type;
    private String macroCategory;
    private String category;
    private String currency;
    private String amount;
    private String description;
}

