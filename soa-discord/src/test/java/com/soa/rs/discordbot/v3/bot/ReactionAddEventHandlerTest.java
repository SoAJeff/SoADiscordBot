package com.soa.rs.discordbot.v3.bot;

import java.util.Optional;

import com.soa.rs.discordbot.v3.usertrack.RecentlySeenCache;

import org.junit.Test;
import org.mockito.Mockito;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

public class ReactionAddEventHandlerTest {

	@Test
	public void testReactionAddEventWithGuild() {
		RecentlySeenCache cache = Mockito.mock(RecentlySeenCache.class);

		ReactionAddEventHandler handler = new ReactionAddEventHandler();
		handler.setCache(cache);

		ReactionAddEvent event = Mockito.mock(ReactionAddEvent.class);
		MessageChannel channel = Mockito.mock(MessageChannel.class);
		Mockito.when(event.getGuildId()).thenReturn(Optional.of(Snowflake.of(6789L)));
		Mockito.when(event.getUserId()).thenReturn(Snowflake.of(1234L));
		Mockito.when(event.getChannel()).thenReturn(Mono.just(channel));

		handler.handle(event).block();
		Mockito.verify(cache).updateCacheForGuildUser(6789L, 1234L);
	}

	@Test
	public void testReactionAddEventWithoutGuild() {
		RecentlySeenCache cache = Mockito.mock(RecentlySeenCache.class);

		ReactionAddEventHandler handler = new ReactionAddEventHandler();
		handler.setCache(cache);

		ReactionAddEvent event = Mockito.mock(ReactionAddEvent.class);
		MessageChannel channel = Mockito.mock(MessageChannel.class);
		Mockito.when(event.getGuildId()).thenReturn(Optional.empty());
		Mockito.when(event.getUserId()).thenReturn(Snowflake.of(1234L));
		Mockito.when(event.getChannel()).thenReturn(Mono.just(channel));

		handler.handle(event).block();
		Mockito.verify(cache, Mockito.never()).updateCacheForGuildUser(6789L, 1234L);
	}
}
