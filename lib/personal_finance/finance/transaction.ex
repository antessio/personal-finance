defmodule PersonalFinance.Finance.Transaction do
  use Ecto.Schema
  import Ecto.Changeset

  @type t :: %__MODULE__{}

  @primary_key {:id, UUIDv7, autogenerate: true}
  schema "transactions" do
    field :date, :date
    field :description, :string
    field :amount, :decimal

    many_to_many :categories, PersonalFinance.Finance.Category,
      join_through: "transactions_categories",
      join_keys: [transaction_id: :id, category_id: :id],
      on_delete: :delete_all


    timestamps(type: :utc_datetime)
  end

  @doc false
  def changeset(transaction, attrs, categories \\ []) do
    transaction
    |> cast(attrs, [:date, :amount, :description])
    |> validate_required([:date, :amount, :description])
    |> put_assoc(:categories, categories)
  end

  def assign_categories(transaction, categories) do
    %PersonalFinance.Finance.Transaction{transaction | categories: categories}
  end

  def to_map(%PersonalFinance.Finance.Transaction{} = transaction) do
    %{
      id: transaction.id,
      date: transaction.date,
      description: transaction.description,
      amount: transaction.amount,
      categories: transaction.categories
    }
  end
end
