package com.soa.rs.discordbot.v3.usertrack;

import java.util.Date;
import java.util.HashMap;

import com.soa.rs.discordbot.v3.util.SoaLogging;

public class LastSeenCache extends RecentCache {

	public void writeCacheToDatabase() {
		for (long guildId : getGuildsMap().keySet()) {
			HashMap<Long, Date> map = getGuildsMap().get(guildId);
			synchronized (map) {
				if (map.size() > 0) {
					try {
						SoaLogging.getLogger(this).debug("Writing last seen cache to database for guild [" + guildId + "], containing " + map.size() + " entries.");
						getGuildUserUtility().updateLastSeenForUser(guildId, map);
					} catch (Exception e) {
						SoaLogging.getLogger(this)
								.error("Unexpected error encountered when flushing data to database.", e);
					}
					//We have written everything, clear the map before re-allowing access
					SoaLogging.getLogger(this)
							.trace("Finished writing last seen cache for guild [" + guildId + "], clearing map");
					map.clear();
				} else {
					SoaLogging.getLogger(this).trace("No entries in last seen cache for [" + guildId + "]");
				}
			}
		}
	}

	@Override
	String getCacheName() {
		return "LastSeenCache";
	}
}
