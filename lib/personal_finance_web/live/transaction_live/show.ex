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

  defp page_title(:show), do: "Show Transaction"
  defp page_title(:edit), do: "Edit Transaction"
end
