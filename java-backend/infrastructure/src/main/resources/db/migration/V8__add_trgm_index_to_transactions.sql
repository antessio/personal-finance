-- V8__add_trgm_index_to_transactions.sql
-- Migrazione per creare l'estensione pg_trgm e l'indice GIN su user_owner e description

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX if not exists idx_transactions_description_trgm
  ON transactions
  USING gin (description gin_trgm_ops);

