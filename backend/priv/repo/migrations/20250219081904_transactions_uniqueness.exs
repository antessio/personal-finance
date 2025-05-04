defmodule PersonalFinance.Repo.Migrations.TransactionsUniqueness do
  use Ecto.Migration

  def change do
    # add column to transactions
    alter table(:transactions) do
      add :unique_id, :string
    end

    create index(:transactions, [:unique_id], unique: true)

  end
end
