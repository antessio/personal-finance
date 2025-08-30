package antessio.personalfinance.domain.model;

public enum AccountType {
    WIDIBA("Widiba"),
    INTESA("Intesa"),
    PAYPAL("PayPal"),
    SATISPAY("Satispay");

    private final String description;

    public static AccountType fromString(String accountType) {
        if (accountType == null) {
            throw new IllegalArgumentException("Account type cannot be null");
        }
        return AccountType.valueOf(accountType.toUpperCase());
    }
    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
