defmodule PersonalFinanceWeb.TransactionLive.FormComponent do
  use PersonalFinanceWeb, :live_component

  alias PersonalFinance.Finance

  @impl true
  def render(assigns) do
    ~H"""
    <div>
      <.header>
        {@title}
        <:subtitle>Use this form to manage transaction records in your database.</:subtitle>
      </.header>

      <.simple_form
        for={@form}
        id="transaction-form"
        phx-target={@myself}
        phx-change="validate"
        phx-submit="save"
      >
        <.input field={@form[:date]} type="date" label="Date" />
        <.input field={@form[:amount]} type="number" label="Amount" step="any" />
        <.input field={@form[:description]} type="text" label="Description" />

        <div id="categories-fields">
          <label class="block mb-1 font-semibold">Categories</label>
          <div class="flex flex-wrap gap-2 mb-2">
            <%= for {category, index} <- Enum.with_index(@selected_categories || []) do %>
              <div class="flex items-center bg-blue-100 text-blue-700 px-3 py-1 rounded-lg">
                <span>{category.name}</span>
                <button
                  type="button"
                  phx-click="remove_category_transaction"
                  phx-value-category_id={category.id}
                  class="ml-2 text-red-500 hover:text-red-700"
                >
                  Remove
                </button>
              </div>
            <% end %>
          </div>
          <label class="block mb-1 font-semibold">Available Categories</label>
          <div class="flex gap-2">
            <div class="flex flex-wrap gap-2">
              <%= for category <- @categories do %>
                <button
                  type="button"
                  disabled={Enum.member?(@selected_categories, category)}
                  phx-click="add_category_transaction"
                  phx-value-category_id={category.id}
                  class="bg-gray-100 text-gray-700 px-3 py-1 rounded-full hover:bg-gray-200"
                >
                  {category.name}
                </button>
              <% end %>
            </div>
          </div>
        </div>
        <:actions>
          <.button phx-disable-with="Saving...">Save Transaction</.button>
        </:actions>
      </.simple_form>
    </div>
    """
  end

  @impl true
  def update(%{transaction: transaction} = assigns, socket) do
    {:ok,
     socket
     |> assign(assigns)
     |> assign_new(:form, fn ->
       to_form(Finance.change_transaction(transaction))
     end)}
  end

  @impl true
  def handle_event("validate", %{"transaction" => transaction_params}, socket) do
    changeset = Finance.change_transaction(socket.assigns.transaction, transaction_params)
    {:noreply, assign(socket, form: to_form(changeset, action: :validate))}
  end

  def handle_event("save", %{"transaction" => transaction_params}, socket) do
    save_transaction(socket, socket.assigns.action, transaction_params)
  end

  defp save_transaction(socket, :edit, transaction_params) do
    IO.inspect(transaction_params, label: "transaction params")

    transaction_params = socket.assigns.selected_categories
    |> Enum.map(& Map.from_struct(&1))
    |> then(& Map.put_new(transaction_params, "categories", &1))

    case Finance.update_transaction(socket.assigns.transaction, transaction_params) do
      {:ok, transaction} ->
        notify_parent({:saved, transaction})

        {:noreply,
         socket
         |> put_flash(:info, "Transaction updated successfully")
         |> push_patch(to: socket.assigns.patch)}

      {:error, %Ecto.Changeset{} = changeset} ->
        {:noreply, assign(socket, form: to_form(changeset))}
    end
  end

  defp save_transaction(socket, :new, transaction_params) do

    transaction_params = socket.assigns.selected_categories
    |> Enum.map(& Map.from_struct(&1))
    |> then(& Map.put_new(transaction_params, "categories", &1))
    |> dbg()

    case Finance.create_transaction(transaction_params)|> dbg() do
      {:ok, transaction} ->
        notify_parent({:saved, transaction})

        {:noreply,
         socket
         |> put_flash(:info, "Transaction created successfully")
         |> push_patch(to: socket.assigns.patch)}

      {:error, %Ecto.Changeset{} = changeset} ->
        {:noreply, assign(socket, form: to_form(changeset))}
    end
  end

  defp notify_parent(msg), do: send(self(), {__MODULE__, msg})
end
