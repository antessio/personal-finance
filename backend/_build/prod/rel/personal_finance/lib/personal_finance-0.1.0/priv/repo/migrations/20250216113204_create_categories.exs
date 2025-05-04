defmodule PersonalFinance.Repo.Migrations.CreateCategories do
  use Ecto.Migration

  def change do
    create table(:categories) do
      add :name, :string
      add :macro_category, :string
      add :emoji, :string
      add :matchers, {:array, :string}

      timestamps(type: :utc_datetime)
    end
  end
end
