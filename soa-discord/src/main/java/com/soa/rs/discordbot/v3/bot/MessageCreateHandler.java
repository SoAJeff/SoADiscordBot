package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.api.command.CommandProcessor;
import com.soa.rs.discordbot.v3.usertrack.RecentCache;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class MessageCreateHandler {

	private final CommandProcessor processor = new CommandProcessor();

	public Mono<Void> handle(MessageCreateEvent event) {
		try {
			return processor.processMessageEvent(event).onErrorResume(throwable -> Mono.fromRunnable(
					() -> SoaLogging.getLogger(this)
							.error("Error when processing message create event: " + throwable.getMessage(), throwable)))
					.then();
		} catch (Exception e) {
			return Mono.error(e);
		}
	}

	public void setLastSeenCache(RecentCache cache) {
		this.processor.setLastSeenCache(cache);
	}

	public void setLastActiveCache(RecentCache cache) {
		this.processor.setLastActiveCache(cache);
	}
}
