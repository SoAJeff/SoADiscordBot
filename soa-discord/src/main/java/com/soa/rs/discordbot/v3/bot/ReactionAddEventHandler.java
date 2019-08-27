package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.usertrack.RecentlySeenCache;
import com.soa.rs.discordbot.v3.usertrack.UserTrackReactionUpdate;

import discord4j.core.event.domain.message.ReactionAddEvent;
import reactor.core.publisher.Mono;

public class ReactionAddEventHandler {

	private UserTrackReactionUpdate userTrackReactionUpdate = new UserTrackReactionUpdate();

	public Mono<Void> handle(ReactionAddEvent event) {
		return event.getChannel().filter(ignored -> event.getGuildId().isPresent()).flatMap(ignored -> Mono
				.fromRunnable(() -> userTrackReactionUpdate.handle(event.getGuildId().get(), event.getUserId())))
				.then();
	}

	public void setCache(RecentlySeenCache cache) {
		this.userTrackReactionUpdate.setCache(cache);
	}
}
