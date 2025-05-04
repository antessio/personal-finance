defmodule PersonalFinanceWeb.TransactionJSON do
  alias PersonalFinance.Finance.Transaction

  @doc """
  Renders a list of transactions.
  """
  def index(%{transactions: transactions}) do
    %{data: for(transaction <- transactions, do: data(transaction))}
  end

  @doc """
  Renders a single transaction.
  """
  def show(%{transaction: transaction}) do
    %{data: data(transaction)}
  end

  defp data(%Transaction{} = transaction) do
    %{
      id: transaction.id,
      date: transaction.date,
      amount: transaction.amount,
      description: transaction.description,
      source: transaction.source,
      skip: transaction.skip,
      user_id: transaction.user_id
    }
  end
end
