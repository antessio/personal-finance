defmodule PersonalFinance.ExternalAccounts.AccountProcessor do
  alias PersonalFinance.Finance.Transaction
  alias PersonalFinance.ExternalAccounts.Accounts


  @callback can_process?(%Accounts{}) :: boolean()
  @callback process_account(%Accounts{}) :: {:ok, [Transaction.t()]} | {:error, String.t()} | :skip
end
