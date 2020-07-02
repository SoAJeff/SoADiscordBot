package com.soa.rs.discordbot.v3.usertrack;

import discord4j.common.util.Snowflake;

public class UserTrackVoiceStateUpdate {

	private RecentCache lastSeenCache;
	private RecentCache lastActiveCache;

	public void handleVoiceStateUpdate(Snowflake user, Snowflake guild) {
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
