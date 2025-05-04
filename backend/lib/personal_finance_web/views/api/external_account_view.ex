defmodule PersonalFinanceWeb.API.ExternalAccountView do
  use PersonalFinanceWeb, :view

  def render("index.json", %{accounts: accounts}) do
    %{data: render_many(accounts, __MODULE__, "account.json")}
  end

  def render("show.json", %{account: account}) do
    %{data: render_one(account, __MODULE__, "account.json")}
  end

  def render("account.json", %{external_account: account}) do
    %{
      id: account.id,
      name: account.name,
      provider: account.provider,
      last_synced_at: account.last_synced_at,
      user_id: account.user_id,
      inserted_at: account.inserted_at,
      updated_at: account.updated_at
    }
  end
end
