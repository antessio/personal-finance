defmodule PersonalFinanceWeb.AccountsLive.Show do
  use PersonalFinanceWeb, :live_view

  alias PersonalFinance.ExternalAccounts

  @impl true
  def mount(_params, _session, socket) do
    {:ok, socket}
  end

  @impl true
  def handle_params(%{"id" => id}, _, socket) do
    {:noreply,
     socket
     |> assign(:page_title, page_title(socket.assigns.live_action))
     |> assign(:accounts, ExternalAccounts.get_accounts!(id))}
  end

  defp page_title(:show), do: "Show Accounts"
  defp page_title(:edit), do: "Edit Accounts"
end
