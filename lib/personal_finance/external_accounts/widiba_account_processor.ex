defmodule PersonalFinance.ExternalAccounts.WidibaAccountProcessor do
  alias PersonalFinance.ExternalAccounts.TransactionsCategorization
  alias PersonalFinance.Finance.Transaction
  alias PersonalFinance.ExternalAccounts.Accounts

  @behaviour PersonalFinance.ExternalAccounts.AccountProcessor
  # 18 empty + 1 header
  @skip_rows 19
  @regex ~r/Data\s(\d{2}\/\d{2}\/\d{2})\sOra\s(\d{2}\.\d{2})/

  @impl true
  @spec can_process?(%Accounts{}) :: boolean()
  def can_process?(%Accounts{source_type: "widiba"}), do: true
  def can_process?(_), do: false

  @impl true
  def process_account(%Accounts{
        status: "pending",
        source_type: "widiba",
        file_path: file_path
      }) do
    transaction_categorization = TransactionsCategorization.categorize_transactions()

    transactions = case Xlsxir.multi_extract(file_path) do
      {:error, error} ->
        # File.rm!(tmp_file)
        {:error, error}

      [ok: table_id] ->
        [rows: row_count, cols: _cols_count, cells: _cells_count, name: _sheet_name] =
          Xlsxir.get_info(table_id)

        Enum.to_list(@skip_rows..row_count)
        |> Enum.map(&Xlsxir.get_row(table_id, &1))
        |> Enum.filter(&(!skip_row(&1)))
        |> Enum.map(&parse_row(&1, transaction_categorization))
    end

    {:ok, transactions}

  end

  def process_account(%Accounts{source_type: "widiba"}), do: {:error, "Invalid account status"}
  def process_account(_), do: :skip

  @spec extract_date_time(String.t()) :: {:ok, DateTime} | {:error, String.t()}
  defp extract_date_time(nil), do: {:error, "No description"}
  defp extract_date_time(string) do
    case Regex.run(@regex, string) do
      [_, date, time] ->
        time = String.replace(time, ".", ":")
        datetime_string = date <> " " <> time
        Timex.parse(datetime_string, "%d/%m/%y %H:%M", :strftime)

      _ ->
        {:error, "No match found"}
    end
  end

  defp skip_row([]) do
    true
  end

  defp skip_row(row) do
    Enum.all?(row, &is_nil/1)
  end

  #  %{
  #  "CAUSALE" => causale,
  #  "DATA CONT." => data_cont,
  #  "DATA VAL." => _data_val,
  #  "IMPORTO (€)(€)" => importo,
  #  "DESCRIZIONE" => descrizione
  # },
  defp parse_row(
         [
           _whatever,
           settlement_date,
           _another_date,
           reason,
           description,
           _whatever_2,
           amount
         ],
         transaction_categorization
       ) do
    transaction_date =
      case extract_date_time(description) do
        {:ok, date} ->
          date

        {:error, _} -> settlement_date
      end

    %Transaction{
      date: transaction_date,
      amount: amount,
      description: description || reason,
      source: "widiba",
      categories: []
    }
    |> transaction_categorization.()
  end
end
