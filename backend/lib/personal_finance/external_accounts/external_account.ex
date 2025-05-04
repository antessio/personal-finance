defmodule PersonalFinance.ExternalAccounts.ExternalAccount do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, UUIDv7, autogenerate: true}
  schema "external_accounts" do
    field :name, :string
    field :type, :string
    belongs_to :user, PersonalFinance.Accounts.User

    timestamps(type: :utc_datetime)
  end

  @doc false
  def changeset(external_account, attrs) do
    external_account
    |> cast(attrs, [:name, :type, :user_id])
    |> validate_required([:name, :type])
  end
end
