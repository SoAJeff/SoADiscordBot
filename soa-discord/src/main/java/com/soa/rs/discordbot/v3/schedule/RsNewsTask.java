package com.soa.rs.discordbot.v3.schedule;

import java.net.MalformedURLException;
import java.util.List;
import java.util.function.Consumer;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.rssfeeds.RsNewsParser;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

public class RsNewsTask implements Runnable {

	private GatewayDiscordClient client;
	private RsNewsParser parser;

	public boolean initialize() {
		if (DiscordCfgFactory.getConfig().getRsNewsTask() != null && DiscordCfgFactory.getConfig().getRsNewsTask()
				.isEnabled()) {
			parser = new RsNewsParser();
			try {
				parser.setUrl(DiscordCfgFactory.getConfig().getRsNewsTask().getUrl());
			} catch (MalformedURLException e) {
				//Initialization failed.  We have already logged why
				return false;
			}
			parser.initialize();
			return true;
		}
		return false;
	}

	public void setClient(GatewayDiscordClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		List<Consumer<EmbedCreateSpec>> specs = parser.parseAsEmbed();
		for (Consumer<EmbedCreateSpec> spec : specs) {
			client.getGuilds().flatMap(Guild::getChannels).filter(guildChannel -> guildChannel.getName()
					.equalsIgnoreCase(DiscordCfgFactory.getConfig().getRsNewsTask().getChannel()))
					.filter(guildChannel -> guildChannel.getType().equals(Channel.Type.GUILD_TEXT))
					.map(guildChannel -> ((MessageChannel) guildChannel))
					.flatMap(messageChannel -> DiscordUtils.sendMessage(spec, messageChannel)).subscribe();
		}

	}
}
