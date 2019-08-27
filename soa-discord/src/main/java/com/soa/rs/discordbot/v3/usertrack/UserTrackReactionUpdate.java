package com.soa.rs.discordbot.v3.usertrack;

import discord4j.core.object.util.Snowflake;

public class UserTrackReactionUpdate {

	private RecentlySeenCache cache;

	public void handle(Snowflake guild, Snowflake user) {
		cache.updateCacheForGuildUser(guild.asLong(), user.asLong());
	}

	public void setCache(RecentlySeenCache cache) {
		this.cache = cache;
	}
}
