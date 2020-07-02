package com.soa.rs.discordbot.v3.bot;

import java.util.Optional;

import com.soa.rs.discordbot.v3.usertrack.RecentCache;

import org.junit.Test;
import org.mockito.Mockito;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class ReactionAddEventHandlerTest {

	@Test
	public void testReactionAddEventWithGuild() {
		RecentCache cache = Mockito.mock(RecentCache.class);
		RecentCache lastActiveCache = Mockito.mock(RecentCache.class);

		ReactionAddEventHandler handler = new ReactionAddEventHandler();
		handler.setLastSeenCache(cache);
		handler.setLastActiveCache(lastActiveCache);

		ReactionAddEvent event = Mockito.mock(ReactionAddEvent.class);
		MessageChannel channel = Mockito.mock(MessageChannel.class);
		Mockito.when(event.getGuildId()).thenReturn(Optional.of(Snowflake.of(6789L)));
		Mockito.when(event.getUserId()).thenReturn(Snowflake.of(1234L));
		Mockito.when(event.getChannel()).thenReturn(Mono.just(channel));

		handler.handle(event).block();
		Mockito.verify(cache).updateCacheForGuildUser(6789L, 1234L);
		Mockito.verify(lastActiveCache).updateCacheForGuildUser(6789L, 1234L);
	}

	@Test
	public void testReactionAddEventWithoutGuild() {
		RecentCache cache = Mockito.mock(RecentCache.class);
		RecentCache lastActiveCache = Mockito.mock(RecentCache.class);

		ReactionAddEventHandler handler = new ReactionAddEventHandler();
		handler.setLastSeenCache(cache);
		handler.setLastActiveCache(lastActiveCache);

		ReactionAddEvent event = Mockito.mock(ReactionAddEvent.class);
		MessageChannel channel = Mockito.mock(MessageChannel.class);
		Mockito.when(event.getGuildId()).thenReturn(Optional.empty());
		Mockito.when(event.getUserId()).thenReturn(Snowflake.of(1234L));
		Mockito.when(event.getChannel()).thenReturn(Mono.just(channel));

		handler.handle(event).block();
		Mockito.verify(cache, Mockito.never()).updateCacheForGuildUser(6789L, 1234L);
	}
}
