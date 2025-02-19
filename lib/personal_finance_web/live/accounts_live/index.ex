defmodule PersonalFinanceWeb.AccountsLive.Index do
  use PersonalFinanceWeb, :live_view

  alias PersonalFinance.ExternalAccounts
  alias PersonalFinance.ExternalAccounts.Accounts

  @impl true
  def mount(_params, _session, socket) do
    {:ok, stream(socket, :accounts_collection, ExternalAccounts.list_accounts())}
  end

  @impl true
  def handle_params(params, _url, socket) do
    {:noreply, apply_action(socket, socket.assigns.live_action, params)}
  end

  defp apply_action(socket, :edit, %{"id" => id}) do
    socket
    |> assign(:page_title, "Edit Accounts")
    |> assign(:accounts, ExternalAccounts.get_accounts!(id))
  end

  defp apply_action(socket, :new, _params) do
    socket
    |> assign(:page_title, "New Accounts")
    |> assign(:accounts, %Accounts{})
  end

  defp apply_action(socket, :index, _params) do
    socket
    |> assign(:page_title, "Listing Accounts")
    |> assign(:accounts, nil)
  end

  @impl true
  def handle_info({PersonalFinanceWeb.AccountsLive.FormComponent, {:saved, accounts}}, socket) do
    {:noreply, stream_insert(socket, :accounts_collection, accounts)}
  end

  @impl true
  def handle_event("delete", %{"id" => id}, socket) do
    accounts = ExternalAccounts.get_accounts!(id)
    {:ok, _} = ExternalAccounts.delete_accounts(accounts)

    {:noreply, stream_delete(socket, :accounts_collection, accounts)}
  end
end
