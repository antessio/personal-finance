defmodule PersonalFinanceWeb.TransactionLive.Index do
  use PersonalFinanceWeb, :live_view

  alias PersonalFinance.Finance
  alias PersonalFinance.Finance.Transaction

  @years_start 1970

  defp concatenated_months(year) do
    Enum.map(1..12, fn month ->
      month
      |> Integer.to_string()
      |> then(&String.pad_leading(&1, 2, "0"))
      |> then(&"#{year}-#{&1}")
      |> then(&%{value: &1, label: &1})
    end)
  end

  @impl true
  def mount(_params, _session, socket) do
    # Initial filter state
    filters = %{
      month_year: "",
      skipped_included: "",
      source: ""
    }

    transactions = Finance.list_transactions(filters)

    months =
      Enum.to_list(Date.utc_today().year()..@years_start)
      |> Enum.map(&concatenated_months/1)
      |> List.flatten()

    {:ok,
     socket
     |> assign(:filters, filters)
     |> assign(:months, months)
     |> assign(:sources, ["widiba", "intesa", "paypal", "satispay"])
     |> stream(:transactions, transactions)}
  end

  @impl true
  def handle_params(params, _url, socket) do
    {:noreply, apply_action(socket, socket.assigns.live_action, params)}
  end

  defp apply_action(socket, :edit, %{"id" => id}) do
    categories = Finance.list_categories()
    transaction = Finance.get_transaction!(id)

    socket
    |> assign(:page_title, "Edit Transaction")
    |> assign(:categories, categories)
    |> assign(:selected_categories, transaction.categories)
    |> assign(:transaction, transaction)
  end

  defp apply_action(socket, :new, _params) do
    categories = Finance.list_categories()

    socket
    |> assign(:page_title, "New Transaction")
    |> assign(:categories, categories)
    |> assign(:selected_categories, [])
    |> assign(:transaction, %Transaction{})
  end

  defp apply_action(socket, :index, _params) do
    socket
    |> assign(:page_title, "Listing Transactions")
    |> assign(:transaction, nil)
  end

  @impl true
  def handle_info(
        {PersonalFinanceWeb.TransactionLive.FormComponent, {:saved, transaction}},
        socket
      ) do
    {:noreply, stream_insert(socket, :transactions, transaction)}
  end

  @impl true
  def handle_event("delete", %{"id" => id}, socket) do
    transaction = Finance.get_transaction!(id)
    {:ok, _} = Finance.delete_transaction(transaction)

    {:noreply, stream_delete(socket, :transactions, transaction)}
  end



  @impl true
  def handle_event(
        "filter",
        %{"skipped_included" => skipped_included, "month_year" => month_year, "source" => source} = f,
        socket
      ) do
        IO.inspect(f, label: "filter")
    filters = %{
      month_year: month_year || "",
      skipped_included: parse_skip_included(skipped_included),
      source: source || ""
    }

    # Fetch filtered transactions
    # IO.inspect(Enum.count(socket.assigns.transactions), label: "transactions count")
    transactions = Finance.list_transactions(filters)
    IO.inspect(Enum.count(transactions), label: "filtered transactions count")

    {:noreply,
     socket
     |> stream(:transactions, [], reset: true)
     |> add_transactions_stream(transactions)}

    # {:noreply, stream_insert(socket, :transactions, transactions)}
  end
  defp parse_skip_included("included"), do: true
  defp parse_skip_included("skipped"), do: false
  defp parse_skip_included(""), do: ""

  defp add_transactions_stream(socket, []) do
    socket
  end
  defp add_transactions_stream(socket, transactions) do
    Enum.reduce(transactions, socket, fn transaction, acc_socket ->
      stream_insert(acc_socket, :transactions, transaction)
    end)
  end

  @impl true
  def handle_event("toggle_skip", %{"id" => id}, socket) do
    transaction = Finance.toggle_skip_transaction!(id)

    {:noreply, stream_insert(socket, :transactions, transaction)}
  end

  @impl true
  def handle_event("reprocess_categories", _, socket) do
    transactions =
      Finance.list_transactions()
      |> Enum.map(&Finance.process_categories(&1))

    processed_transactions =
      Enum.filter(transactions, fn
        {:ok, _transaction} -> true
        _ -> false
      end)
      |> Enum.map(fn {:ok, transaction} -> transaction end)
      |> dbg()

    socket =
      Enum.reduce(processed_transactions, socket, fn transaction, acc_socket ->
        stream_insert(acc_socket, :transactions, transaction)
      end)

    {:noreply, socket}
  end

  @impl true
  def handle_event("add_category_transaction", %{"category_id" => category_id}, socket) do
    category_id = String.to_integer(category_id)
    categories = socket.assigns.selected_categories || []

    # Ensure we don't add duplicates
    if category_id in Enum.map(categories, & &1.id) do
      {:noreply, socket}
    else
      category = Finance.get_category!(category_id) |> dbg()
      {:noreply, assign(socket, :selected_categories, categories ++ [category])}
    end
  end

  @impl true
  def handle_event("remove_category_transaction", %{"category_id" => category_id}, socket) do
    category_id = String.to_integer(category_id)
    categories = socket.assigns.selected_categories || []

    new_categories = Enum.reject(categories, fn c -> c.id == category_id end)

    {:noreply, assign(socket, :selected_categories, new_categories)}
  end
end
