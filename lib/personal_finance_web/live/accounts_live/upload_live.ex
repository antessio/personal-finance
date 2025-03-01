defmodule PersonalFinanceWeb.AccountsLive.UploadLive do

  use PersonalFinanceWeb, :live_view

  alias PersonalFinance.ExternalAccounts

  @impl true
  def mount(_params, _session, socket) do
    {:ok,
     socket
     |> assign(:source_types, PersonalFinance.ExternalAccounts.Accounts.source_types())
     |> assign(:uploaded_files, [])
     |> allow_upload(:file, accept: ~w(.csv .xlsx), max_entries: 1)}
  end

  @impl true
  def handle_event("validate", _params, socket) do
    {:noreply, socket}
  end

  @impl true
  def handle_event("save", %{"source_type" => source_type}, socket) do
    [%ExternalAccounts.Accounts{id: id}] =
      consume_uploaded_entries(socket, :file, fn %{path: path}, _entry ->
        dest = Path.join(Application.app_dir(:personal_finance, "priv/static/uploads"), Path.basename(path))
        # # You will need to create `priv/static/uploads` for `File.cp!/2` to work.
        File.cp!(path, dest)
        command = %ExternalAccounts.CreateAccountCommand{
          source_type: source_type,
          file_path: dest
        }

        ExternalAccounts.import_account(command)
      end)

    {:noreply,
     socket
     |> put_flash(:info, "File uploaded successfully. Account ID: #{id}")}
  end

  @impl true
  def handle_event("cancel-upload", %{"ref" => ref}, socket) do
    {:noreply, cancel_upload(socket, :file, ref)}
  end

  defp error_to_string(:too_large), do: "Too large"
  defp error_to_string(:not_accepted), do: "You have selected an unacceptable file type"
  defp error_to_string(:too_many_files), do: "You have selected too many files"
end
