package antessio.personalfinance.domain.model;

public enum AccountType {
    WIDIBA("Widiba"),
    INTESA("Intesa"),
    PAYPAL("PayPal"),
    SATISPAY("Satispay");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
