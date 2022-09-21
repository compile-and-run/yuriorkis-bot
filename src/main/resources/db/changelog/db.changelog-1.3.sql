--liquibase formatted sql

--changeset kelvium:1
CREATE TABLE IF NOT EXISTS DANCER_VIEWER (
    id IDENTITY NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE,
    score INTEGER
)
