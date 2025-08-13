package antessio.personalfinance.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Builder
public class SavingsExportDTO {
    private LocalDate date;
    private String category;
    private String currency;
    private String amount;
}

