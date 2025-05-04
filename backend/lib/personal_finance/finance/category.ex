defmodule PersonalFinance.Finance.Category do
  use Ecto.Schema
  import Ecto.Changeset

  @type t :: %__MODULE__{}

  @primary_key {:id, :id, autogenerate: true}
  schema "categories" do
    field :name, :string
    field :macro_category, Ecto.Enum, values: PersonalFinance.Finance.MacroCategory.allowed_types()
    field :emoji, :string
    field :matchers, {:array, :string}
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
    |> cast(attrs, [:name, :macro_category, :emoji, :matchers, :user_id])
    |> validate_required([:name, :macro_category, :emoji, :matchers])
  end

  def from_map(attrs) do
    %PersonalFinance.Finance.Category{}
    |> cast(attrs, [:name, :macro_category, :emoji, :matchers, :user_id])
  end
end
