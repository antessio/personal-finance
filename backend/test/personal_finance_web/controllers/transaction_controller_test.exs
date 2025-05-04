defmodule PersonalFinanceWeb.TransactionControllerTest do
  use PersonalFinanceWeb.ConnCase

  import PersonalFinance.FinanceFixtures
  import PersonalFinance.AccountsFixtures

  alias PersonalFinance.Finance.Transaction

  @create_attrs %{
    skip: true,
    date: ~D[2025-05-03],
    description: "some description",
    source: "some source",
    amount: "120.5"
  }
  @update_attrs %{
    skip: false,
    date: ~D[2025-05-04],
    description: "some updated description",
    source: "some updated source",
    amount: "456.7"
  }
  @invalid_attrs %{skip: nil, date: nil, description: nil, source: nil, amount: nil}

  setup %{conn: conn} do
    user = user_fixture()
    conn = conn |> put_req_header("accept", "application/json") |> log_in_user(user)
    {:ok, conn: conn, user: user}
  end

  describe "index" do
    test "lists all transactions", %{conn: conn, user: user} do
      transaction = transaction_fixture(%{user_id: user.id})
      conn = get(conn, ~p"/api/transactions")
      assert json_response(conn, 200)["data"] == [
        %{
          "id" => transaction.id,
          "amount" => "120.5",
          "date" => "2025-05-03",
          "description" => "some description",
          "skip" => true,
          "source" => "some source",
          "user_id" => user.id
        }
      ]
    end
  end

  describe "create transaction" do
    test "renders transaction when data is valid", %{conn: conn, user: user} do
      conn = post(conn, ~p"/api/transactions", transaction: @create_attrs)
      assert %{"id" => id} = json_response(conn, 201)["data"]

      conn = get(conn, ~p"/api/transactions/#{id}")

      assert %{
               "id" => ^id,
               "amount" => "120.5",
               "date" => "2025-05-03",
               "description" => "some description",
               "skip" => true,
               "source" => "some source",
               "user_id" => ^user.id
             } = json_response(conn, 200)["data"]
    end

    test "renders errors when data is invalid", %{conn: conn} do
      conn = post(conn, ~p"/api/transactions", transaction: @invalid_attrs)
      assert json_response(conn, 422)["errors"] != %{}
    end
  end

  describe "update transaction" do
    setup [:create_transaction]

    test "renders transaction when data is valid", %{conn: conn, transaction: %Transaction{id: id} = transaction} do
      conn = put(conn, ~p"/api/transactions/#{transaction}", transaction: @update_attrs)
      assert %{"id" => ^id} = json_response(conn, 200)["data"]

      conn = get(conn, ~p"/api/transactions/#{id}")

      assert %{
               "id" => ^id,
               "amount" => "456.7",
               "date" => "2025-05-04",
               "description" => "some updated description",
               "skip" => false,
               "source" => "some updated source"
             } = json_response(conn, 200)["data"]
    end

    test "renders errors when data is invalid", %{conn: conn, transaction: transaction} do
      conn = put(conn, ~p"/api/transactions/#{transaction}", transaction: @invalid_attrs)
      assert json_response(conn, 422)["errors"] != %{}
    end
  end

  describe "delete transaction" do
    setup [:create_transaction]

    test "deletes chosen transaction", %{conn: conn, transaction: transaction} do
      conn = delete(conn, ~p"/api/transactions/#{transaction}")
      assert response(conn, 204)

      assert_error_sent 404, fn ->
        get(conn, ~p"/api/transactions/#{transaction}")
      end
    end
  end

  defp create_transaction(%{user: user}) do
    transaction = transaction_fixture(%{user_id: user.id})
    %{transaction: transaction}
  end
end
