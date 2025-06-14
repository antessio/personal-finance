CREATE TABLE categories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    macro_category VARCHAR(50) NOT NULL,
    emoji VARCHAR(255),
    user_owner VARCHAR(255) NOT NULL,
    inserted_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE category_matchers (
    category_id BIGINT NOT NULL,
    matcher VARCHAR(255) NOT NULL,
    PRIMARY KEY (category_id, matcher),
    FOREIGN KEY (category_id) REFERENCES categories(id)
); 