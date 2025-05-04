defmodule PersonalFinance.Repo.Migrations.AddUserIdToTransactions do
  use Ecto.Migration

  def change do
    alter table(:transactions) do
      add :user_id, references(:users, type: :id, on_delete: :delete_all), null: true
    end

    create index(:transactions, [:user_id])
  end

  def down do
    alter table(:transactions) do
      remove :user_id
    end
  end
end
