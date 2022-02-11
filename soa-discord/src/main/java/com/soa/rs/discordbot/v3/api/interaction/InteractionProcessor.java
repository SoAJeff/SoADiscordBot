package com.soa.rs.discordbot.v3.api.interaction;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.usertrack.RecentCache;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

public class InteractionProcessor {

	private RecentCache lastSeenCache;
	private RecentCache lastActiveCache;

	public Mono<Void> processInteraction(ChatInputInteractionEvent event)
	{
		User member = event.getInteraction().getUser();

		if(!member.isBot())
		{
			if (event.getInteraction().getGuildId().isPresent() && DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
				if (lastSeenCache != null) {
					lastSeenCache.updateCacheForGuildUser(event.getInteraction().getGuildId().get().asLong(),
							member.getId().asLong());
				}
				if (lastActiveCache != null) {
					lastActiveCache.updateCacheForGuildUser(event.getInteraction().getGuildId().get().asLong(),
							member.getId().asLong());
				}
			}
		}

		SoaLogging.getLogger(this).info("Received interaction: " + event.getCommandName());
		if(InteractionInitializer.getInteraction(event.getCommandName())!=null)
		{
			return InteractionInitializer.getInteraction(event.getCommandName()).execute(event);
		}
		else
			return Mono.empty();
	}

	public void setLastSeenCache(RecentCache cache) {
		this.lastSeenCache = cache;
	}

	public void setLastActiveCache(RecentCache cache) {
		this.lastActiveCache = cache;
	}
}
