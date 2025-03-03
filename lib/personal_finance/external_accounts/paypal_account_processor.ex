defmodule PersonalFinance.ExternalAccounts.PayPalAccountProcessor do
  alias PersonalFinance.ExternalAccounts.TransactionsCategorization
  alias PersonalFinance.Finance.Transaction
  alias PersonalFinance.ExternalAccounts.Accounts

  @behaviour PersonalFinance.ExternalAccounts.AccountProcessor

  @impl true
  @spec can_process?(%Accounts{}) :: boolean()
  def can_process?(%Accounts{source_type: "paypal"}), do: true
  def can_process?(_), do: false

  @impl true
  @spec process_account(any()) :: :skip
  def process_account(%Accounts{
        source_type: "paypal",
        file_path: file_path
      }) do
    transaction_categorization = TransactionsCategorization.categorize_transactions()


    {:ok,
     file_path
     |> File.read!()
     |> String.replace_prefix("\uFEFF", "")
     |> String.replace("\n", "\r\n")
     |> String.split("\n")
     |> Stream.map(& &1)
     |> Stream.map(&String.replace(&1, "\"", "|"))
     |> Stream.map(&String.replace(&1, "|,", "|;"))
     |> CSV.decode!(
       headers: true,
       separator: ?;,
       trim: true,
       escape_character: ?|,
       unredact_exceptions: true
     )
     |> Enum.filter(fn row -> row["Stato"] == "Completata" and row["Nome"] != "" end)
     |> Enum.map(&parse_line(&1, transaction_categorization))}
  end

  def process_account(_), do: :skip

  # "Data","Orario","Fuso orario","Nome","Tipo","Stato","Valuta","Importo","Codice ricevuta","Saldo"
  @spec parse_line(Map.t(), (Transaction.t() -> Transaction.t())) :: Transaction.t()
  defp parse_line(
         %{
           "Data" => date,
           "Orario" => hour,
           "Fuso orario" => _timezone,
           "Nome" => name,
           "Tipo" => _type,
           "Stato" => _status,
           "Valuta" => _currency,
           "Importo" => amount,
           "Codice ricevuta" => _receipt_code,
           "Saldo" => _balance
         },
         transaction_categorization
       ) do
    # convert string to date in the format dd/mm/yyyy
    datetime_string = date <> " " <> hour

    {:ok, date} =
      datetime_string
      |> Timex.parse("%d/%m/%Y %H:%M:%S", :strftime)
      |> dbg()

    # convert string to float
    amount = String.to_float(amount)

    %Transaction{
      date: date,
      amount: amount,
      description: name,
      source: "paypal"
    }
    |> transaction_categorization.()
  end

  defp parse_line(_, _) do
    nil
  end
end
