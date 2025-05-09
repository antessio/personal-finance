defmodule PersonalFinance.ExternalAccounts do
  @moduledoc """
  The ExternalAccounts context.
  """

  import Ecto.Query, warn: false
  require Logger
  alias PersonalFinance.ExternalAccounts.SatispayAccountProcessor
  alias PersonalFinance.ExternalAccounts.PayPalAccountProcessor
  alias PersonalFinance.ExternalAccounts.IntesaAccountProcessor
  alias PersonalFinance.Finance.Transaction
  alias PersonalFinance.Finance
  alias PersonalFinance.ExternalAccounts.WidibaAccountProcessor
  alias PersonalFinance.Repo

  alias PersonalFinance.ExternalAccounts.Accounts

  defmodule CreateAccountCommand do
    use TypedStruct

    typedstruct do
      field :source_type, String.t()
      field :file_path, String.t()
    end
  end

  @doc """
  Returns the list of accounts.

  ## Examples

      iex> list_accounts()
      [%Accounts{}, ...]

  """
  def list_accounts do
    Repo.all(Accounts)
  end

  @doc """
  Gets a single accounts.

  Raises `Ecto.NoResultsError` if the Accounts does not exist.

  ## Examples

      iex> get_accounts!(123)
      %Accounts{}

      iex> get_accounts!(456)
      ** (Ecto.NoResultsError)

  """
  def get_accounts!(id), do: Repo.get!(Accounts, id)

  @doc """
  Creates a accounts.

  ## Examples

      iex> create_accounts(%{field: value})
      {:ok, %Accounts{}}

      iex> create_accounts(%{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def create_accounts(attrs \\ %{}) do
    %Accounts{}
    |> Accounts.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a accounts.

  ## Examples

      iex> update_accounts(accounts, %{field: new_value})
      {:ok, %Accounts{}}

      iex> update_accounts(accounts, %{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def update_accounts(%Accounts{} = accounts, attrs) do
    accounts
    |> Accounts.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Deletes a accounts.

  ## Examples

      iex> delete_accounts(accounts)
      {:ok, %Accounts{}}

      iex> delete_accounts(accounts)
      {:error, %Ecto.Changeset{}}

  """
  def delete_accounts(%Accounts{} = accounts) do
    Repo.delete(accounts)
  end

  @doc """
  Returns an `%Ecto.Changeset{}` for tracking accounts changes.

  ## Examples

      iex> change_accounts(accounts)
      %Ecto.Changeset{data: %Accounts{}}

  """
  def change_accounts(%Accounts{} = accounts, attrs \\ %{}) do
    Accounts.changeset(accounts, attrs)
  end

  @spec import_account(CreateAccountCommand.t()) :: {:ok, %Accounts{}} | {:error, any()}
  def import_account(%PersonalFinance.ExternalAccounts.CreateAccountCommand{
        source_type: source_type,
        file_path: file_path
      }) do
    %Accounts{}
    |> Accounts.changeset(%{
      source_type: source_type,
      file_path: file_path,
      status: "pending"
    })
    |> Repo.insert()
  end

  @account_processors [WidibaAccountProcessor, IntesaAccountProcessor, PayPalAccountProcessor, SatispayAccountProcessor]

  @spec process_account_import(id :: integer) ::
          {:ok, %Accounts{}, [Transaction.t()]} | {:error, %Accounts{}, [String.t()]}
  def process_account_import(id) do
    id
    |> get_accounts!()
    |> then(&process_account/1)
  end

  @spec process_account(%Accounts{}) ::
          {:ok, %Accounts{}, [String.t()]} | {:error, %Accounts{}, [String.t()]}
  defp process_account(%Accounts{} = account) do
    ## TODO: transactions and account update should be done in a transaction or using events
    @account_processors
    |> Enum.find(fn processor -> processor.can_process?(account) end)
    |> then(fn processor -> processor.process_account(account) end)
    |> then(fn result ->
      case result do
        {:ok, transactions} ->
          transactions |> Enum.map(&Transaction.to_map/1)

        {:skip} ->
          []

        {:error, error} ->
          Logger.error("Failed to process account: #{error}")
          []
      end
    end)
    |> then(fn transactions -> Finance.create_transactions(transactions) end)
    |> case do
      {:ok, transactions} -> {:ok, account |> update_accounts(%{status: "completed"}), transactions}
      {:error, error} -> {:error, account |> update_accounts(%{status: "error"}), error}
    end
  end


end
