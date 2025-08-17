-- V9__create_budgets_table.sql
-- Migrazione per creare la tabella budgets

CREATE TABLE IF NOT EXISTS budgets (
    id VARCHAR(36) PRIMARY KEY,
    category_id bigint NOT NULL REFERENCES categories(id),
    amount numeric(19,2) NOT NULL,
    user_owner varchar(255) NOT NULL,
    year integer,
    month integer
);
