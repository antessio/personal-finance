-- Enable UUID generation (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name TEXT       NOT NULL,
    email     VARCHAR(100) NOT NULL UNIQUE,
    password  TEXT        NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    is_verified BOOLEAN  NOT NULL DEFAULT FALSE
);

-- Roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    role    TEXT NOT NULL,
    CONSTRAINT fk_roles_user
      FOREIGN KEY(user_id)
      REFERENCES users(id)
      ON DELETE CASCADE
);

-- Optional: index for faster lookups of a user’s roles
CREATE INDEX idx_roles_user_id
    ON roles(user_id);