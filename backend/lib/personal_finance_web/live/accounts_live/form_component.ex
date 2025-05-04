defmodule PersonalFinanceWeb.AccountsLive.FormComponent do
alias PersonalFinance.Finance
  use PersonalFinanceWeb, :live_component

  alias PersonalFinance.ExternalAccounts

  @impl true
  def render(assigns) do
    ~H"""
    <div>
      <.header>
        {@title}
        <:subtitle>Use this form to manage accounts records in your database.</:subtitle>
      </.header>

      <.simple_form
        for={@form}
        id="accounts-form"
        phx-target={@myself}
        phx-change="validate"
        phx-submit="save"
      >
        <.input field={@form[:source_type]} type="text" label="Source type" />
        <.input field={@form[:file_path]} type="text" label="File path" />
        <.input field={@form[:status]} type="text" label="Status" />
        <:actions>
          <.button phx-disable-with="Saving...">Save Accounts</.button>
        </:actions>
      </.simple_form>
    </div>
    """
  end

  @impl true
  def update(%{accounts: accounts} = assigns, socket) do
    {:ok,
     socket
     |> assign(assigns)
     |> assign_new(:form, fn ->
       to_form(ExternalAccounts.change_accounts(accounts))
     end)}
  end

  @impl true
  def handle_event("validate", %{"accounts" => accounts_params}, socket) do
    changeset = ExternalAccounts.change_accounts(socket.assigns.accounts, accounts_params)
    {:noreply, assign(socket, form: to_form(changeset, action: :validate))}
  end

  def handle_event("save", %{"accounts" => accounts_params}, socket) do
    save_accounts(socket, socket.assigns.action, accounts_params)
  end


  defp save_accounts(socket, :edit, accounts_params) do
    case ExternalAccounts.update_accounts(socket.assigns.accounts, accounts_params) do
      {:ok, accounts} ->
        notify_parent({:saved, accounts})

        {:noreply,
         socket
         |> put_flash(:info, "Accounts updated successfully")
         |> push_patch(to: socket.assigns.patch)}

      {:error, %Ecto.Changeset{} = changeset} ->
        {:noreply, assign(socket, form: to_form(changeset))}
    end
  end

  defp save_accounts(socket, :new, accounts_params) do
    case ExternalAccounts.create_accounts(accounts_params) do
      {:ok, accounts} ->
        notify_parent({:saved, accounts})

        {:noreply,
         socket
         |> put_flash(:info, "Accounts created successfully")
         |> push_patch(to: socket.assigns.patch)}

      {:error, %Ecto.Changeset{} = changeset} ->
        {:noreply, assign(socket, form: to_form(changeset))}
    end
  end

  defp notify_parent(msg), do: send(self(), {__MODULE__, msg})
end
