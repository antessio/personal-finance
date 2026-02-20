package antessio.personalfinance.infrastructure.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Professional currency and number formatting for PDF reports.
 * Uses Italian locale for Euro formatting with proper thousand separators.
 */
public class CurrencyFormatter {

    private static final DecimalFormat CURRENCY_FORMAT;
    private static final DecimalFormat PERCENTAGE_FORMAT;
    private static final DecimalFormat COMPACT_FORMAT;

    static {
        // Italian locale for Euro formatting
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ITALY);

        // Currency format: €1.234,56
        CURRENCY_FORMAT = new DecimalFormat("€ #,##0.00", symbols);

        // Percentage format: 85,5%
        PERCENTAGE_FORMAT = new DecimalFormat("#,##0.0'%'", symbols);

        // Compact format for large numbers: €1,2M
        COMPACT_FORMAT = new DecimalFormat("€ #,##0", symbols);
    }

    /**
     * Format a BigDecimal as currency with full precision.
     *
     * @param amount Amount to format (can be null)
     * @return Formatted string (e.g., "€1.234,56") or "€0,00" for null
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "€ 0,00";
        }
        return CURRENCY_FORMAT.format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Format a BigDecimal as currency with color indicator for positive/negative.
     *
     * @param amount Amount to format (can be null)
     * @return Formatted string with sign (e.g., "+€1.234,56" or "-€1.234,56")
     */
    public static String formatCurrencyWithSign(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "€ 0,00";
        }

        String formatted = CURRENCY_FORMAT.format(amount.abs().setScale(2, RoundingMode.HALF_UP));
        return amount.compareTo(BigDecimal.ZERO) > 0 ? "+" + formatted : "-" + formatted;
    }

    /**
     * Format a percentage value.
     *
     * @param percentage Percentage value (e.g., 85.5 for 85.5%)
     * @return Formatted string (e.g., "85,5%")
     */
    public static String formatPercentage(double percentage) {
        return PERCENTAGE_FORMAT.format(percentage);
    }

    /**
     * Format a percentage with one decimal place.
     *
     * @param numerator Numerator value
     * @param denominator Denominator value
     * @return Formatted percentage or "N/A" if denominator is zero
     */
    public static String formatPercentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return "N/A";
        }
        if (numerator == null) {
            return "0,0%";
        }

        double percentage = numerator.divide(denominator, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .doubleValue();
        return formatPercentage(percentage);
    }

    /**
     * Format a compact currency value for large amounts (e.g., €1,2M for millions).
     *
     * @param amount Amount to format
     * @return Compact formatted string
     */
    public static String formatCompactCurrency(BigDecimal amount) {
        if (amount == null) {
            return "€ 0";
        }

        BigDecimal absAmount = amount.abs();
        String prefix = amount.compareTo(BigDecimal.ZERO) < 0 ? "-" : "";

        if (absAmount.compareTo(BigDecimal.valueOf(1_000_000)) >= 0) {
            // Millions
            BigDecimal millions = absAmount.divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP);
            return prefix + COMPACT_FORMAT.format(millions) + "M";
        } else if (absAmount.compareTo(BigDecimal.valueOf(1_000)) >= 0) {
            // Thousands
            BigDecimal thousands = absAmount.divide(BigDecimal.valueOf(1_000), 1, RoundingMode.HALF_UP);
            return prefix + COMPACT_FORMAT.format(thousands) + "K";
        } else {
            return prefix + COMPACT_FORMAT.format(absAmount);
        }
    }

    /**
     * Calculate difference between two amounts and format as currency.
     *
     * @param actual Actual amount
     * @param budget Budget amount
     * @return Formatted difference with sign
     */
    public static String formatDifference(BigDecimal actual, BigDecimal budget) {
        if (actual == null && budget == null) {
            return "€ 0,00";
        }

        BigDecimal actualValue = actual != null ? actual : BigDecimal.ZERO;
        BigDecimal budgetValue = budget != null ? budget : BigDecimal.ZERO;
        BigDecimal difference = actualValue.subtract(budgetValue);

        return formatCurrencyWithSign(difference);
    }

    /**
     * Format a number without currency symbol.
     *
     * @param number Number to format
     * @param decimals Number of decimal places
     * @return Formatted number string
     */
    public static String formatNumber(BigDecimal number, int decimals) {
        if (number == null) {
            return "0";
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ITALY);
        String pattern = "#,##0";
        if (decimals > 0) {
            pattern += "." + "0".repeat(decimals);
        }

        DecimalFormat format = new DecimalFormat(pattern, symbols);
        return format.format(number.setScale(decimals, RoundingMode.HALF_UP));
    }
}
