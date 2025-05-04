defmodule PersonalFinance.Repo.Migrations.AddSkipTransaction do
  use Ecto.Migration

  def change do
    alter table(:transactions) do
      add :skip, :boolean, default: false
    end
  end
end
