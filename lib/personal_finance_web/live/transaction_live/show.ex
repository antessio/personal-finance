defmodule PersonalFinanceWeb.TransactionLive.Show do
  use PersonalFinanceWeb, :live_view

  alias PersonalFinance.Finance

  @impl true
  def mount(_params, _session, socket) do
    {:ok, socket}
  end

  @impl true
  def handle_params(%{"id" => id}, _, socket) do
    transaction = Finance.get_transaction!(id)
    categories = Finance.list_categories()

    {:noreply,
     socket
     |> assign(:page_title, page_title(socket.assigns.live_action))
     |> assign(:categories, categories)
     |> assign(:selected_categories, transaction.categories)
     |> assign(:transaction, transaction)}
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

  defp page_title(:show), do: "Show Transaction"
  defp page_title(:edit), do: "Edit Transaction"
end
