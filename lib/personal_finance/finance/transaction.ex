defmodule PersonalFinance.Finance.Transaction do
  import Ecto.Query, warn: false
  alias Ecto.Query
  use Ecto.Schema
  import Ecto.Changeset

  @type t :: %__MODULE__{}

  @primary_key {:id, UUIDv7, autogenerate: true}
  schema "transactions" do
    field :date, :date
    field :description, :string
    field :amount, :decimal
    field :unique_id, :string
    field :source, :string
    field :skip, :boolean, default: false

    many_to_many :categories, PersonalFinance.Finance.Category,
      join_through: "transactions_categories",
      join_keys: [transaction_id: :id, category_id: :id],
      on_delete: :delete_all,
      on_replace: :delete

    timestamps(type: :utc_datetime)
  end

  @doc false
  def changeset(transaction, attrs, categories \\ []) do
    transaction
    |> cast(attrs, [:date, :amount, :description, :unique_id, :source, :skip])
    |> validate_required([:date, :amount, :description, :unique_id, :source])
    |> unique_constraint(:unique_id)
    |> put_assoc(:categories, categories)
  end

  def assign_categories(transaction, categories) do
    %PersonalFinance.Finance.Transaction{transaction | categories: categories}
  end

  @spec assign_unique_id(PersonalFinance.Finance.Transaction.t()) ::
          PersonalFinance.Finance.Transaction.t()
  def assign_unique_id(transaction) do
    unique_string = "#{transaction.date}-#{transaction.amount}-#{transaction.description}"
    unique_id = :crypto.hash(:sha256, unique_string) |> Base.encode16(case: :lower)
    %PersonalFinance.Finance.Transaction{transaction | unique_id: unique_id}
  end

  @spec get_unique_id(PersonalFinance.Finance.Transaction.t()) :: String.t()
  def get_unique_id(transaction) do
    unique_string = "#{transaction.date}-#{transaction.amount}-#{transaction.description}"
    :crypto.hash(:sha256, unique_string) |> Base.encode16(case: :lower)
  end

  @spec toggle_skip(PersonalFinance.Finance.Transaction.t()) :: Ecto.Changeset.t()
  def toggle_skip(transaction) do
    changeset(transaction, %{skip: !transaction.skip})
  end

  def to_map(%PersonalFinance.Finance.Transaction{} = transaction) do
    %{
      id: transaction.id,
      date: transaction.date,
      description: transaction.description,
      source: transaction.source,
      amount: transaction.amount,
      categories: transaction.categories,
      unique_id: transaction.unique_id
    }
  end

  def by_month(query, %{month_year: ""}), do: query

  def by_month(query, %{month_year: month_year}) do
    start_date_month = Timex.parse("#{month_year}-01", "%Y-%m-%d")
    end_date_month = Timex.add(start_date_month, months: 1)

    Query.from(t in query,
      where: t.inserted_at >= ^start_date_month and t.inserted_at < ^end_date_month
    )
  end

  def by_skip(query, %{skipped_included: ""}), do: query

  def by_skip(query, %{skipped_included: skipped_included}) do
    Query.from(t in query,
      where: t.skip == ^skipped_included)
  end

  def by_source(query, %{source: ""}), do: query

  def by_source(query, %{source: source}) do
    Query.from(t in query, where: t.source == ^source)
  end
end
