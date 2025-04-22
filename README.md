# Spirits of Arianwyn Discord Bot
Version: 4.0

A Rewrite of the SoA Discord Bot project using the [Discord.py](https://github.com/Rapptz/discord.py) framework for use by the Spirits of Arianwyn RuneScape clan's Discord Server.

This rewrite is a work-in-progress.  The plan is to start from scratch, replicating the features that we wish to keep while eliminating functionality that is no longer useful.

## Project History:
The original bot (version 1) was written using Discord4J in Java.  Early versions used Discord4J 2.x and provided a few useful utilities for the clan's server.

There was no Version 2 of the bot, as Version 2 was skipped to keep version numbers more in line with Discord4J's version numbers.

Version 3 of the SoA Discord Bot is a complete rewrite to accomodate the new Project Reactor framework used within Discord4J.  This bot utilized application commands rather than text based commands and had rewritten older components to be more stable as the original implementations of some components were prone to issues and could have been implemented much better.  The final version was using Discord4J version 3.2.x.  

While the bot as it stands written in Java is still completely functional, development on the Discord4J project has slowed significantly - Discord4J's current release does not support many new Discord features.  Work on a different project that made use of Discord.py library provided some significant enhancements to the development experience of creating a Discord bot.  Given the bot has been in the state of needing a refresh for some time, it is being rewritten to take advantage of the Discord.py framework as well as to remove some old tech debt that the previous versions had.

## Using the bot
The following dependencies are required to run the bot:
- A Python 3.8+ environment
- A PostgreSQL database

The production version of the bot runs in a Docker based environment.  A `Dockerfile` and `docker-compose.yml` file are provided as an example and may be tailored to fit the environment that the bot runs in.

The following instructions may be followed to set up a functional environment for the bot.

1. Build the Docker invite with the following command in the base of this repo: `docker build -t soa:latest .`.  This will create the container image the bot will use and install all required dependencies.

2. Start the Postgres instance.  If using Docker Compose, this can be done with the following: `docker compose up -d postgres`.  Connect to the psql shell (if using Docker compose, run `docker exec -it soa-postgres-1 psql -U postgres`).  Execute the following at the psql prompt to create the database:

```
CREATE ROLE soabot WITH LOGIN PASSWORD 'soabot';
CREATE DATABASE soa OWNER soabot;
\c soabot
CREATE EXTENSION pg_trgm;
```

3. Setup a configuration file with the following parameters:

```
BOT_TOKEN="" # Your Bot's token from the Discord Developer Portal
INITIAL_PRESENCE="RuneScape Clan" # The initial presence to set when the bot runs.  RuneScape Clan is an example default value
FORUMS_API_KEY = "" # An API key .  This should have access to the following endpoints: GET /core/members, GET /calendar/events
PSQL_URI = "" # The URI to your postgres instance
ERROR_WEBHOOK = "" # Optional, a Discord webhook that can be used to send uncaught errors to a Discord channel
```

4. Using the `soa:latest` Discord image, load into a shell prompt and execute the following to populate the database: `python migrations.py init`

5. Execute `docker compose up -d` to bring up the entire stack of images and start the bot.

## More Information
For more information, please refer to the thread within Elvish Lounge found on the [SoA Forums](https://forums.soa-rs.com).
