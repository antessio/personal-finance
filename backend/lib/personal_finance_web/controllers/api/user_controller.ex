defmodule PersonalFinanceWeb.API.UserController do
  use PersonalFinanceWeb, :controller

  alias PersonalFinance.Accounts

  action_fallback PersonalFinanceWeb.FallbackController

  def me(conn, _params) do
    user = conn.assigns.current_user
    render(conn, :show, user: user)
  end
end
