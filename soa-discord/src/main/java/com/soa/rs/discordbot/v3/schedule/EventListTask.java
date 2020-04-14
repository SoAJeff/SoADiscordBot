package com.soa.rs.discordbot.v3.schedule;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.ipb.events.IpbEventListParser;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;

public class EventListTask implements Runnable {

	private GatewayDiscordClient client;
	private IpbEventListParser parser;

	public boolean initialize() {
		if (DiscordCfgFactory.getConfig().getEventListingEvent() != null && DiscordCfgFactory.getConfig()
				.getEventListingEvent().isEnabled()) {
			parser = new IpbEventListParser();
			return true;
		}
		return false;
	}

	@Override
	public void run() {

		String content = parser.generateListing();

		if (content.trim().length() > 0) {
			client.getGuilds().flatMap(Guild::getChannels).filter(guildChannel -> guildChannel.getName()
					.equalsIgnoreCase(DiscordCfgFactory.getConfig().getEventListingEvent().getChannel()))
					.filter(guildChannel -> guildChannel.getType().equals(Channel.Type.GUILD_TEXT))
					.map(guildChannel -> ((MessageChannel) guildChannel))
					.flatMap(messageChannel -> DiscordUtils.sendMessage(content, messageChannel)).subscribe();
		}
	}

	public void setClient(GatewayDiscordClient client) {
		this.client = client;
	}
}
