defmodule PersonalFinance.MacroCategory do
  use TypedStruct

  @type macro_category_type() :: :income | :expense | :bills | :savings | :subscriptions | :debts

  typedstruct do
    field :type, macro_category_type()
    field :name, String.t()
  end

end
