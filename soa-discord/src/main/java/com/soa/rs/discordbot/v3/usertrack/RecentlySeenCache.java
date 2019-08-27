package com.soa.rs.discordbot.v3.usertrack;

import java.util.Date;
import java.util.HashMap;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.util.SoaLogging;

public class RecentlySeenCache {
	private final HashMap<Long, HashMap<Long, Date>> guildsMap = new HashMap<>();
	private GuildUserUtility guildUserUtility;

	public RecentlySeenCache() {
	}

	public void addNewGuild(long id) {
		SoaLogging.getLogger(this).debug("Adding guild [" + id + "] to cache");
		guildsMap.put(id, new HashMap<>());
	}

	public void updateCacheForGuildUser(long guildId, long userId) {
		HashMap<Long, Date> map = guildsMap.get(guildId);
		if (map != null) {
			synchronized (map) {
				SoaLogging.getLogger(this)
						.trace("Caching lastActive for guild [" + guildId + "], user [" + userId + "]");
				map.put(userId, new Date());
			}
		}
	}

	HashMap<Long, Date> getCacheForGuild(long guildId) {
		//Return a copy, so that the original map is not modified.
		return new HashMap<>(guildsMap.get(guildId));
	}

	public void writeCacheToDatabase() {
		for (long guildId : guildsMap.keySet()) {
			HashMap<Long, Date> map = guildsMap.get(guildId);
			synchronized (map) {
				if (map.size() > 0) {
					try {
						SoaLogging.getLogger(this).debug("Writing cache to database for guild [" + guildId + "], containing " + map.size() + " entries.");
						this.guildUserUtility.updateLastSeenForUser(guildId, map);
					} catch (Exception e) {
						SoaLogging.getLogger(this)
								.error("Unexpected error encountered when flushing data to database.", e);
					}
					//We have written everything, clear the map before re-allowing access
					SoaLogging.getLogger(this)
							.trace("Finished writing cache for guild [" + guildId + "], clearing map");
					map.clear();
				} else {
					SoaLogging.getLogger(this).trace("No entries in cache for [" + guildId + "]");
				}
			}
		}
	}

	public void setGuildUserUtility(GuildUserUtility guildUserUtility) {
		this.guildUserUtility = guildUserUtility;
	}

	int numOfGuilds() {
		return guildsMap.size();
	}
}
