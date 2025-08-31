package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.AccountType;

import java.math.BigDecimal;

public record AccountMonthlyDataDTO(int year, int month, int week, AccountType accountType, BigDecimal total) {
}
