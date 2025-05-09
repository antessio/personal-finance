defmodule PersonalFinanceWeb.Router do
  use PersonalFinanceWeb, :router

  import PersonalFinanceWeb.UserAuth

  pipeline :browser do
    plug :accepts, ["html"]
    plug :fetch_session
    plug :fetch_live_flash
    plug :put_root_layout, html: {PersonalFinanceWeb.Layouts, :root}
    plug :protect_from_forgery
    plug :put_secure_browser_headers
    plug :fetch_current_user
  end

  pipeline :api do
    plug :accepts, ["json"]
    plug :fetch_session
    plug :fetch_current_user
  end

  pipeline :api_protected do
    plug :accepts, ["json"]
    plug :fetch_session
    plug :fetch_current_user
    plug :require_authenticated_user
  end

  scope "/", PersonalFinanceWeb do
    pipe_through :browser

    get "/", PageController, :home
  end

  # Other scopes may use custom stacks.
  scope "/api", PersonalFinanceWeb.API do
    pipe_through :api

    post "/users/register", AuthController, :register
    post "/users/log_in", AuthController, :login
    post "/users/reset_password", PasswordController, :forgot
    put "/users/reset_password/:token", PasswordController, :reset
  end

  scope "/api", PersonalFinanceWeb.API do
    pipe_through :api_protected

    get "/users/me", UserController, :me
    resources "/transactions", TransactionController, except: [:new, :edit]
  end

  # Enable LiveDashboard and Swoosh mailbox preview in development
  if Application.compile_env(:personal_finance, :dev_routes) do
    # If you want to use the LiveDashboard in production, you should put
    # it behind authentication and allow only admins to access it.
    # If your application does not have an admins-only section yet,
    # you can use Plug.BasicAuth to set up some basic authentication
    # as long as you are also using SSL (which you should anyway).
    import Phoenix.LiveDashboard.Router

    scope "/dev" do
      pipe_through :browser

      live_dashboard "/dashboard", metrics: PersonalFinanceWeb.Telemetry
      forward "/mailbox", Plug.Swoosh.MailboxPreview
    end
  end

  ## Authentication routes

  scope "/", PersonalFinanceWeb do
    pipe_through [:browser, :redirect_if_user_is_authenticated]

    live_session :redirect_if_user_is_authenticated,
      on_mount: [{PersonalFinanceWeb.UserAuth, :redirect_if_user_is_authenticated}] do
      live "/users/register", UserRegistrationLive, :new
      live "/users/log_in", UserLoginLive, :new
      live "/users/reset_password", UserForgotPasswordLive, :new
      live "/users/reset_password/:token", UserResetPasswordLive, :edit
    end

    post "/users/log_in", UserSessionController, :create
  end

  scope "/", PersonalFinanceWeb do
    pipe_through [:browser, :require_authenticated_user]

    live_session :require_authenticated_user,
      on_mount: [{PersonalFinanceWeb.UserAuth, :ensure_authenticated}] do
      live "/users/settings", UserSettingsLive, :edit
      live "/users/settings/confirm_email/:token", UserSettingsLive, :confirm_email

      # categories
      live "/categories", CategoryLive.Index, :index
      live "/categories/new", CategoryLive.Index, :new
      live "/categories/:id/edit", CategoryLive.Index, :edit

      live "/categories/:id", CategoryLive.Show, :show
      live "/categories/:id/show/edit", CategoryLive.Show, :edit

      # transactions
      live "/transactions", TransactionLive.Index, :index
      live "/transactions/new", TransactionLive.Index, :new
      live "/transactions/bulk_categorization", TransactionLive.Index, :bulk_categorization
      live "/transactions/:id/edit", TransactionLive.Index, :edit

      live "/transactions/:id", TransactionLive.Show, :show
      live "/transactions/:id/show/edit", TransactionLive.Show, :edit


      # transaction imports
      live "/accounts", AccountsLive.Index, :index
      live "/accounts/new", AccountsLive.Index, :new
      live "/accounts/upload", AccountsLive.UploadLive, :upload
      live "/accounts/:id/edit", AccountsLive.Index, :edit

      live "/accounts/:id", AccountsLive.Show, :show
      live "/accounts/:id/show/edit", AccountsLive.Show, :edit
    end
  end

  scope "/", PersonalFinanceWeb do
    pipe_through [:browser]

    delete "/users/log_out", UserSessionController, :delete

    live_session :current_user,
      on_mount: [{PersonalFinanceWeb.UserAuth, :mount_current_user}] do
      live "/users/confirm/:token", UserConfirmationLive, :edit
      live "/users/confirm", UserConfirmationInstructionsLive, :new
    end
  end
end
