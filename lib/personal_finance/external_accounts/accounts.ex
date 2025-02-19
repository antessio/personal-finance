defmodule PersonalFinance.ExternalAccounts.Accounts do
  use Ecto.Schema
  import Ecto.Changeset

  @source_types [:widiba, :satispay, :intesa, :paypal]

  schema "accounts" do
    field :status, :string
    field :source_type, :string
    field :file_content, :string

    timestamps(type: :utc_datetime)
  end

  @doc false
  def changeset(accounts, attrs) do
    accounts
    |> cast(attrs, [:source_type, :file_content, :status])
    |> validate_required([:source_type, :file_content, :status])
  end

  def source_types(), do: @source_types

end
