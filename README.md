# Spirits of Arianwyn Discord Bot
Version: 4.0

A Rewrite of the SoA Discord Bot project using the [Discord.py](https://github.com/Rapptz/discord.py) framework for use by the Spirits of Arianwyn RuneScape clan's Discord Server.

This rewrite is a work-in-progress.  The plan is to start from scratch, replicating the features that we wish to keep while eliminating functionality that is no longer useful.

## Project History:
The original bot (version 1) was written using Discord4J in Java.  Early versions used Discord4J 2.x and provided a few useful utilities for the clan's server.

There was no Version 2 of the bot, as Version 2 was skipped to keep version numbers more in line with Discord4J's version numbers.

Version 3 of the SoA Discord Bot is a complete rewrite to accomodate the new Project Reactor framework used within Discord4J.  This bot utilized application commands rather than text based commands and had rewritten older components to be more stable as the original implementations of some components were prone to issues and could have been implemented much better.  The final version was using Discord4J version 3.2.x.  

While the bot as it stands written in Java is still completely functional, development on Discord4J has slowed significantly and work on a different project that made use of Discord.py provided some significant enhancements to the development experience.  Given the bot has been in the state of needing a refresh for some time, it is being rewritten to take advantage of the different framework as well as to remove some old tech debt that the previous versions had.

## Using the bot
TBD

## More Information
For more information, please refer to the thread within Elvish Lounge found on the [SoA Forums](https://forums.soa-rs.com).
