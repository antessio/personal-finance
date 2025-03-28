defmodule PersonalFinance.Finance.Category do
  use Ecto.Schema
  import Ecto.Changeset

  @type t :: %__MODULE__{}

  schema "categories" do
    field :name, :string
    field :macro_category, Ecto.Enum, values: PersonalFinance.Finance.MacroCategory.allowed_types()
    field :emoji, :string
    field :matchers, {:array, :string}

    timestamps(type: :utc_datetime)
  end

  @doc false
  def changeset(category, attrs) do
    category
    |> cast(attrs, [:name, :macro_category, :emoji, :matchers])
    |> validate_required([:name, :macro_category, :emoji, :matchers])
  end

  def from_map(attrs) do
    %PersonalFinance.Finance.Category{}
    |> cast(attrs, [:name, :macro_category, :emoji, :matchers])
  end
end
