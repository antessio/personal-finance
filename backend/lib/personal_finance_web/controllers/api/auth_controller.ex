defmodule PersonalFinanceWeb.API.AuthController do
  use PersonalFinanceWeb, :controller

  alias PersonalFinance.Accounts
  alias PersonalFinanceWeb.API.AuthJSON

  action_fallback PersonalFinanceWeb.FallbackController

  def login(conn, %{"user" => %{"email" => email, "password" => password}}) do
    if user = Accounts.get_user_by_email_and_password(email, password) do
      token = Accounts.generate_user_session_token(user)
      conn = put_session(conn, :user_token, token)

      conn
      |> put_status(:ok)
      |> render(:login, user: user, token: token)
    else
      conn
      |> put_status(:unauthorized)
      |> put_view(json: PersonalFinanceWeb.ErrorJSON)
      |> render(:"401")
    end
  end

  def register(conn, %{"user" => user_params}) do
    case Accounts.register_user(user_params) do
      {:ok, user} ->
        {:ok, _} =
          Accounts.deliver_user_confirmation_instructions(
            user,
            &url(~p"/users/confirm/#{&1}")
          )

        conn
        |> put_status(:created)
        |> render(:register, user: user)

      {:error, %Ecto.Changeset{} = changeset} ->
        conn
        |> put_status(:unprocessable_entity)
        |> put_view(json: PersonalFinanceWeb.ChangesetJSON)
        |> render(:error, changeset: changeset)
    end
  end
end
