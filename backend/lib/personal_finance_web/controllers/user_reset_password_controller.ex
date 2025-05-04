defmodule PersonalFinanceWeb.UserResetPasswordController do
  use PersonalFinanceWeb, :controller

  alias PersonalFinance.Accounts

  def update(conn, %{"user" => user_params, "token" => token}) do
    case Accounts.reset_user_password(token, user_params) do
      {:ok, user} ->
        conn
        |> put_status(:ok)
        |> render(:show, user: user)

      {:error, %Ecto.Changeset{} = changeset} ->
        conn
        |> put_status(:unprocessable_entity)
        |> put_view(json: PersonalFinanceWeb.ChangesetJSON)
        |> render(:error, changeset: changeset)
    end
  end
end 