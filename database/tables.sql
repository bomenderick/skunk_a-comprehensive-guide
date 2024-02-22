
CREATE TABLE IF NOT EXISTS users (
    id      UUID    PRIMARY KEY,
    name    VARCHAR        NOT NULL,
    email   VARCHAR UNIQUE NOT NULL
);