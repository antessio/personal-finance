defmodule PersonalFinanceWeb.API.ExternalAccountController do
  use PersonalFinanceWeb, :controller

  alias PersonalFinance.ExternalAccounts
  alias PersonalFinance.ExternalAccounts.Account

  action_fallback PersonalFinanceWeb.FallbackController

  def index(conn, _params) do
    user = conn.assigns.current_user
    accounts = ExternalAccounts.list_user_external_accounts(user)
    render(conn, :index, accounts: accounts)
  end

  def show(conn, %{"id" => id}) do
    user = conn.assigns.current_user
    account = ExternalAccounts.get_user_external_account!(user, id)
    render(conn, :show, account: account)
  end

  def create(conn, %{"account" => account_params}) do
    user = conn.assigns.current_user
    account_params = Map.put(account_params, "user_id", user.id)

    with {:ok, %Account{} = account} <- ExternalAccounts.create_external_account(account_params) do
      conn
      |> put_status(:created)
      |> put_resp_header("location", ~p"/api/external_accounts/#{account}")
      |> render(:show, account: account)
    end
  end

  def delete(conn, %{"id" => id}) do
    user = conn.assigns.current_user
    account = ExternalAccounts.get_user_external_account!(user, id)

    with {:ok, %Account{}} <- ExternalAccounts.delete_external_account(account) do
      send_resp(conn, :no_content, "")
    end
  end

  def process_import(conn, %{"id" => id}) do
    user = conn.assigns.current_user
    account = ExternalAccounts.get_user_external_account!(user, id)

    case ExternalAccounts.process_account_import(account) do
      {:ok, result} -> json(conn, %{result: result})
      {:error, reason} -> conn |> put_status(:unprocessable_entity) |> json(%{error: reason})
    end
  end
end
