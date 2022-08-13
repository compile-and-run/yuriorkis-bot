--liquibase formatted sql

--changeset compile_and_run:1
CREATE TABLE IF NOT EXISTS TOY (
        id IDENTITY NOT NULL PRIMARY KEY,
        name VARCHAR NOT NULL UNIQUE,
        owner VARCHAR
)
