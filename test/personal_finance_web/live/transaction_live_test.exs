defmodule PersonalFinanceWeb.TransactionLiveTest do
  use PersonalFinanceWeb.ConnCase

  import Phoenix.LiveViewTest
  import PersonalFinance.FinanceFixtures

  @create_attrs %{date: "2025-02-15", description: "some description", amount: "120.5"}
  @update_attrs %{date: "2025-02-16", description: "some updated description", amount: "456.7"}
  @invalid_attrs %{date: nil, description: nil, amount: nil}

  defp create_transaction(_) do
    transaction = transaction_fixture()
    %{transaction: transaction}
  end

  describe "Index" do
    setup [:create_transaction]

    test "lists all transactions", %{conn: conn, transaction: transaction} do
      {:ok, _index_live, html} = live(conn, ~p"/transactions")

      assert html =~ "Listing Transactions"
      assert html =~ transaction.description
    end

    test "saves new transaction", %{conn: conn} do
      {:ok, index_live, _html} = live(conn, ~p"/transactions")

      assert index_live |> element("a", "New Transaction") |> render_click() =~
               "New Transaction"

      assert_patch(index_live, ~p"/transactions/new")

      assert index_live
             |> form("#transaction-form", transaction: @invalid_attrs)
             |> render_change() =~ "can&#39;t be blank"

      assert index_live
             |> form("#transaction-form", transaction: @create_attrs)
             |> render_submit()

      assert_patch(index_live, ~p"/transactions")

      html = render(index_live)
      assert html =~ "Transaction created successfully"
      assert html =~ "some description"
    end

    test "updates transaction in listing", %{conn: conn, transaction: transaction} do
      {:ok, index_live, _html} = live(conn, ~p"/transactions")

      assert index_live |> element("#transactions-#{transaction.id} a", "Edit") |> render_click() =~
               "Edit Transaction"

      assert_patch(index_live, ~p"/transactions/#{transaction}/edit")

      assert index_live
             |> form("#transaction-form", transaction: @invalid_attrs)
             |> render_change() =~ "can&#39;t be blank"

      assert index_live
             |> form("#transaction-form", transaction: @update_attrs)
             |> render_submit()

      assert_patch(index_live, ~p"/transactions")

      html = render(index_live)
      assert html =~ "Transaction updated successfully"
      assert html =~ "some updated description"
    end

    test "deletes transaction in listing", %{conn: conn, transaction: transaction} do
      {:ok, index_live, _html} = live(conn, ~p"/transactions")

      assert index_live |> element("#transactions-#{transaction.id} a", "Delete") |> render_click()
      refute has_element?(index_live, "#transactions-#{transaction.id}")
    end
  end

  describe "Show" do
    setup [:create_transaction]

    test "displays transaction", %{conn: conn, transaction: transaction} do
      {:ok, _show_live, html} = live(conn, ~p"/transactions/#{transaction}")

      assert html =~ "Show Transaction"
      assert html =~ transaction.description
    end

    test "updates transaction within modal", %{conn: conn, transaction: transaction} do
      {:ok, show_live, _html} = live(conn, ~p"/transactions/#{transaction}")

      assert show_live |> element("a", "Edit") |> render_click() =~
               "Edit Transaction"

      assert_patch(show_live, ~p"/transactions/#{transaction}/show/edit")

      assert show_live
             |> form("#transaction-form", transaction: @invalid_attrs)
             |> render_change() =~ "can&#39;t be blank"

      assert show_live
             |> form("#transaction-form", transaction: @update_attrs)
             |> render_submit()

      assert_patch(show_live, ~p"/transactions/#{transaction}")

      html = render(show_live)
      assert html =~ "Transaction updated successfully"
      assert html =~ "some updated description"
    end
  end
end
