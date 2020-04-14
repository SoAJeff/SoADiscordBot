package com.soa.rs.discordbot.v3.usertrack;

import discord4j.rest.util.Snowflake;

public class UserTrackReactionUpdate {

	private RecentCache lastSeenCache;
	private RecentCache lastActiveCache;

	public void handle(Snowflake guild, Snowflake user) {
		lastSeenCache.updateCacheForGuildUser(guild.asLong(), user.asLong());
		lastActiveCache.updateCacheForGuildUser(guild.asLong(), user.asLong());
	}

	public void setLastSeenCache(RecentCache lastSeenCache) {
		this.lastSeenCache = lastSeenCache;
	}

	public void setLastActiveCache(RecentCache lastActiveCache) {
		this.lastActiveCache = lastActiveCache;
	}
}
