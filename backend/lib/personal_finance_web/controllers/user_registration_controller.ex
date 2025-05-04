defmodule PersonalFinanceWeb.UserRegistrationController do
  use PersonalFinanceWeb, :controller

  alias PersonalFinance.Accounts
  alias PersonalFinance.Accounts.User
  alias PersonalFinanceWeb.UserAuth

  def create(conn, %{"user" => user_params}) do
    case Accounts.register_user(user_params) do
      {:ok, user} ->
        {:ok, _} =
          Accounts.deliver_user_confirmation_instructions(
            user,
            &url(~p"/users/confirm/#{&1}")
          )

        conn
        |> put_status(:created)
        |> render(:show, user: user)

      {:error, %Ecto.Changeset{} = changeset} ->
        conn
        |> put_status(:unprocessable_entity)
        |> put_view(json: PersonalFinanceWeb.ChangesetJSON)
        |> render(:error, changeset: changeset)
    end
  end
end 