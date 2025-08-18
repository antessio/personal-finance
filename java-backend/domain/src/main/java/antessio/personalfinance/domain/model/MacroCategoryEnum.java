package antessio.personalfinance.domain.model;

import java.util.List;

public enum MacroCategoryEnum {
    INCOME,
    EXPENSE,
    BILLS,
    SAVINGS,
    SUBSCRIPTIONS,
    DEBTS;

    private static List<MacroCategoryEnum> INCOME_MACRO_CATEGORIES = List.of(MacroCategoryEnum.INCOME);
    private static List<MacroCategoryEnum> SAVINGS_MACRO_CATEGORIES = List.of(MacroCategoryEnum.SAVINGS);
    private static List<MacroCategoryEnum> EXPENSE_MACRO_CATEGORIES = List.of(MacroCategoryEnum.EXPENSE,
            MacroCategoryEnum.BILLS,
            MacroCategoryEnum.DEBTS,
            MacroCategoryEnum.SUBSCRIPTIONS);


    public boolean isSavings() {
        return SAVINGS_MACRO_CATEGORIES.contains(this);
    }

    public boolean isIncome() {
        return INCOME_MACRO_CATEGORIES.contains(this);
    }

    public boolean isExpense() {
        return EXPENSE_MACRO_CATEGORIES.contains(this);
    }

}
