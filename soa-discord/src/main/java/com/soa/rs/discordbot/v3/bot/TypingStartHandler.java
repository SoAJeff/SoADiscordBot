package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.usertrack.RecentCache;

import discord4j.core.event.domain.channel.TypingStartEvent;
import reactor.core.publisher.Mono;

public class TypingStartHandler {

	private RecentCache cache;

	public Mono<Void> handleTypingStart(TypingStartEvent event) {
		return Mono.fromRunnable(() -> event.getGuildId()
				.ifPresent(snowflake -> cache.updateCacheForGuildUser(snowflake.asLong(), event.getUserId().asLong())))
				.then();
	}

	public void setCache(RecentCache cache) {
		this.cache = cache;
	}
}
