defmodule PersonalFinance.FinanceFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `PersonalFinance.Finance` context.
  """

  @doc """
  Generate a category.
  """
  def category_fixture(attrs \\ %{}) do
    {:ok, category} =
      attrs
      |> Enum.into(%{
        emoji: "some emoji",
        macro_category: "some macro_category",
        matchers: ["option1", "option2"],
        name: "some name"
      })
      |> PersonalFinance.Finance.create_category()

    category
  end

  @doc """
  Generate a transaction.
  """
  def transaction_fixture(attrs \\ %{}) do
    {:ok, transaction} =
      attrs
      |> Enum.into(%{
        amount: "120.5",
        date: ~D[2025-02-15],
        description: "some description"
      })
      |> PersonalFinance.Finance.create_transaction()

    transaction
  end
end
