-- Revises: V2
-- Creation Date: 2026-03-01 04:20:00.000000 UTC
-- Reason: Adds tables for Resource Changes and tracking RSNs

CREATE TABLE IF NOT EXISTS resource_change_fields (
    id SERIAL PRIMARY KEY,
    guild_id BIGINT,
    field_name TEXT NOT NULL,
    position INT,
    link TEXT
);

CREATE TABLE IF NOT EXISTS resource_change (
    id SERIAL PRIMARY KEY,
    username TEXT NOT NULL,
    reason TEXT NOT NULL,
    guild_id BIGINT,
    channel_id BIGINT,
    message_id BIGINT
);

CREATE TABLE IF NOT EXISTS resource_change_statuses (
    id SERIAL PRIMARY KEY,
    res_change_id INT,
    res_change_field INT,
    res_change_value TEXT ARRAY DEFAULT '{}'
);

CREATE INDEX IF NOT EXISTS resource_change_statuses_id_idx on resource_change_statuses (res_change_id);

CREATE TABLE IF NOT EXISTS resource_change_settings (
    guild_id BIGINT PRIMARY KEY,
    channel_id BIGINT
);

CREATE TABLE IF NOT EXISTS runescape_names (
    guild_id BIGINT,
    user_id BIGINT,
    runescape_name TEXT NOT NULL,
    PRIMARY KEY (user_id, guild_id)
);

CREATE INDEX IF NOT EXISTS runescape_names_idx on runescape_names (runescape_name);
