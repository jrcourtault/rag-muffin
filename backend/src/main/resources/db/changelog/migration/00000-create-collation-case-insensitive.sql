--liquibase formatted sql

--changeset ragmuffin:000-create-collation-case-insensitive
CREATE COLLATION IF NOT EXISTS case_insensitive (provider = icu, locale = 'und-u-ks-level2', deterministic = false);
