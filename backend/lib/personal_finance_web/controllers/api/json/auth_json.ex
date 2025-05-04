defmodule PersonalFinanceWeb.API.AuthJSON do
  alias PersonalFinance.Accounts.User

  def login(%{user: user, token: token}) do
    %{
      data: %{
        user: data(user),
        token: Base.url_encode64(token, padding: false)
      }
    }
  end

  def register(%{user: user}) do
    %{
      data: data(user)
    }
  end

  defp data(%User{} = user) do
    %{
      id: user.id,
      email: user.email,
      confirmed_at: user.confirmed_at
    }
  end
end
