CREATE TABLE users (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATE NOT NULL
);
