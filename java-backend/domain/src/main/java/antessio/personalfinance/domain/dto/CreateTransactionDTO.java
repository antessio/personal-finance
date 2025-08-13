package antessio.personalfinance.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionDTO(String userOwner, LocalDate date, BigDecimal amount, String description, String source) {

}
