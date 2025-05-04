defmodule PersonalFinanceWeb.CategoryLive.FormComponent do
  use PersonalFinanceWeb, :live_component

  alias PersonalFinance.Finance

  @impl true
  def render(assigns) do
    ~H"""
    <div>
      <.header>
        {@title}
        <:subtitle>Use this form to manage category records in your database.</:subtitle>
      </.header>

      <.simple_form
        for={@form}
        id="category-form"
        phx-target={@myself}
        phx-change="validate"
        phx-submit="save"
      >
        <.input field={@form[:name]} type="text" label="Name" />
        <.input
          field={@form[:macro_category]}
          type="select"
          label="Macro category"
          options={
            Enum.map(@macro_categories, fn %PersonalFinance.Finance.MacroCategory{
                                             type: type,
                                             name: name
                                           } ->
              {name, type}
            end)
          }
        />
        <.input field={@form[:emoji]} type="text" label="Emoji" />
        <div id="matchers-fields">
      <label class="block mb-1">Matchers</label>
      <%= for {matcher, index} <- Enum.with_index(@matchers || []) do %>
        <div class="matcher-field flex gap-2 items-center mb-2">
          <!-- The name attribute ensures that matchers come as a list in params -->
          <input
            type="text"
            name="category[matchers][]"
            value={matcher}
            placeholder="Enter matcher"
            class="input"
          />
          <button
            type="button"
            phx-click="remove_matcher"
            phx-value-index={index}
            class="btn btn-danger"
          >
            Remove
          </button>
        </div>
      <% end %>
    </div>
    <button type="button" phx-click="add_matcher" class="btn btn-secondary">
      Add Matcher
    </button>
        <:actions>
          <.button phx-disable-with="Saving...">Save Category</.button>
        </:actions>
      </.simple_form>
    </div>
    """
  end

  @impl true
  def update(%{category: category} = assigns, socket) do
    {:ok,
     socket
     |> assign(assigns)
     |> assign_new(:form, fn ->
       to_form(Finance.change_category(category))
     end)}
  end

  @impl true
  def handle_event("validate", %{"category" => category_params}, socket) do
    changeset = Finance.change_category(socket.assigns.category, category_params)
    {:noreply, assign(socket, form: to_form(changeset, action: :validate))}
  end

  def handle_event("save", %{"category" => category_params}, socket) do
    save_category(socket, socket.assigns.action, category_params)
  end

  defp save_category(socket, :edit, category_params) do
    case Finance.update_category(socket.assigns.category, category_params) do
      {:ok, category} ->
        notify_parent({:saved, category})

        {:noreply,
         socket
         |> put_flash(:info, "Category updated successfully")
         |> push_patch(to: socket.assigns.patch)}

      {:error, %Ecto.Changeset{} = changeset} ->
        {:noreply, assign(socket, form: to_form(changeset))}
    end
  end

  defp save_category(socket, :new, category_params) do
    case Finance.create_category(category_params) do
      {:ok, category} ->
        notify_parent({:saved, category})

        {:noreply,
         socket
         |> put_flash(:info, "Category created successfully")
         |> push_patch(to: socket.assigns.patch)}

      {:error, %Ecto.Changeset{} = changeset} ->
        {:noreply, assign(socket, form: to_form(changeset))}
    end
  end

  defp notify_parent(msg), do: send(self(), {__MODULE__, msg})
end
