defmodule PersonalFinance.ExternalAccountsFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `PersonalFinance.ExternalAccounts` context.
  """

  @doc """
  Generate a accounts.
  """
  def accounts_fixture(attrs \\ %{}) do
    {:ok, accounts} =
      attrs
      |> Enum.into(%{
        file_content: "some file_content",
        source_type: "some source_type",
        status: "some status"
      })
      |> PersonalFinance.ExternalAccounts.create_accounts()

    accounts
  end
end
