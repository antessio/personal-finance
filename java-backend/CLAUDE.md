# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Personal Finance Management application built with Java 21, Spring Boot 3.2.3, and PostgreSQL 16. This is a multi-module Maven project implementing **Hexagonal Architecture** (Ports and Adapters) to separate business logic from infrastructure concerns.

Note: The root pom.xml declares Java 24 compatibility, but the actual modules (domain and infrastructure) use Java 21.

## Module Structure

### `domain/`
Pure business logic module with **no framework dependencies** (except Lombok, logging, and test utilities).

- **`model/`**: Core domain entities (`Transaction`, `Category`, `Budget`, `AutomaticSkip`, `TransactionImport`)
- **`service/`**: Business logic services (`TransactionService`, `TransactionQueryService`, `CategoryService`, `BudgetService`, `TransactionImportService`)
- **`ports/`**: Repository interfaces and event publishers that infrastructure must implement
- **`dto/`**: Data transfer objects for service layer
- **`events/`**: Domain events for transaction lifecycle
- **`service/transactionparser/`**: Bank-specific transaction parsers (Intesa, Widiba, Satispay)

### `infrastructure/`
Spring Boot application implementing domain ports and exposing REST APIs.

- **`persistence/`**: JPA entities, Spring Data repositories, and adapters implementing domain repository ports
- **`web/controller/`**: REST controllers organized by feature:
  - `authentication/`: User authentication and registration
  - `dashboard/`: Main API endpoints (`TransactionController`, `CategoryController`, `BudgetController`, `AccountController`, `ConfigurationController`, `TransactionImportController`)
- **`security/`**: JWT-based authentication (can be disabled via `security.authentication.enabled=false`)
- **`eventpublisher/`**: Spring event publisher implementing domain event publisher ports
- **`eventhandler/`**: Handlers for domain events

## Key Architectural Patterns

1. **Dependency Flow**: `infrastructure` depends on `domain`, never the reverse
2. **Ports and Adapters**: Domain defines repository interfaces (`ports/`), infrastructure provides implementations (`persistence/repository/*Adapter.java`)
3. **Domain Services**: Business logic lives in domain services, not in entities or controllers
4. **Event-Driven**: Domain events published for transaction operations, handled by infrastructure
5. **Custom IDs**: Using `Id` value object with time-ordered UUIDs (uuid-creator library)

## Build and Run Commands

### Prerequisites
- Java 21
- Maven 3.x
- PostgreSQL 16 (or use Docker Compose)

### Start Database
```bash
cd infrastructure/docker
docker compose -f postgres.yml up -d
```

Default credentials:
- Database: `personalfinance_db`
- Username: `personalfinance_root`
- Password: `personalfinance_pwd`
- Port: `5432`

### Build
```bash
# Build everything from root
mvn clean install

# Build specific module
cd domain && mvn clean install
cd infrastructure && mvn clean install
```

### Run Application
```bash
cd infrastructure
mvn spring-boot:run
```

Application runs on `http://localhost:8080`

### Run Tests
```bash
# Run all tests
mvn test

# Run tests for specific module
cd domain && mvn test
cd infrastructure && mvn test

# Run single test class
mvn test -Dtest=TransactionServiceTest

# Run single test method
mvn test -Dtest=TransactionServiceTest#testMethodName
```

## Configuration

Main configuration file: `infrastructure/src/main/resources/application.properties`

Important settings:
- `security.authentication.enabled=false`: Disables JWT authentication for development
- `security.allowedOrigins`: CORS origins (default: `http://localhost:3000`)
- `spring.datasource.*`: Database connection settings (can override with environment variables)
- `personal-finance.file.path=/tmp`: File upload directory for transaction imports

## Database Migrations

Using **Flyway** for schema versioning. Migrations in `infrastructure/src/main/resources/db/migration/`:
- `V1__create_categories_table.sql`
- `V2__create_transactions_table.sql`
- `V3__create_transaction_imports_table.sql`
- `V4__create_users_table.sql`
- `V5__create_skip_matchers.sql`
- `V6__alter_transactions_description_to_text.sql`
- `V7__add_transaction_import_id_to_transactions.sql`
- `V8__add_trgm_index_to_transactions.sql` (PostgreSQL trigram extension for similarity search)
- `V9__create_budgets_table.sql`
- `V10__add_category_type.sql`

Migrations run automatically on application startup.

## Key Domain Concepts

### Transactions
Financial transactions with amount, date, description, category, and account type. Can be:
- Manually created
- Imported from bank files (Excel/CSV/PDF)
- Automatically categorized
- Marked as "skip" to exclude from analytics

### Categories
Two-level hierarchy:
- **Macro Category**: High-level grouping (e.g., EXPENSES, INCOME, SAVINGS)
- **Category**: Specific category (e.g., "Groceries", "Salary")

Categories have a `CategoryType` (INCOME, EXPENSE, TRANSFER) that determines how they're treated in calculations.

### Budgets
Monthly spending limits per category with tracking against actual spending.

### Automatic Skip
Pattern-based rules to automatically mark transactions as "skip" (e.g., transfers between own accounts).

### Transaction Import
Batch import of transactions from bank files (Excel/CSV/PDF). Tracks which transactions came from which import for deduplication. The system automatically detects the file format based on the `sourceType` field in the import request.

## Transaction Parsers

Bank-specific parsers in `domain/service/transactionparser/`:
- `IntesaTransactionParser`: Intesa Sanpaolo bank format (Excel)
- `WidibaTransactionParser`: Widiba bank format (Excel)
- `SatispayTransactionParser`: Satispay payment app format (Excel)
- `SatispayOldTransactionParser`: Legacy Satispay format (CSV)
- `PayPalTransactionParser`: PayPal transaction export (CSV)
- `TradeRepublicTransactionParser`: Trade Republic bank statement (PDF)

Each parser implements specific logic for parsing bank-specific file formats (Excel/CSV/PDF). Note: PDF parsers use Apache PDFBox and must handle non-breaking spaces (\u00A0) in amount formatting.

## Code Conventions

- **Lombok** extensively used for boilerplate reduction (@Data, @Builder, @AllArgsConstructor, etc.)
- **Domain models are immutable** where possible (use @Value or final fields)
- **Repository methods** return `Optional<T>` for single results, not null
- **Custom ID types** (`TransactionId`, `CategoryId`, `BudgetId`) wrap `Id` value object
- **DTOs** separate from domain models - mappers convert between them
- **Service layer** handles all business logic, controllers are thin

## Testing

- JUnit 5 (Jupiter) for test framework
- Mockito for mocking
- AssertJ for fluent assertions
- Integration tests use Spring Boot Test with test database

When writing tests:
- Unit test domain services by mocking repository ports
- Integration test infrastructure layer with real Spring context
- Test transaction parsers with sample bank files in `src/test/resources`