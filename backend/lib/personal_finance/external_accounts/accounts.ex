defmodule PersonalFinance.ExternalAccounts.Accounts do
  use Ecto.Schema
  import Ecto.Changeset

  @source_types [:widiba, :satispay, :intesa, :paypal]

  schema "accounts" do
    field :status, :string
    field :source_type, :string
    field :file_path, :string

    timestamps(type: :utc_datetime)
  end

  @doc false
  def changeset(accounts, attrs) do
    accounts
    |> cast(attrs, [:source_type, :file_path, :status])
    |> validate_required([:source_type, :file_path, :status])
  end

  def source_types(), do: @source_types

end
