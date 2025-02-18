defmodule PersonalFinance.ExternalAccounts.TransactionsCategorization do
  alias PersonalFinance.Finance
  alias PersonalFinance.Finance.Transaction

  @spec categorize_transactions() :: (PersonalFinance.Finance.Transaction.t() ->
                                        PersonalFinance.Finance.Transaction.t())
  def categorize_transactions() do
    categories = Finance.list_categories()

    fn transaction ->
      matching_categories =
        categories
        |> Enum.filter(&any_matcher_matches?(transaction, &1))

      Transaction.assign_categories(transaction, matching_categories)
    end
  end

  defp any_matcher_matches?(transaction, category) do
    category.matchers
    |> Enum.map(&convert_matcher_to_regex/1)
    |> dbg()
    |> Enum.any?(&Regex.match?(&1, transaction.description))
  end

  defp convert_matcher_to_regex(matcher) do
    {:ok, regex} = Regex.compile(matcher)
    regex
  end
end
