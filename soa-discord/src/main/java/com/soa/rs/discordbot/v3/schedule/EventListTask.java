package com.soa.rs.discordbot.v3.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.ipb.events.IpbEventListParser;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Flux;

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

		List<Consumer<MessageCreateSpec>> createSpecs = new ArrayList<>();
		for (String msg : parser.generateListing()) {
			createSpecs.add(DiscordUtils.createMessageSpecWithMessage(msg));
		}

		Flux.fromIterable(createSpecs).flatMapSequential(
				messageCreateSpecConsumer -> client.getGuilds().flatMap(Guild::getChannels)
						.filter(guildChannel -> guildChannel.getName()
								.equalsIgnoreCase(DiscordCfgFactory.getConfig().getEventListingEvent().getChannel()))
						.filter(guildChannel -> guildChannel.getType().equals(Channel.Type.GUILD_TEXT))
						.map(guildChannel -> ((MessageChannel) guildChannel))
						.flatMap(messageChannel -> messageChannel.createMessage(messageCreateSpecConsumer)))
				.subscribe();
	}

	public void setClient(GatewayDiscordClient client) {
		this.client = client;
	}
}
