package com.soa.rs.discordbot.v3.usertrack;

import discord4j.core.object.util.Snowflake;

public class UserTrackVoiceStateUpdate {

	private RecentlySeenCache cache;

	public void handleVoiceStateUpdate(Snowflake user, Snowflake guild) {
		cache.updateCacheForGuildUser(guild.asLong(), user.asLong());
	}

	public void setCache(RecentlySeenCache cache) {
		this.cache = cache;
	}
}
