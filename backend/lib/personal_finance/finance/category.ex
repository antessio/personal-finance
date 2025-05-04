defmodule PersonalFinance.Finance.Category do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, UUIDv7, autogenerate: true}
  schema "categories" do
    field :name, :string
    field :type, Ecto.Enum, values: [:income, :expense, :bills, :savings, :subscriptions, :debts]
    belongs_to :user, PersonalFinance.Accounts.User

    many_to_many :transactions, PersonalFinance.Finance.Transaction,
      join_through: "transactions_categories",
      join_keys: [category_id: :id, transaction_id: :id],
      on_delete: :delete_all

    timestamps(type: :utc_datetime)
  end

  @doc false
  def changeset(category, attrs) do
    category
    |> cast(attrs, [:name, :type, :user_id])
    |> validate_required([:name, :type])
  end

  def from_map(attrs) do
    %PersonalFinance.Finance.Category{}
    |> cast(attrs, [:name, :type, :user_id])
  end
end
