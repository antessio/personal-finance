defmodule PersonalFinance.Repo.Migrations.CreateTransactions do
  use Ecto.Migration

  def change do
    create table(:transactions, primary_key: false) do
      add :id, :uuid, primary_key: true
      add :date, :date
      add :amount, :decimal
      add :description, :string

      timestamps(type: :utc_datetime)
    end
  end
end
