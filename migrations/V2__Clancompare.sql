-- Revises: V1
-- Creation Date: 2025-05-04 00:06:00.000000 UTC
-- Reason: Adds ClanCompare table for storing the alt id

CREATE TABLE IF NOT EXISTS clan_compare (
    guild_id BIGINT PRIMARY KEY,
    alt_comp_id INT
);