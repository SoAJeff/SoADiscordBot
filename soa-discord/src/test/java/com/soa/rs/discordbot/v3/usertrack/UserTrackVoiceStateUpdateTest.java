package com.soa.rs.discordbot.v3.usertrack;

import org.junit.Test;
import org.mockito.Mockito;

import discord4j.rest.util.Snowflake;

public class UserTrackVoiceStateUpdateTest {

	@Test
	public void handleVoiceStateUpdate()
	{
		RecentCache cache = Mockito.mock(RecentCache.class);
		RecentCache lastActiveCache = Mockito.mock(RecentCache.class);

		UserTrackVoiceStateUpdate update = new UserTrackVoiceStateUpdate();
		update.setLastSeenCache(cache);
		update.setLastActiveCache(lastActiveCache);

		Snowflake user = Snowflake.of(1234L);
		Snowflake guild = Snowflake.of(6789L);

		update.handleVoiceStateUpdate(user, guild);

		Mockito.verify(cache).updateCacheForGuildUser(Mockito.eq(6789L), Mockito.eq(1234L));
		Mockito.verify(lastActiveCache).updateCacheForGuildUser(Mockito.eq(6789L), Mockito.eq(1234L));
	}
}
