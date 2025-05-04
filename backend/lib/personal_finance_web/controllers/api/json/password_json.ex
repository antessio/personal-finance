defmodule PersonalFinanceWeb.API.PasswordJSON do
  alias PersonalFinance.Accounts.User

  def reset(%{user: user}) do
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
