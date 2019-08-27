package com.soa.rs.discordbot.v3.usertrack;

import org.junit.Test;
import org.mockito.Mockito;

import discord4j.core.object.util.Snowflake;

public class UserTrackReactionUpdateTest {

	@Test
	public void testUserTrackReactionAdd() {
		RecentlySeenCache cache = Mockito.mock(RecentlySeenCache.class);
		UserTrackReactionUpdate utru = new UserTrackReactionUpdate();
		utru.setCache(cache);

		utru.handle(Snowflake.of(6789L), Snowflake.of(1234L));

		Mockito.verify(cache).updateCacheForGuildUser(6789L, 1234L);
	}
}
