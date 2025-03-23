defmodule PersonalFinance.Finance.Transaction do
  import Ecto.Query, warn: false
  alias Ecto.Query
  use Ecto.Schema
  import Ecto.Changeset

  @type t :: %__MODULE__{}

  @required_fields [:date, :amount, :description, :unique_id, :source]

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
    |> validate_required(@required_fields)
    |> unique_constraint(:unique_id)
    |> put_assoc(:categories, categories)
  end

  def changeset_update_categories(transaction, []) do
    transaction
    |> cast(%{}, @required_fields)
    |> put_assoc(:categories, transaction.categories)
  end

  def changeset_update_categories(transaction, categories) do
    transaction
    |> cast(%{}, @required_fields)
    |> put_assoc(:categories, categories)
  end

  def assign_categories(transaction, []) do
    transaction
  end

  def assign_categories(transaction, categories) do
    %PersonalFinance.Finance.Transaction{transaction | categories: merge_categories(transaction.categories, categories)}
  end


  @spec merge_categories([PersonalFinance.Finance.Category.t()], [PersonalFinance.Finance.Category.t()]) ::
          [PersonalFinance.Finance.Category.t()]
  defp merge_categories(existing_categories, new_categories) do
    Enum.uniq(existing_categories ++ new_categories)
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
    {:ok, start_date_month } = Timex.parse("#{month_year}-01", "{YYYY}-{0M}-{D}")
    end_date_month = Timex.shift(start_date_month, months: 1)

    Query.from(t in query,
      where: t.date >= ^start_date_month and t.date < ^end_date_month
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
  def by_category(query, %{category_ids: nil}), do: query
  def by_category(query, %{category_ids: [""]}) do
    Query.from(t in query,
      left_join: c in assoc(t, :categories),
      where: is_nil(c.id)
    )
  end
  def by_category(query, %{category_ids: category_ids}) do
    Query.from(t in query,
      join: c in assoc(t, :categories),
      where: c.id in ^category_ids
    )
  end

  def by_ids(query, transaction_ids) do
    Query.from(t in query,
      where: t.id in ^transaction_ids
    )
  end
end
