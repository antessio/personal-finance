defmodule PersonalFinance.Finance do
  @moduledoc """
  The Finance context.
  """

  import Ecto.Query, warn: false
  alias Ecto.Multi
  alias PersonalFinance.Finance.MacroCategory
  alias PersonalFinance.Repo

  alias PersonalFinance.Finance.Category

  @doc """
  Returns the list of categories.

  ## Examples

      iex> list_categories()
      [%Category{}, ...]

  """
  def list_categories do
    Repo.all(Category)
  end

  @doc """
  Gets a single category.

  Raises `Ecto.NoResultsError` if the Category does not exist.

  ## Examples

      iex> get_category!(123)
      %Category{}

      iex> get_category!(456)
      ** (Ecto.NoResultsError)

  """
  def get_category!(id), do: Repo.get!(Category, id)

  @doc """
  Creates a category.

  ## Examples

      iex> create_category(%{field: value})
      {:ok, %Category{}}

      iex> create_category(%{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def create_category(attrs \\ %{}) do
    %Category{}
    |> Category.changeset(attrs)
    |> Repo.insert(on_conflict: :replace_all, conflict_target: [:unique_id])
  end

  @doc """
  Updates a category.

  ## Examples

      iex> update_category(category, %{field: new_value})
      {:ok, %Category{}}

      iex> update_category(category, %{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def update_category(%Category{} = category, attrs) do
    category
    |> Category.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Deletes a category.

  ## Examples

      iex> delete_category(category)
      {:ok, %Category{}}

      iex> delete_category(category)
      {:error, %Ecto.Changeset{}}

  """
  def delete_category(%Category{} = category) do
    Repo.delete(category)
  end

  @doc """
  Returns an `%Ecto.Changeset{}` for tracking category changes.

  ## Examples

      iex> change_category(category)
      %Ecto.Changeset{data: %Category{}}

  """
  def change_category(%Category{} = category, attrs \\ %{}) do
    Category.changeset(category, attrs)
  end

  @spec get_macro_categories() :: [PersonalFinance.Finance.MacroCategory.t()]
  def get_macro_categories() do
    [
      %MacroCategory{type: :income, name: "Income"},
      %MacroCategory{type: :expense, name: "Expense"},
      %MacroCategory{type: :bills, name: "Bills"},
      %MacroCategory{type: :savings, name: "Savings"},
      %MacroCategory{type: :subscriptions, name: "Subscriptions"},
      %MacroCategory{type: :debts, name: "Debts"}
    ]
  end

  alias PersonalFinance.Finance.Transaction

  @doc """
  Returns the list of transactions.

  ## Examples

      iex> list_transactions()
      [%Transaction{}, ...]

  """
  def list_transactions do
    Repo.all(Transaction)
    |> Repo.preload(:categories)
  end

  @doc """
  Gets a single transaction.

  Raises `Ecto.NoResultsError` if the Transaction does not exist.

  ## Examples

      iex> get_transaction!(123)
      %Transaction{}

      iex> get_transaction!(456)
      ** (Ecto.NoResultsError)

  """
  def get_transaction!(id), do: Repo.get!(Transaction, id) |> Repo.preload(:categories)

  @spec create_transaction(map()) :: {:ok, %Transaction{}} | {:error, %Ecto.Changeset{}}
  def create_transaction(attrs \\ %{}) do
    categories =
      attrs
      |> Map.get("categories", [])
      |> Enum.map(& &1.id)
      |> then(&Repo.all(from category in Category, where: category.id in ^&1))

    unique_id = Transaction.get_unique_id(attrs)
    attrs = Map.put(attrs, :unique_id, unique_id)

    %Transaction{}
    |> Transaction.changeset(attrs, categories)
    |> Repo.insert(on_conflict: :replace_all, conflict_target: [:unique_id])
  end

  @spec create_transactions([map()]) :: [{:ok, %Transaction{}}] | [{:error, %Ecto.Changeset{}}]
  def create_transactions(attrs_list \\ []) do
    categories_by_id =
      attrs_list
      |> Enum.flat_map(&Map.get(&1, "categories", []))
      |> Enum.uniq_by(& &1.id)
      |> then(&Repo.all(from category in Category, where: category.id in ^&1))
      |> Enum.group_by(& &1.id)

    multi =
      Enum.reduce(attrs_list, Multi.new(), fn attrs, multi ->
        unique_id = Transaction.get_unique_id(attrs)
        attrs = Map.put(attrs, :unique_id, unique_id)

        categories =
          attrs
          |> Map.get("categories", [])
          |> Enum.map(& &1.id)
          |> Enum.map(&Map.get(categories_by_id, &1, []))
          |> List.flatten()

        %Transaction{}
        |> Transaction.changeset(attrs, categories)
        |> then(&Multi.insert(multi, Ecto.UUID.generate(), &1))
      end)

    case Repo.transaction(multi) do
      {:ok, result} ->
        transactions = result |> Map.values() |> dbg()
        {:ok, transactions}

      {:error, _operation, changeset, _changes} ->
        {:error, changeset}
    end
  end

  @doc """
  Updates a transaction.

  ## Examples

      iex> update_transaction(transaction, %{field: new_value})
      {:ok, %Transaction{}}

      iex> update_transaction(transaction, %{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def update_transaction(%Transaction{} = transaction, attrs) do
    categories =
      attrs
      |> Map.get("categories", [])
      |> Enum.map(& &1.id)
      |> then(&Repo.all(from category in Category, where: category.id in ^&1))

    transaction
    |> Transaction.assign_unique_id()
    |> Transaction.changeset(attrs, categories)
    |> Repo.update()
  end

  @doc """
  Deletes a transaction.

  ## Examples

      iex> delete_transaction(transaction)
      {:ok, %Transaction{}}

      iex> delete_transaction(transaction)
      {:error, %Ecto.Changeset{}}

  """
  def delete_transaction(%Transaction{} = transaction) do
    Repo.delete(transaction)
  end

  @doc """
  Returns an `%Ecto.Changeset{}` for tracking transaction changes.

  ## Examples

      iex> change_transaction(transaction)
      %Ecto.Changeset{data: %Transaction{}}

  """
  def change_transaction(%Transaction{} = transaction, attrs \\ %{}) do
    Transaction.changeset(transaction, attrs)
  end

  def toggle_skip_transaction!(id) do
    {:ok, transaction} =
      id
      |> get_transaction!()
      |> Transaction.toggle_skip()
      |> Repo.update()

    transaction
  end
end
