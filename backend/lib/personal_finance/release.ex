defmodule PersonalFinance.Release do
  @app :personal_finance
  def migrate do
    # 1) Start the app and all its dependencies
    {:ok, _} = Application.ensure_all_started(@app)

    # 2) Run migrations for each repo
    for repo <- Application.fetch_env!(@app, :ecto_repos) do
      path = priv_path_for(repo, "migrations")
      Ecto.Migrator.run(repo, path, :up, all: true)
    end

    # 3) (Optional) Stop the node if you want the eval command to exit
    :init.stop()
  end

  defp priv_path_for(repo, subdir) do
    repo_underscore =
      repo
      |> Module.split()
      |> List.last()
      |> Macro.underscore()

    Application.app_dir(@app, ["priv", repo_underscore, subdir])
  end
end
