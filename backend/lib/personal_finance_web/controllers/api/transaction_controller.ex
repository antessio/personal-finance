defmodule PersonalFinanceWeb.API.TransactionController do
  use PersonalFinanceWeb, :controller

  alias PersonalFinance.Finance
  alias PersonalFinance.Finance.Transaction

  action_fallback PersonalFinanceWeb.FallbackController

  def index(conn, _params) do
    user = conn.assigns.current_user
    transactions = Finance.list_user_transactions(user)
    render(conn, :index, transactions: transactions)
  end

  def create(conn, %{"transaction" => transaction_params}) do
    user = conn.assigns.current_user
    transaction_params = Map.put(transaction_params, "user_id", user.id)

    with {:ok, %Transaction{} = transaction} <- Finance.create_transaction(transaction_params) do
      conn
      |> put_status(:created)
      |> put_resp_header("location", ~p"/api/transactions/#{transaction}")
      |> render(:show, transaction: transaction)
    end
  end

  def show(conn, %{"id" => id}) do
    user = conn.assigns.current_user
    transaction = Finance.get_user_transaction!(user, id)
    render(conn, :show, transaction: transaction)
  end

  def update(conn, %{"id" => id, "transaction" => transaction_params}) do
    user = conn.assigns.current_user
    transaction = Finance.get_user_transaction!(user, id)

    with {:ok, %Transaction{} = transaction} <- Finance.update_transaction(transaction, transaction_params) do
      render(conn, :show, transaction: transaction)
    end
  end

  def delete(conn, %{"id" => id}) do
    user = conn.assigns.current_user
    transaction = Finance.get_user_transaction!(user, id)

    with {:ok, %Transaction{}} <- Finance.delete_transaction(transaction) do
      send_resp(conn, :no_content, "")
    end
  end
end
