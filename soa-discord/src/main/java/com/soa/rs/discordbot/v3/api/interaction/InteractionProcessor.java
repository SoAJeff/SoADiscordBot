package com.soa.rs.discordbot.v3.api.interaction;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.usertrack.RecentCache;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
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

	public Mono<Void> processModal(ModalSubmitInteractionEvent event)
	{
		String customId = getCustomIdForEvent(event);
		if(InteractionInitializer.getInteraction(customId) != null)
		{
			return InteractionInitializer.getInteraction(customId).execute(event);
		}
		else
			return Mono.empty();

	}

	public Mono<Void> processButton(ButtonInteractionEvent event)
	{
		String customId = getCustomIdForEvent(event);
		if(InteractionInitializer.getInteraction(customId) != null)
		{
			return InteractionInitializer.getInteraction(customId).execute(event);
		}
		else
			return Mono.empty();

	}

	private String getCustomIdForEvent(ComponentInteractionEvent event) {
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

		String customId = event.getCustomId();
		SoaLogging.getLogger(this).info("Received ComponentEvent with custom ID " + customId);
		if (customId.contains("-"))
		{
			customId = customId.substring(0, customId.indexOf("-"));
			SoaLogging.getLogger(this).info("Searching for ComponentEvent whose interaction begins with " + customId);
		}
		return customId;
	}

	public void setLastSeenCache(RecentCache cache) {
		this.lastSeenCache = cache;
	}

	public void setLastActiveCache(RecentCache cache) {
		this.lastActiveCache = cache;
	}
}
