defmodule PersonalFinance.ExternalAccounts.IntesaAccountProcessor do
  alias PersonalFinance.ExternalAccounts.TransactionsCategorization
  alias PersonalFinance.Finance.Transaction
  alias PersonalFinance.ExternalAccounts.Accounts

  @behaviour PersonalFinance.ExternalAccounts.AccountProcessor

  # 20 empty + 1 header
  @skip_rows 21

  @impl true
  @spec can_process?(%Accounts{}) :: boolean()
  def can_process?(%Accounts{source_type: "intesa"}), do: true
  def can_process?(_), do: false

  @impl true
  def process_account(%Accounts{
        status: "pending",
        source_type: "intesa",
        file_path: file_path
      }) do
    transaction_categorization = TransactionsCategorization.categorize_transactions()



    case Xlsxir.multi_extract(file_path) do
      {:error, error} ->
        #File.rm!(tmp_file)
        {:error, error}

      [ok: table_id] ->
        [rows: row_count, cols: _cols_count, cells: _cells_count, name: _sheet_name] =
          Xlsxir.get_info(table_id)

        transactions = Enum.to_list(@skip_rows..row_count)
        |> Enum.map(&Xlsxir.get_row(table_id, &1))
        |> Enum.filter(&(!skip_row(&1)))
        |> Enum.map(&parse_row(&1, transaction_categorization))
        {:ok, transactions}
    end
  end

  def process_account(%Accounts{source_type: "widiba"}), do: {:error, "Invalid account status"}
  def process_account(_), do: :skip

  defp parse_row(
         [
           transaction_date,
           operation,
           details,
           _account_card,
           _settlement,
           _category,
           _currency,
           amount
         ],
         transaction_categorization
       ) do
    %Transaction{
      date: transaction_date,
      amount: amount,
      description: operation <> " " <> details,
      source: "intesa"
    }
    |> transaction_categorization.()
  end

  defp skip_row([]) do
    true
  end

  defp skip_row(row) do
    Enum.all?(row, &is_nil/1)
  end
end
