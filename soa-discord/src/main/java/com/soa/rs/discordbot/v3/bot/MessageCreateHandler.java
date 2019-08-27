package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.api.command.CommandProcessor;
import com.soa.rs.discordbot.v3.usertrack.RecentlySeenCache;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class MessageCreateHandler {

	private CommandProcessor processor = new CommandProcessor();

	public Mono<Void> handle(MessageCreateEvent event) {
		return processor.processMessageEvent(event);
	}

	public void setCache(RecentlySeenCache cache) {
		this.processor.setCache(cache);
	}
}
