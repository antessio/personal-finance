defmodule PersonalFinanceWeb.UserForgotPasswordController do
  use PersonalFinanceWeb, :controller

  alias PersonalFinance.Accounts

  def create(conn, %{"user" => %{"email" => email}}) do
    if user = Accounts.get_user_by_email(email) do
      {:ok, _} =
        Accounts.deliver_user_reset_password_instructions(
          user,
          &url(~p"/users/reset_password/#{&1}")
        )
    end

    # Always return a 202 to prevent email enumeration
    send_resp(conn, :accepted, "")
  end
end 