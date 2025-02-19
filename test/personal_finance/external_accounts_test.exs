defmodule PersonalFinance.ExternalAccountsTest do
  use PersonalFinance.DataCase

  alias PersonalFinance.ExternalAccounts

  describe "accounts" do
    alias PersonalFinance.ExternalAccounts.Accounts

    import PersonalFinance.ExternalAccountsFixtures

    @invalid_attrs %{status: nil, source_type: nil, file_content: nil}

    test "list_accounts/0 returns all accounts" do
      accounts = accounts_fixture()
      assert ExternalAccounts.list_accounts() == [accounts]
    end

    test "get_accounts!/1 returns the accounts with given id" do
      accounts = accounts_fixture()
      assert ExternalAccounts.get_accounts!(accounts.id) == accounts
    end

    test "create_accounts/1 with valid data creates a accounts" do
      valid_attrs = %{status: "some status", source_type: "some source_type", file_content: "some file_content"}

      assert {:ok, %Accounts{} = accounts} = ExternalAccounts.create_accounts(valid_attrs)
      assert accounts.status == "some status"
      assert accounts.source_type == "some source_type"
      assert accounts.file_content == "some file_content"
    end

    test "create_accounts/1 with invalid data returns error changeset" do
      assert {:error, %Ecto.Changeset{}} = ExternalAccounts.create_accounts(@invalid_attrs)
    end

    test "update_accounts/2 with valid data updates the accounts" do
      accounts = accounts_fixture()
      update_attrs = %{status: "some updated status", source_type: "some updated source_type", file_content: "some updated file_content"}

      assert {:ok, %Accounts{} = accounts} = ExternalAccounts.update_accounts(accounts, update_attrs)
      assert accounts.status == "some updated status"
      assert accounts.source_type == "some updated source_type"
      assert accounts.file_content == "some updated file_content"
    end

    test "update_accounts/2 with invalid data returns error changeset" do
      accounts = accounts_fixture()
      assert {:error, %Ecto.Changeset{}} = ExternalAccounts.update_accounts(accounts, @invalid_attrs)
      assert accounts == ExternalAccounts.get_accounts!(accounts.id)
    end

    test "delete_accounts/1 deletes the accounts" do
      accounts = accounts_fixture()
      assert {:ok, %Accounts{}} = ExternalAccounts.delete_accounts(accounts)
      assert_raise Ecto.NoResultsError, fn -> ExternalAccounts.get_accounts!(accounts.id) end
    end

    test "change_accounts/1 returns a accounts changeset" do
      accounts = accounts_fixture()
      assert %Ecto.Changeset{} = ExternalAccounts.change_accounts(accounts)
    end
  end
end
