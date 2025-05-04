defmodule PersonalFinanceWeb.API.ErrorJSON do
  @moduledoc """
  This module is invoked by your endpoint in case of errors on JSON requests.

  See config/config.exs.
  """

  # If you want to customize a particular status code,
  # you may add your own clauses, such as:
  #
  # def render("500.json", _assigns) do
  #   %{errors: %{detail: "Internal Server Error"}}
  # end

  # By default, Phoenix returns the status message from
  # the template name. For example, "404.json" becomes
  # "Not Found".
  def render(template, _assigns) do
    %{errors: %{detail: Phoenix.Controller.status_message_from_template(template)}}
  end

  @doc """
  Renders changeset errors.
  """
  def error(%{status: status}) do
    %{errors: %{detail: status_message(status)}}
  end

  defp status_message(:not_found), do: "Not Found"
  defp status_message(:unauthorized), do: "Unauthorized"
  defp status_message(:forbidden), do: "Forbidden"
  defp status_message(:bad_request), do: "Bad Request"
  defp status_message(:internal_server_error), do: "Internal Server Error"
  defp status_message(_), do: "Unknown Error"
end
