defmodule PersonalFinance.Repo.Migrations.CreateAccounts do
  use Ecto.Migration

  def change do
    create table(:accounts) do
      add :source_type, :string
      add :file_content, :text
      add :status, :string

      timestamps(type: :utc_datetime)
    end
  end
end
