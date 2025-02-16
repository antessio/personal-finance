defmodule PersonalFinance.MacroCategories do
  alias PersonalFinance.MacroCategory


  @spec get_macro_categories() :: [PersonalFinance.MacroCategory.t()]
  def get_macro_categories() do
    [
      %MacroCategory{type: :income, name: "Income"},
      %MacroCategory{type: :expense, name: "Expense"},
      %MacroCategory{type: :bills, name: "Bills"},
      %MacroCategory{type: :savings, name: "Savings"},
      %MacroCategory{type: :subscriptions, name: "Subscriptions"},
      %MacroCategory{type: :debts, name: "Debts"}
    ]
  end
end
