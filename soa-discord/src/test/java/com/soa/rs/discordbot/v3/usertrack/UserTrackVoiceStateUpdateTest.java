package com.soa.rs.discordbot.v3.usertrack;

import org.junit.Test;
import org.mockito.Mockito;

import discord4j.core.object.util.Snowflake;

public class UserTrackVoiceStateUpdateTest {

	@Test
	public void handleVoiceStateUpdate()
	{
		RecentlySeenCache cache = Mockito.mock(RecentlySeenCache.class);

		UserTrackVoiceStateUpdate update = new UserTrackVoiceStateUpdate();
		update.setCache(cache);

		Snowflake user = Snowflake.of(1234L);
		Snowflake guild = Snowflake.of(6789L);

		update.handleVoiceStateUpdate(user, guild);

		Mockito.verify(cache).updateCacheForGuildUser(Mockito.eq(6789L), Mockito.eq(1234L));
	}
}
