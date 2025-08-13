CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    date DATE NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    description VARCHAR(255) NOT NULL,
    unique_id VARCHAR(255) NOT NULL UNIQUE,
    source VARCHAR(50) NOT NULL,
    skip BOOLEAN NOT NULL,
    user_owner VARCHAR(255) NOT NULL,
    category_id BIGINT,
    inserted_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id)
); 