defmodule PersonalFinance.ExternalAccounts.WidibaAccountProcessor do
  alias PersonalFinance.ExternalAccounts.TransactionsCategorization
  alias PersonalFinance.Finance.Transaction
  alias PersonalFinance.ExternalAccounts.Accounts

  @behaviour PersonalFinance.ExternalAccounts.AccountProcessor

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

    {:ok,
     file_path
     |> File.read!()
     |> String.split("\n")
     |> Stream.map(& &1)
     |> CSV.decode!(headers: true, separator: ?,, trim: true)
     |> Enum.map(&parse_line(&1, transaction_categorization))}
  end

  def process_account(%Accounts{source_type: "widiba"}), do: {:error, "Invalid account status"}
  def process_account(_), do: :skip

  @spec parse_line(Map.t(), (Transaction.t() -> Transaction.t())) :: Transaction.t()
  defp parse_line(
         %{
           "CAUSALE" => causale,
           "DATA CONT." => data_cont,
           "DATA VAL." => _data_val,
           "IMPORTO (€)(€)" => importo,
           "DESCRIZIONE" => descrizione
         },
         transaction_categorization
       ) do
    # convert string to date in the format dd/mm/yyyy
    date =
      case extract_date_time(descrizione) do
        {:ok, date} -> date
        {:error, _} -> data_cont
      end

    # convert string to float
    amount = String.to_float(importo)

    %Transaction{
      date: date,
      amount: amount,
      description: causale <> " " <> descrizione,
      source: "widiba"
    }
    |> transaction_categorization.()
  end

  @spec extract_date_time(String.t()) :: {:ok, DateTime} | {:error, String.t()}
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
end
