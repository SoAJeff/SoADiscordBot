package com.soa.rs.discordbot.v3.usertrack;

import java.util.Date;
import java.util.HashMap;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.util.SoaLogging;

public abstract class RecentCache {
	private final HashMap<Long, HashMap<Long, Date>> guildsMap = new HashMap<>();
	private GuildUserUtility guildUserUtility;

	public RecentCache() {
	}

	public void addNewGuild(long id) {
		SoaLogging.getLogger(this).debug("Adding guild [" + id + "] to " + getCacheName());
		guildsMap.put(id, new HashMap<>());
	}

	public void updateCacheForGuildUser(long guildId, long userId) {
		HashMap<Long, Date> map = guildsMap.get(guildId);
		if (map != null) {
			synchronized (map) {
				SoaLogging.getLogger(this)
						.trace("Caching activity for guild [" + guildId + "], user [" + userId + "] to " + getCacheName());
				map.put(userId, new Date());
			}
		}
	}

	HashMap<Long, Date> getCacheForGuild(long guildId) {
		//Return a copy, so that the original map is not modified.
		return new HashMap<>(guildsMap.get(guildId));
	}

	public abstract void writeCacheToDatabase();

	abstract String getCacheName();

	public void setGuildUserUtility(GuildUserUtility guildUserUtility) {
		this.guildUserUtility = guildUserUtility;
	}

	protected HashMap<Long, HashMap<Long, Date>> getGuildsMap() {
		return guildsMap;
	}

	protected GuildUserUtility getGuildUserUtility() {
		return guildUserUtility;
	}

	int numOfGuilds() {
		return guildsMap.size();
	}
}
