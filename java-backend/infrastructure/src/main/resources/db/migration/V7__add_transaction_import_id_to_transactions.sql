-- V7__add_transaction_import_id_to_transactions.sql
-- Migrazione per aggiungere il campo transaction_import_id alla tabella transactions

ALTER TABLE transactions
    ADD COLUMN transaction_import_id BIGINT DEFAULT NULL,
    ADD CONSTRAINT fk_transaction_import_id FOREIGN KEY (transaction_import_id) REFERENCES transaction_imports(id);
