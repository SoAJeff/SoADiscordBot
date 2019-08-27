package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.usertrack.RecentlySeenCache;
import com.soa.rs.discordbot.v3.usertrack.UserTrackVoiceStateUpdate;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import reactor.core.publisher.Mono;

public class VoiceStateUpdateHandler {

	private UserTrackVoiceStateUpdate userTrackVoiceStateUpdate = new UserTrackVoiceStateUpdate();

	public Mono<Void> handle(VoiceStateUpdateEvent event) {
		return event.getCurrent().getUser().filter(ignored -> DiscordCfgFactory.getInstance().isUserTrackingEnabled())
				.filter(user -> !user.isBot()).flatMap(user -> Mono.fromRunnable(() -> userTrackVoiceStateUpdate
						.handleVoiceStateUpdate(user.getId(), event.getCurrent().getGuildId()))).then();
	}

	public void setCache(RecentlySeenCache cache) {
		this.userTrackVoiceStateUpdate.setCache(cache);
	}

}
