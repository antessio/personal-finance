defmodule PersonalFinance.Finance do
  @moduledoc """
  The Finance context.
  """

  import Ecto.Query, warn: false
  alias PersonalFinance.ExternalAccounts.TransactionsCategorization
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
    |> Repo.insert()
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
      %MacroCategory{type: :expense, name: "Expense Var."},
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

  def list_transactions(%{
        month_year: month_year,
        skipped_included: skipped_included,
        source: source,
        categories: category_ids
      }) do
    query =
      Transaction
      |> Transaction.by_month(%{month_year: month_year})
      |> Transaction.by_skip(%{skipped_included: skipped_included})
      |> Transaction.by_source(%{source: source})
      |> Transaction.by_category(%{category_ids: category_ids})

    Repo.all(query)
    |> Repo.preload(:categories)
  end
  def list_transactions(%{}), do: list_transactions()

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

  @doc """
  Returns the list of transactions for a user.

  ## Examples

      iex> list_user_transactions(user)
      [%Transaction{}, ...]

  """
  def list_user_transactions(user) do
    Transaction
    |> where([t], t.user_id == ^user.id)
    |> Repo.all()
  end

  @doc """
  Gets a single transaction for a user.

  Raises `Ecto.NoResultsError` if the Transaction does not exist.

  ## Examples

      iex> get_user_transaction!(user, 123)
      %Transaction{}

      iex> get_user_transaction!(user, 456)
      ** (Ecto.NoResultsError)

  """
  def get_user_transaction!(user, id) do
    Transaction
    |> where([t], t.user_id == ^user.id)
    |> Repo.get!(id)
  end

  @doc """
  Creates a transaction.

  ## Examples

      iex> create_transaction(%{field: value})
      {:ok, %Transaction{}}

      iex> create_transaction(%{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def create_transaction(attrs \\ %{}) do
    %Transaction{}
    |> Transaction.changeset(attrs)
    |> Repo.insert()
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
    transaction
    |> Transaction.changeset(attrs)
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

  def process_categories(transaction) do
    categories = TransactionsCategorization.get_categories_matching(transaction)
    internal_process_categories(transaction, categories)
  end
  defp internal_process_categories(transaction, []) do
    transaction
  end

  defp internal_process_categories(transaction, categories) do
    transaction
    |> Transaction.changeset_update_categories(categories)
    |> Repo.update()
  end

  @spec bulk_update_transactions_categories([String.t()], [PersonalFinance.Finance.Category.t()]) ::
          {:ok, [Transaction.t()]} | {:error, any()}
  def bulk_update_transactions_categories(transaction_ids, categories) do
    # Transaction
    # |> Transaction.by_ids(transaction_ids)
    # |> Repo.all()
    # |> Repo.preload(:categories)
    # |> Enum.map(fn transaction ->
    #   transaction
    #   |> Transaction.changeset_update_categories(categories)
    #   |> Repo.update()
    # end)

    transactions =
      Transaction
      |> Transaction.by_ids(transaction_ids)
      |> Repo.all()
      |> Repo.preload(:categories)

    multi =
      Enum.reduce(transactions, Ecto.Multi.new(), fn transaction, multi ->
        changeset =
          transaction
          |> Transaction.changeset_update_categories(categories)

        Ecto.Multi.update(multi, {:transaction, transaction.id}, changeset)
      end)

    case Repo.transaction(multi) do
      {:ok, result} ->
        updated_transactions = result |> Map.values()
        {:ok, updated_transactions}

      {:error, _operation, changeset, _changes} ->
        {:error, changeset}
    end
    # |> dbg()

    # Ecto.Multi.new()
    # |> Ecto.Multi.run(:fetch_transactions, fn repo, _changes ->
    #   transactions =
    #     Transaction
    #     |> Transaction.by_ids(transaction_ids)
    #     |> repo.all()
    #     |> repo.preload(:categories)

    #   {:ok, transactions}
    # end)
    # |> Ecto.Multi.run(:update_categories, fn repo, %{fetch_transactions: transactions} ->
    #   updated_transactions =
    #     Enum.map(transactions, fn transaction ->
    #       transaction
    #       |> Transaction.assign_categories(categories)
    #       |> then(&Transaction.changeset(&1, %{}, &1.categories))
    #       |> repo.update()
    #     end)

    #   # Check if all updates were successful
    #   if Enum.all?(updated_transactions, &match?({:ok, _}, &1)) do
    #     {:ok, updated_transactions}
    #   else
    #     {:error, :update_failed}
    #   end
    # end)
    # |> Repo.transaction()

    # :ok
  end
end
