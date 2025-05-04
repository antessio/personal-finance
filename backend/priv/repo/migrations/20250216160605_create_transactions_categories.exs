defmodule PersonalFinance.Repo.Migrations.CreateTransactionsCategories do
  use Ecto.Migration

  def change do
    create table(:transactions_categories, primary_key: false) do
      add :transaction_id, references(:transactions, type: :binary_id, on_delete: :delete_all)
      add :category_id, references(:categories, type: :id, on_delete: :delete_all)
    end

    create unique_index(:transactions_categories, [:transaction_id, :category_id])
  end
end
