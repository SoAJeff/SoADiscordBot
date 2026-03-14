-- Revises: V3
-- Creation Date: 2026-03-13 02:12:00.000000 UTC
-- Reason: Adds tables for storing event attendance submission thread

CREATE TABLE IF NOT EXISTS attendance_channel (
    id SERIAL PRIMARY KEY,
    guild_id BIGINT,
    channel_id BIGINT,
    thread_id BIGINT
);

-- There will only ever be one of these, so insert a dummy row.
INSERT INTO attendance_channel (guild_id, channel_id, thread_id) VALUES (-1, -1, -1);