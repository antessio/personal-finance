# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev        # Development server with Turbopack (port 3000)
npm run build      # Production build
npm run lint       # ESLint
```

No test runner is configured. MSW (Mock Service Worker) is used in-browser for local mocking.

## Architecture

### Service Layer (`src/services/`)

All data access goes through the `PersonalFinanceService` interface (`personalFinanceService.ts`). Two implementations exist:

- **`restPersonalFinanceService.ts`** — Axios-based REST client. Attaches JWT Bearer token from localStorage via request interceptor; clears token and redirects to `/login` on 401.
- **`mockPersonalFinanceService.ts`** — In-memory implementation with 500ms simulated delays and realistic data generation. Stores current user in `sessionStorage`.

**Service factory** (`api.ts`) selects the implementation:
```typescript
export const service = process.env.NODE_ENV != 'development'
  ? new MockPersonalFinanceService()
  : new RestPersonalFinanceService();
```
Note: This is inverted — REST is active in `development`, Mock in other environments (production build).

### State Management

**React Query** handles all server state. Cache key convention: `['resource', filtersObject]`.

```typescript
useQuery({ queryKey: ['transactions', filters], queryFn: () => service.getTransactions(filters) })
useQuery({ queryKey: ['categories'], queryFn: () => service.getAllCategories() })
```

Invalidate via `queryClient.invalidateQueries({ queryKey: ['transactions'] })` after mutations.

**Cursor-based pagination**: `filters.cursor` drives "load more". When cursor is present, append new data; otherwise replace:
```typescript
if (filters.cursor) setAllTransactions(prev => [...prev, ...newData]);
else setAllTransactions(newData);
```

### Auth

JWT is stored in both `localStorage` (client reads) and a cookie (middleware SSR reads). `src/middleware.ts` runs server-side on every request — redirects unauthenticated users to `/login` and authenticated users away from auth pages.

`src/config/auth.ts` exposes `isAuthEnabled()` which reads `NEXT_PUBLIC_AUTH_ENABLED`. Set to `false` to skip auth checks in local dev.

### Theming

MUI theme is built dynamically in `src/app/layout.tsx` based on `ThemeContext` mode (`light`/`dark`). Theme preference is persisted to localStorage and initialized from the system color scheme. The provider stack in layout is:

```
QueryClientProvider → ThemeContextProvider → AppThemeProvider (MUI) → LocalizationProvider → AuthProvider
```

## Type Mappings

REST API responses differ from frontend types. `src/services/rest/types/index.ts` handles conversion:
- `source` → `account`
- `matchers` → `regexPatterns`
- Emoji extraction from category/macro-category names

Frontend domain types are in `src/types/index.ts`. Key ones: `Transaction`, `Category`, `Budget`, `TransactionFilters`, `PaginatedResponse<T>`, `MonthlyData`, `MacroCategoryMonthlyData`.

## Environment Variables

```
NEXT_PUBLIC_API_URL=http://localhost:8080   # Backend base URL
NEXT_PUBLIC_AUTH_ENABLED=false              # Disable auth enforcement
```
