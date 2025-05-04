defmodule PersonalFinance.Repo.Migrations.AddTransactionSource do
  use Ecto.Migration

  def change do
    alter table(:transactions) do
      add :source, :string
    end
  end
end
