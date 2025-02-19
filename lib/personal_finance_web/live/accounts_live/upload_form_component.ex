defmodule PersonalFinanceWeb.AccountsLive.UploadFormComponent do
  use PersonalFinanceWeb, :live_component

  alias PersonalFinance.ExternalAccounts

  @impl true
  def render(assigns) do
    ~H"""
    <div>
      <form id="upload-form" phx-submit="save" phx-change="validate">
        <.live_file_input upload={@uploads.file} />
        <button type="submit">Upload</button>

        <%!-- use phx-drop-target with the upload ref to enable file drag and drop --%>
        <section phx-drop-target={@uploads.file.ref}>
          <%!-- render each avatar entry --%>
          <article :for={entry <- @uploads.file.entries} class="upload-entry">
            <figure>
              <.live_img_preview entry={entry} />
              <figcaption>{entry.client_name}</figcaption>
            </figure>

            <%!-- entry.progress will update automatically for in-flight entries --%>
            <progress value={entry.progress} max="100">{entry.progress}%</progress>

            <%!-- a regular click event whose handler will invoke Phoenix.LiveView.cancel_upload/3 --%>
            <button
              type="button"
              phx-click="cancel-upload"
              phx-value-ref={entry.ref}
              aria-label="cancel"
            >
              &times;
            </button>

            <%!-- Phoenix.Component.upload_errors/2 returns a list of error atoms --%>
            <p :for={err <- upload_errors(@uploads.file, entry)} class="alert alert-danger">
              {error_to_string(err)}
            </p>
          </article>

          <%!-- Phoenix.Component.upload_errors/1 returns a list of error atoms --%>
          <p :for={err <- upload_errors(@uploads.file)} class="alert alert-danger">
            {error_to_string(err)}
          </p>
        </section>
      </form>
    </div>
    """
  end

  @impl true
  def update(assigns, socket) do
    {:ok,
     socket
     |> assign(assigns)
     |> assign(:source_types, ExternalAccounts.Accounts.source_types())
     |> assign(:uploaded_files, [])
     |> allow_upload(:file, accept: ~w(.csv), max_entries: 2)
    }

  end

  @impl true
  def handle_event("validate", %{"_target" => ["file"]}, socket) do
    {:noreply, socket}
  end

  @impl true
  def handle_event("save", _params, socket) do
    uploaded_files =
      consume_uploaded_entries(socket, :file, fn %{path: path}, _entry ->
        # dest = Path.join(Application.app_dir(:my_app, "priv/static/uploads"), Path.basename(path))
        # # You will need to create `priv/static/uploads` for `File.cp!/2` to work.
        # File.cp!(path, dest)
        IO.inspect(path, label: "path")
        {:ok, ~p"/uploads/#{Path.basename(path)}"}
      end)

    {:noreply, update(socket, :uploaded_files, &(&1 ++ uploaded_files))}
  end

  # def handle_event(
  #       "upload",
  #       %{"upload_form" => %{"source_type" => source_type, "file" => file}},
  #       socket
  #     ) do
  #   file_content = File.read!(file.path)

  #   command = %ExternalAccounts.CreateAccountCommand{
  #     source_type: source_type,
  #     file_content: file_content
  #   }

  #   case ExternalAccounts.import_account(command) do
  #     {:ok, _account} ->
  #       {:noreply,
  #        socket
  #        |> put_flash(:info, "File uploaded successfully")
  #        |> push_patch(to: socket.assigns.patch)}

  #     {:error, reason} ->
  #       {:noreply, put_flash(socket, :error, "Failed to upload file: #{reason}")}
  #   end
  # end

  @impl true
  def handle_event("cancel-upload", %{"ref" => ref}, socket) do
    {:noreply, cancel_upload(socket, :file, ref)}
  end

  defp error_to_string(:too_large), do: "Too large"
  defp error_to_string(:not_accepted), do: "You have selected an unacceptable file type"
  defp error_to_string(:too_many_files), do: "You have selected too many files"
end
