package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.usertrack.RecentCache;

import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

public class TypingStartHandler {

	private RecentCache cache;

	public Mono<Void> handleTypingStart(TypingStartEvent event) {
		Snowflake channelSnowflake = event.getChannelId();
		return event.getClient().getGuilds().flatMap(Guild::getChannels)
				.filter(guildChannel -> guildChannel.getId().equals(channelSnowflake)).flatMap(GuildChannel::getGuild)
				.flatMap(guild -> Mono.fromRunnable(
						() -> cache.updateCacheForGuildUser(guild.getId().asLong(), event.getUserId().asLong())))
				.then();
	}

	public void setCache(RecentCache cache) {
		this.cache = cache;
	}
}
