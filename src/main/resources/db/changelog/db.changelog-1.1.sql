--liquibase formatted sql

--changeset compile_and_run:1
ALTER TABLE TOY
ADD createdAt TIMESTAMP
DEFAULT CURRENT_TIMESTAMP();
