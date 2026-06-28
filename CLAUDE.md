# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Personal Finance Management app — a full-stack application with a **Next.js 15 frontend** and a **Java 21 / Spring Boot 3 backend** using Hexagonal Architecture. Supports importing bank statements from multiple Italian banks, auto-categorizing transactions, tracking budgets, and visualizing analytics.

> For Java backend details, see `java-backend/CLAUDE.md`.

## Running the Full Stack

**Start database:**
```bash
cd java-backend/infrastructure/docker
docker compose -f postgres.yml up -d
```

**Start backend** (port 8080):
```bash
cd java-backend/infrastructure
mvn spring-boot:run
```

**Start frontend** (port 3000):
```bash
cd frontend
npm run dev        # Next.js with Turbopack
```

## Frontend Commands

```bash
cd frontend

npm run dev        # Development server (Turbopack)
npm run build      # Production build
npm run lint       # ESLint
```

No test runner is configured for the frontend; MSW (Mock Service Worker) is used in-browser for local mocking.

## Backend Commands

```bash
cd java-backend

mvn clean install                                    # Full build
cd infrastructure && mvn spring-boot:run             # Run (port 8080)
mvn test                                             # All tests
mvn test -Dtest=TransactionServiceTest               # Single test class
mvn test -Dtest=TransactionServiceTest#methodName    # Single test method
```

## Frontend Environment Variables

```
NEXT_PUBLIC_API_URL=http://localhost:8080   # Backend base URL
NEXT_PUBLIC_AUTH_ENABLED=false             # Disable auth enforcement
```

Copy `.env.example` to `.env.local` to configure locally.

## Architecture

### System Components

```
browser → Next.js 15 (port 3000) → Java Spring Boot (port 8080) → PostgreSQL 16 (port 5432)
```

The legacy Phoenix/Elixir `backend/` is inactive — all active development is in `java-backend/`.

### Frontend Structure (`frontend/src/`)

- **`app/`** — Next.js App Router pages (`/`, `/transactions`, `/categories`, `/budgets`, `/analytics`, `/upload`, `/investments`, `/login`, `/signup`)
- **`services/`** — Service layer with a `personalFinanceService` interface and two implementations:
  - `restPersonalFinanceService.ts` — REST client (Axios, attaches JWT from localStorage)
  - `mockPersonalFinanceService.ts` — in-memory mock with 500ms simulated delays
  - **`api.ts` factory**: REST is active when `NODE_ENV === 'development'`; Mock in production builds (intentionally inverted — development targets a real backend)
- **`contexts/`** — `AuthContext` (JWT state + login/logout), `ThemeContext` (dark/light mode)
- **`components/charts/`** — Recharts-based visualization components
- **`config/auth.ts`** — `isAuthEnabled()` toggle to disable auth enforcement in development
- **`middleware.ts`** — Server-side route protection (redirects unauthenticated users to `/login`)

### Key Frontend Patterns

- **React Query** (`@tanstack/react-query`) handles all server state — cache keys are `[resource, filters]`
- **JWT** is stored in both `localStorage` (client reads) and a cookie (middleware reads for SSR)
- The Axios instance in `services/api.ts` automatically attaches `Authorization: Bearer <token>` and redirects on 401
- Path alias `@/*` maps to `src/*`

### Backend Summary (see `java-backend/CLAUDE.md` for full details)

- **Hexagonal Architecture**: `domain/` (pure business logic, no framework deps) ← `infrastructure/` (Spring Boot, JPA, REST)
- **Domain ports**: repository interfaces in `domain/ports/` implemented by JPA adapters in `infrastructure/persistence/repository/`
- **Transaction parsers** for: Intesa Sanpaolo (Excel), Widiba (Excel), Satispay (Excel + legacy CSV), PayPal (CSV), Trade Republic (PDF)
- **Flyway** manages schema migrations (V1–V11), runs automatically on startup
- Auth toggle: `security.authentication.enabled=false` in `application.properties` for local dev

## Key Domain Concepts

| Concept | Description |
|---|---|
| **Transaction** | Financial event with date, amount, description, account, category, and `skip` flag |
| **Category** | Two-level: Macro Category (EXPENSES/INCOME/SAVINGS) → Category with regex matchers for auto-categorization |
| **CategoryType** | INCOME, EXPENSE, or TRANSFER — controls how a category is treated in analytics |
| **Budget** | Monthly spending limit per category, tracked against actual spend |
| **AutomaticSkip** | Regex-based rules to mark internal transfers as `skip` (excluded from analytics) |
| **TransactionImport** | Batch import record linking a file upload to the transactions it created |

## Frontend ↔ Backend API

The frontend expects the backend at `NEXT_PUBLIC_API_URL` (defaults to `http://localhost:8080`).

Key endpoint groups:
- `GET/POST/PUT/DELETE /api/transactions` — transaction CRUD + filters (month, account, category, skip)
- `POST /api/transactions/bulk-update` — batch category/skip updates
- `POST /api/uploads` — file upload for bank statement import
- `GET/POST/PUT/DELETE /api/categories`
- `GET/POST/PUT/DELETE /api/budgets`
- `GET /api/reports/*` — analytics data for charts
- `POST /api/auth/login`, `/api/auth/signup`, `/api/auth/logout`
