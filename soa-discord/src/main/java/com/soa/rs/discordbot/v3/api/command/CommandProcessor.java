package com.soa.rs.discordbot.v3.api.command;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.usertrack.RecentCache;
import com.soa.rs.discordbot.v3.util.SoaDiscordBotConstants;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

public class CommandProcessor {

	private RecentCache lastSeenCache;
	private RecentCache lastActiveCache;

	public Mono<Void> processMessageEvent(MessageCreateEvent event) {
		String content = event.getMessage().getContent().orElse("").trim().toLowerCase();
		User member = event.getMessage().getAuthor().orElse(null);

		if (member != null && !member.isBot() && !(content.isEmpty())) {

			//Update the cache
			if (event.getGuildId().isPresent() && DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
				if (lastSeenCache != null) {
					lastSeenCache.updateCacheForGuildUser(event.getGuildId().get().asLong(), member.getId().asLong());
				}
				if (lastActiveCache != null) {
					lastActiveCache.updateCacheForGuildUser(event.getGuildId().get().asLong(), member.getId().asLong());
				}
			}

			if (content.startsWith(SoaDiscordBotConstants.BOT_PREFIX) || content.toLowerCase()
					.startsWith(SoaDiscordBotConstants.RUNEINFO_PREFIX)) {
				String cmd = content.split(" ")[0];
				SoaLogging.getLogger(this).info("cmd is: " + cmd);
				if (CommandInitializer.getCommand(cmd.toLowerCase()) != null) {
					SoaLogging.getLogger(CommandProcessor.class)
							.info("Executing command [" + CommandInitializer.getCommand(cmd).getClass().getSimpleName()
									+ "]");
					return (CommandInitializer.getCommand(cmd).execute(event));
				} else {
					return Mono.empty();
				}
			} else {
				//Handle the 'everything events'
				for (String key : CommandInitializer.getAnyMessageMap().keySet()) {
					if (content.contains(key.toLowerCase())) {
						SoaLogging.getLogger(CommandProcessor.class)
								.info("Executing msgRcvEvent [" + CommandInitializer.getMsgRcvd(key).getClass()
										.getSimpleName() + "]");
						return (CommandInitializer.getMsgRcvd(key).execute(event));
					}
				}
				return Mono.empty();
			}
		} else {
			return Mono.empty();
		}
	}

	public void setLastSeenCache(RecentCache cache) {
		this.lastSeenCache = cache;
	}

	public void setLastActiveCache(RecentCache cache) {
		this.lastActiveCache = cache;
	}
}
