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
      source: "",
      categories: [""]
    }

    transactions = Finance.list_transactions(filters)

    total_amount =
      transactions
      |> Enum.map(& &1.amount)
      |> Enum.reduce(Decimal.new(0), &Decimal.add/2)

    months =
      Enum.to_list(Date.utc_today().year()..@years_start)
      |> Enum.map(&concatenated_months/1)
      |> List.flatten()

    {:ok,
     socket
     |> assign(:filters, filters)
     |> assign(:months, months)
     |> assign(:sources, ["widiba", "intesa", "paypal", "satispay"])
     |> assign(:categories, Finance.list_categories())
     |> assign(:selected_transactions, [])
     |> assign(:total_amount, total_amount)
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

  defp apply_action(socket, :bulk_categorization, _params) do
    categories = Finance.list_categories()

    socket
    |> assign(:page_title, "Bulk Categorization")
    |> assign(:categories, categories)
    |> assign(:selected_transactions, socket.assigns.selected_transactions || [])
    |> assign(:selected_categories, [])
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
        %{
          "skipped_included" => skipped_included,
          "month_year" => month_year,
          "source" => source,
          "category" => category
        } = f,
        socket
      ) do
    filters = %{
      month_year: month_year || "",
      skipped_included: parse_skip_included(skipped_included),
      source: source || "",
      categories: parse_category_filter(category)
    }

    transactions = Finance.list_transactions(filters)

    total_amount =
      transactions
      |> Enum.map(& &1.amount)
      |> Enum.reduce(Decimal.new(0), &Decimal.add/2)

    {:noreply,
     socket
     |> stream(:filters, f)
     |> stream(:transactions, [], reset: true)
     |> assign(:total_amount, total_amount)
     |> add_transactions_stream(transactions)}
  end

  @impl true
  def handle_event("toggle_skip", %{"id" => id}, socket) do
    transaction = Finance.toggle_skip_transaction!(id)

    {:noreply, stream_insert(socket, :transactions, transaction)}
  end

  @impl true
  def handle_event("reprocess_categories", _, socket) do



    transactions = Finance.list_transactions() |> Enum.map(&Finance.process_categories(&1))
    processed_transactions =
      Enum.filter(transactions, fn
        {:ok, _transaction} -> true
        _ -> false
      end)
      |> Enum.map(fn {:ok, transaction} -> transaction end)

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
      category = Finance.get_category!(category_id)
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

  @impl true
  def handle_event("toggle_select_transaction", %{"id" => id}, socket) do
    selected_transactions = Map.get(socket.assigns, :selected_transactions, [])

    if id in selected_transactions do
      selected_transactions = List.delete(selected_transactions, id)
      {:noreply, assign(socket, :selected_transactions, selected_transactions)}
    else
      selected_transactions = [id | selected_transactions]
      {:noreply, assign(socket, :selected_transactions, selected_transactions)}
    end
  end


  @impl true
  def handle_event("delete_selected", _, socket) do
    socket.assigns.selected_transactions
    |> Enum.each(fn id ->
      IO.inspect(id, label: "Selected transaction")
    end)

    {:noreply, socket |> assign(:selected_transactions, [])}
  end

  defp parse_category_filter(nil), do: nil
  defp parse_category_filter(""), do: nil
  defp parse_category_filter("uncategorized"), do: []
  defp parse_category_filter("categorized"), do: Finance.list_categories() |> Enum.map(& &1.id)
  defp parse_category_filter(category_id), do: [category_id]

  defp parse_skip_included("included"), do: false
  defp parse_skip_included("skipped"), do: true
  defp parse_skip_included(""), do: ""

  defp add_transactions_stream(socket, []) do
    socket
  end

  defp add_transactions_stream(socket, transactions) do
    Enum.reduce(transactions, socket, fn transaction, acc_socket ->
      stream_insert(acc_socket, :transactions, transaction)
    end)
  end

  defp convert_to_filters(
         %{
           "skipped_included" => skipped_included,
           "month_year" => month_year,
           "source" => source,
           "category" => category
         }
       ) do
    %{
      month_year: month_year || "",
      skipped_included: parse_skip_included(skipped_included),
      source: source || "",
      categories: parse_category_filter(category)
    }
  end

  defp convert_to_filters(_), do: %{}
end
