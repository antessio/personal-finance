defmodule PersonalFinanceWeb.API.PasswordController do
  use PersonalFinanceWeb, :controller

  alias PersonalFinance.Accounts

  action_fallback PersonalFinanceWeb.FallbackController

  def forgot(conn, %{"user" => %{"email" => email}}) do
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

  def reset(conn, %{"user" => user_params, "token" => token}) do
    case Accounts.reset_user_password(token, user_params) do
      {:ok, user} ->
        conn
        |> put_status(:ok)
        |> render(:reset, user: user)

      {:error, %Ecto.Changeset{} = changeset} ->
        conn
        |> put_status(:unprocessable_entity)
        |> put_view(json: PersonalFinanceWeb.API.ChangesetJSON)
        |> render(:error, changeset: changeset)
    end
  end
end
