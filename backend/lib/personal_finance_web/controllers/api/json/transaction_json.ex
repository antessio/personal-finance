defmodule PersonalFinanceWeb.API.TransactionJSON do
  alias PersonalFinance.Finance.Transaction

  def index(%{transactions: transactions}) do
    %{data: for(transaction <- transactions, do: data(transaction))}
  end

  def show(%{transaction: transaction}) do
    %{data: data(transaction)}
  end

  defp data(%Transaction{} = transaction) do
    %{
      id: transaction.id,
      amount: transaction.amount,
      date: transaction.date,
      description: transaction.description,
      skip: transaction.skip,
      source: transaction.source,
      user_id: transaction.user_id
    }
  end
end
