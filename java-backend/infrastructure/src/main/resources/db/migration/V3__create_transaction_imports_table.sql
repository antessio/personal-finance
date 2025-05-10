CREATE TABLE transaction_imports (
    id BIGINT PRIMARY KEY,
    source_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_owner VARCHAR(255) NOT NULL,
    inserted_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
); 