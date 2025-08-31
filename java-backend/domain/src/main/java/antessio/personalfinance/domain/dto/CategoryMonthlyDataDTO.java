package antessio.personalfinance.domain.dto;

import java.math.BigDecimal;

public record CategoryMonthlyDataDTO(CategoryDTO category,
                                     Integer week,
                                     Integer year,
                                     Integer month,
                                     BigDecimal total) {
}
