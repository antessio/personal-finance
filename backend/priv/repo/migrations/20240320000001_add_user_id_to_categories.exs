defmodule PersonalFinance.Repo.Migrations.AddUserIdToCategories do
  use Ecto.Migration

  def change do
    alter table(:categories) do
      add :user_id, references(:users, type: :id, on_delete: :delete_all), null: true
    end

    create index(:categories, [:user_id])
  end
end
