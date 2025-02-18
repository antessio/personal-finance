defmodule PersonalFinance.ExternalAccounts.AccountProcessor do
  alias PersonalFinance.ExternalAccounts.TransactionsCategorization
  alias PersonalFinance.Finance.Transaction
  alias PersonalFinance.ExternalAccounts.Accounts

  @callback process_account(%Accounts{}) :: {:ok, [Transaction.t()]} | {:error, String.t()} | :skip
end
