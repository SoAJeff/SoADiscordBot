package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.usertrack.RecentCache;
import com.soa.rs.discordbot.v3.usertrack.UserTrackVoiceStateUpdate;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import reactor.core.publisher.Mono;

public class VoiceStateUpdateHandler {

	private final UserTrackVoiceStateUpdate userTrackVoiceStateUpdate = new UserTrackVoiceStateUpdate();

	public Mono<Void> handle(VoiceStateUpdateEvent event) {
		return event.getCurrent().getUser().filter(ignored -> DiscordCfgFactory.getInstance().isUserTrackingEnabled())
				.filter(user -> !user.isBot()).flatMap(user -> Mono.fromRunnable(() -> userTrackVoiceStateUpdate
						.handleVoiceStateUpdate(user.getId(), event.getCurrent().getGuildId()))).then();
	}

	public void setLastSeenCache(RecentCache cache) {
		this.userTrackVoiceStateUpdate.setLastSeenCache(cache);
	}

	public void setLastActiveCache(RecentCache cache) {
		this.userTrackVoiceStateUpdate.setLastActiveCache(cache);
	}

}
