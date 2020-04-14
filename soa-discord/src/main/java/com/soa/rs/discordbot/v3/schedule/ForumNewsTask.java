package com.soa.rs.discordbot.v3.schedule;

import java.net.MalformedURLException;
import java.util.Date;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.rssfeeds.ForumNewsListParser;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;

public class ForumNewsTask implements Runnable {

	private GatewayDiscordClient client;
	private ForumNewsListParser parser;

	public boolean initialize() {
		if (DiscordCfgFactory.getConfig().getNewsListingEvent() != null && DiscordCfgFactory.getConfig()
				.getNewsListingEvent().isEnabled()) {
			parser = new ForumNewsListParser();
			DiscordCfgFactory.getInstance().setNewsLastPost(new Date());
			try {
				parser.setUrl(DiscordCfgFactory.getConfig().getNewsListingEvent().getUrl());
			} catch (MalformedURLException e) {
				//Initialization failed.  We have already logged why
				return false;
			}
			return true;
		}
		return false;
	}

	public void setClient(GatewayDiscordClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		String content = parser.parse();

		if (content.trim().length() > 0) {
			client.getGuilds().flatMap(Guild::getChannels).filter(guildChannel -> guildChannel.getName()
					.equalsIgnoreCase(DiscordCfgFactory.getConfig().getNewsListingEvent().getChannel()))
					.filter(guildChannel -> guildChannel.getType().equals(Channel.Type.GUILD_TEXT))
					.map(guildChannel -> ((MessageChannel) guildChannel))
					.flatMap(messageChannel -> DiscordUtils.sendMessage(content, messageChannel)).subscribe();
		}
	}
}
