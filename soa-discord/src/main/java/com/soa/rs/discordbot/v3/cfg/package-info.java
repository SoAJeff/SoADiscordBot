/**
 * Provides the classes related to handling configuration of the bot. This
 * includes the readers and writers for using a start-up configuration file, and
 * the {@link com.soa.rs.discordbot.cfg.DiscordCfg} class, which holds all
 * configuration generated and used by the bot during normal operation.
 * Configuration parameters which have been defined can be modified in this
 * class during runtime and can be accessed by all other classes in a
 * thread-safe manner.
 */
package com.soa.rs.discordbot.v3.cfg;