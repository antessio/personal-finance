-- V6__alter_transactions_description_to_text.sql
-- Migrazione per modificare il campo description in TEXT nella tabella transactions

ALTER TABLE transactions
    ALTER COLUMN description TYPE TEXT;

