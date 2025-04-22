-- Revises: V0
-- Creation Date: 2025-02-03 02:00:00.000000 UTC
-- Reason: Initial database migration

CREATE TABLE IF NOT EXISTS event_post_channels (
    guild_id BIGINT PRIMARY KEY,
    event_post_channel_id BIGINT
);

CREATE TABLE IF NOT EXISTS runescape_news_feed (
    guild_id BIGINT PRIMARY KEY,
    news_feed_channel_id BIGINT
);

CREATE TABLE IF NOT EXISTS oldschool_news_feed (
    guild_id BIGINT PRIMARY KEY,
    news_feed_channel_id BIGINT
);

CREATE TABLE IF NOT EXISTS soa_news_feed (
    guild_id BIGINT PRIMARY KEY,
    news_feed_channel_id BIGINT
);

-- Usertrack

CREATE TABLE IF NOT EXISTS nicknames (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    guild_id BIGINT NOT NULL,
    nickname TEXT
);

CREATE TABLE IF NOT EXISTS recent_actions (
    id SERIAL PRIMARY KEY,
    date TIMESTAMP DEFAULT (now() at time zone 'utc') NOT NULL,
    user_id BIGINT NOT NULL,
    guild_id BIGINT NOT NULL,
    action TEXT,
    original_value TEXT,
    new_value TEXT
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL,
    guild_id BIGINT NOT NULL,
    user_name TEXT NOT NULL,
    known_name TEXT,
    display_name TEXT NOT NULL,
    last_seen TIMESTAMP DEFAULT (now() at time zone 'utc') NOT NULL,
    joined_server TIMESTAMP NOT NULL,
    left_server TIMESTAMP,
    last_active TIMESTAMP DEFAULT (now() at time zone 'utc') NOT NULL,
    PRIMARY KEY (user_id, guild_id)
);

CREATE TABLE IF NOT EXISTS auditing_channels (
    guild_id BIGINT PRIMARY KEY,
    user_join_channel_id BIGINT,
    user_left_channel_id BIGINT
);