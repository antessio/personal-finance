defmodule PersonalFinance.Finance.MacroCategory do
  use TypedStruct

  @allowed_types [:income, :expense, :bills, :savings, :subscriptions, :debts]

  @type macro_category_type() :: :income | :expense | :bills | :savings | :subscriptions | :debts

  typedstruct do
    field :type, macro_category_type()
    field :name, String.t()
  end

  def allowed_types(), do: @allowed_types

end
