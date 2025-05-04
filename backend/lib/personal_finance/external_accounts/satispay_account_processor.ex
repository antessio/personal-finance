defmodule PersonalFinance.ExternalAccounts.SatispayAccountProcessor do
  alias PersonalFinance.ExternalAccounts.TransactionsCategorization
  alias PersonalFinance.Finance.Transaction
  alias PersonalFinance.ExternalAccounts.Accounts

  @behaviour PersonalFinance.ExternalAccounts.AccountProcessor

  @impl true
  @spec can_process?(%Accounts{}) :: boolean()
  def can_process?(%Accounts{source_type: "satispay"}), do: true
  def can_process?(_), do: false

  @impl true
  def process_account(%Accounts{
        status: "pending",
        source_type: "satispay",
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
     |> CSV.decode!(headers: true, separator: ?,, trim: true)
     |> Stream.filter(&(&1["kind"] != "Ricarica Satispay"))
     |> Enum.map(&parse_line(&1, transaction_categorization))
     |> Enum.reject(&is_nil/1)
    }
  end

  def process_account(%Accounts{source_type: "satispay"}), do: {:error, "Invalid account status"}
  def process_account(_), do: :skip

  @spec parse_line(Map.t(), (Transaction.t() -> Transaction.t())) ::
          Transaction.t() | nil
  defp parse_line(
         %{
           "Data" => date,
           "Importo" => amount_str,
           "ID (Comunicalo all’assistenza clienti in caso di problemi)" => _id,
           "Informazioni aggiuntive (il servizio clienti potrebbe richiederle)" => _extra_info,
           "Nome" => name,
           "Stato" => _state,
           "Tipologia" => kind
         },
         transaction_categorization
       ) do
    # convert string to date in the format dd/mm/yyyy
    date = convert_date!(date, "%d %b %Y %H:%M:%S")

    # convert string to float
    amount =
      amount_str
      |> String.trim()
      |> String.replace(",", ".")
      |> String.to_float()

    %Transaction{
      date: date,
      amount: amount,
      description: name <> " " <> kind,
      source: "satispay",
      categories: []
    }
    |> transaction_categorization.()
  end

  defp parse_line(
         %{
           "id" => _id,
           "name" => name,
           "state" => _state,
           "kind" => kind,
           "date" => date,
           "amount" => amount_str,
           "currency" => _currency,
           "extra info" => _extra_info
         },
         transaction_categorization
       ) do
    # convert string to date in the format dd/mm/yyyy
    date = convert_date!(date)

    # convert string to float
    amount =
      amount_str
      |> String.trim()
      |> String.replace(",", ".")
      |> String.to_float()

    %Transaction{
      date: date,
      amount: amount,
      description: name <> " " <> kind,
      source: "satispay",
      categories: []
    }
    |> transaction_categorization.()
  end

  defp parse_line(_row, _transaction_categorization) do
    nil
  end

  defp convert_date!(date, format \\ "%d %b %Y. %H:%M:%S") do
    # 30 set 2024. 00:35:42

    date
    |> translate_month()
    |> fix_days()
    |> Timex.parse(format, :strftime)
    |> case do
      {:ok, date} -> date
      _ -> raise "Failed to parse date: #{date}"
    end
  end

  @spec fix_days(String.t()) :: String.t()
  defp fix_days(date_str) do
    # take first two characters
    date_split =
      date_str
      |> String.split(" ")

    [days | rest] = date_split

    days =
      days
      |> String.slice(0, 2)
      |> String.pad_leading(2, "0")

    [days | rest]
    |> Enum.join(" ")
  end

  @spec translate_month(String.t()) :: String.t()
  defp translate_month(date_str) do
    date_str
    |> String.replace("gen", "Jan")
    |> String.replace("feb", "Feb")
    |> String.replace("mar", "Mar")
    |> String.replace("apr", "Apr")
    |> String.replace("mag", "May")
    |> String.replace("giu", "Jun")
    |> String.replace("lug", "Jul")
    |> String.replace("ago", "Aug")
    |> String.replace("set", "Sep")
    |> String.replace("ott", "Oct")
    |> String.replace("nov", "Nov")
    |> String.replace("dic", "Dec")
  end
end
