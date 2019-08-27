package com.soa.rs.discordbot.v3.schedule;

import java.net.MalformedURLException;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.rssfeeds.EventListParser;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.MessageChannel;
import reactor.core.publisher.Mono;

public class EventListTask implements Runnable {

	private DiscordClient client;
	private EventListParser parser;

	public boolean initialize() {
		if (DiscordCfgFactory.getConfig().getEventListingEvent() != null && DiscordCfgFactory.getConfig()
				.getEventListingEvent().isEnabled()) {
			parser = new EventListParser();
			try {
				parser.setUrl(DiscordCfgFactory.getConfig().getEventListingEvent().getUrl());
			} catch (MalformedURLException e) {
				//Initialization failed.  We have already logged why
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public void run() {

		String content = parser.parse();

		if (content.trim().length() > 0) {
			client.getGuilds().flatMap(Guild::getChannels).filter(guildChannel -> guildChannel.getName()
					.equalsIgnoreCase(DiscordCfgFactory.getConfig().getEventListingEvent().getChannel()))
					.filter(guildChannel -> guildChannel.getType().equals(Channel.Type.GUILD_TEXT))
					.map(guildChannel -> ((MessageChannel) guildChannel))
					.flatMap(messageChannel -> DiscordUtils.sendMessage(content, messageChannel)).subscribe();
		}
	}

	public void setClient(DiscordClient client) {
		this.client = client;
	}
}
