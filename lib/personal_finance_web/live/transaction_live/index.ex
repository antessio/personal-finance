defmodule PersonalFinanceWeb.TransactionLive.Index do
  use PersonalFinanceWeb, :live_view

  alias PersonalFinance.Finance
  alias PersonalFinance.Finance.Transaction

  @impl true
  def mount(_params, _session, socket) do
    {:ok, stream(socket, :transactions, Finance.list_transactions())}
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
