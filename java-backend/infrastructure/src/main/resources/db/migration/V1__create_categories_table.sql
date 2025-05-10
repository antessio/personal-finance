CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    macro_category VARCHAR(50) NOT NULL,
    user_owner VARCHAR(255) NOT NULL,
    inserted_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE category_matchers (
    category_id VARCHAR(36) NOT NULL,
    matcher VARCHAR(255) NOT NULL,
    PRIMARY KEY (category_id, matcher),
    FOREIGN KEY (category_id) REFERENCES categories(id)
); 