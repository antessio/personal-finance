defmodule PersonalFinanceWeb.AccountsLiveTest do
  use PersonalFinanceWeb.ConnCase

  import Phoenix.LiveViewTest
  import PersonalFinance.ExternalAccountsFixtures

  @create_attrs %{status: "some status", source_type: "some source_type", file_content: "some file_content"}
  @update_attrs %{status: "some updated status", source_type: "some updated source_type", file_content: "some updated file_content"}
  @invalid_attrs %{status: nil, source_type: nil, file_content: nil}

  defp create_accounts(_) do
    accounts = accounts_fixture()
    %{accounts: accounts}
  end

  describe "Index" do
    setup [:create_accounts]

    test "lists all accounts", %{conn: conn, accounts: accounts} do
      {:ok, _index_live, html} = live(conn, ~p"/accounts")

      assert html =~ "Listing Accounts"
      assert html =~ accounts.status
    end

    test "saves new accounts", %{conn: conn} do
      {:ok, index_live, _html} = live(conn, ~p"/accounts")

      assert index_live |> element("a", "New Accounts") |> render_click() =~
               "New Accounts"

      assert_patch(index_live, ~p"/accounts/new")

      assert index_live
             |> form("#accounts-form", accounts: @invalid_attrs)
             |> render_change() =~ "can&#39;t be blank"

      assert index_live
             |> form("#accounts-form", accounts: @create_attrs)
             |> render_submit()

      assert_patch(index_live, ~p"/accounts")

      html = render(index_live)
      assert html =~ "Accounts created successfully"
      assert html =~ "some status"
    end

    test "updates accounts in listing", %{conn: conn, accounts: accounts} do
      {:ok, index_live, _html} = live(conn, ~p"/accounts")

      assert index_live |> element("#accounts-#{accounts.id} a", "Edit") |> render_click() =~
               "Edit Accounts"

      assert_patch(index_live, ~p"/accounts/#{accounts}/edit")

      assert index_live
             |> form("#accounts-form", accounts: @invalid_attrs)
             |> render_change() =~ "can&#39;t be blank"

      assert index_live
             |> form("#accounts-form", accounts: @update_attrs)
             |> render_submit()

      assert_patch(index_live, ~p"/accounts")

      html = render(index_live)
      assert html =~ "Accounts updated successfully"
      assert html =~ "some updated status"
    end

    test "deletes accounts in listing", %{conn: conn, accounts: accounts} do
      {:ok, index_live, _html} = live(conn, ~p"/accounts")

      assert index_live |> element("#accounts-#{accounts.id} a", "Delete") |> render_click()
      refute has_element?(index_live, "#accounts-#{accounts.id}")
    end
  end

  describe "Show" do
    setup [:create_accounts]

    test "displays accounts", %{conn: conn, accounts: accounts} do
      {:ok, _show_live, html} = live(conn, ~p"/accounts/#{accounts}")

      assert html =~ "Show Accounts"
      assert html =~ accounts.status
    end

    test "updates accounts within modal", %{conn: conn, accounts: accounts} do
      {:ok, show_live, _html} = live(conn, ~p"/accounts/#{accounts}")

      assert show_live |> element("a", "Edit") |> render_click() =~
               "Edit Accounts"

      assert_patch(show_live, ~p"/accounts/#{accounts}/show/edit")

      assert show_live
             |> form("#accounts-form", accounts: @invalid_attrs)
             |> render_change() =~ "can&#39;t be blank"

      assert show_live
             |> form("#accounts-form", accounts: @update_attrs)
             |> render_submit()

      assert_patch(show_live, ~p"/accounts/#{accounts}")

      html = render(show_live)
      assert html =~ "Accounts updated successfully"
      assert html =~ "some updated status"
    end
  end
end
