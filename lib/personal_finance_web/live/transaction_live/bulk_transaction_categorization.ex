defmodule PersonalFinanceWeb.TransactionLive.FormBulkTransactionCategorization do
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

      <.simple_form for={@form} id="transaction-form" phx-target={@myself} phx-submit="save">
        <%!-- <%= for transaction_id <- @form["selected_transactions"] do %>
          <div>
            {transaction_id}
          </div>
        <% end %> --%>

        <div id="categories-fields">
          <label class="block mb-1 font-semibold">Categories</label>
          <div class="flex flex-wrap gap-2 mb-2">
            <%= for {category, _index} <- Enum.with_index(@selected_categories || []) do %>
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
  def update(
        assigns,
        socket
      ) do
    {:ok,
     socket
     |> assign(assigns)
     |> assign_new(:form, fn ->
       to_form(%{})
     end)}
  end

  @impl true
  def handle_event("save", _, socket) do
    # IO.inspect(socket.assigns.selected_transactions, label: "Selected transactions")
    # IO.inspect(socket.assigns.selected_categories, label: "Selected categories")

    {:ok, transactions} = Finance.bulk_update_transactions_categories(
      socket.assigns.selected_transactions,
      socket.assigns.selected_categories
    )

    {:noreply,
     socket
     |> put_flash(:info, "Transactions categories successfully")
     |> push_patch(to: socket.assigns.patch)}

  end

  defp add_transactions_stream(socket, []) do
    socket
  end
  defp add_transactions_stream(socket, transactions) do
    Enum.reduce(transactions, socket, fn transaction, acc_socket ->
      stream_insert(acc_socket, :transactions, transaction)
    end)
  end
end
