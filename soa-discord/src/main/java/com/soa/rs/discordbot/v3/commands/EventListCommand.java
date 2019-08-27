package com.soa.rs.discordbot.v3.commands;

import java.net.MalformedURLException;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.rssfeeds.EventListParser;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Command(triggers = { ".events" })
public class EventListCommand extends AbstractCommand {

	private EventListParser parser;

	@Override
	public void initialize() {
		if (DiscordCfgFactory.getConfig().getEventListingEvent().isEnabled()) {
			parser = new EventListParser();
			try {
				parser.setUrl(DiscordCfgFactory.getConfig().getEventListingEvent().getUrl());
				addHelpMsg(".events", "Displays an up-to-date listing of today's events.");
				setEnabled(true);
			} catch (MalformedURLException e) {
				setEnabled(false);
			}
		} else {
			setEnabled(false);
		}

	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		String content = parser.parse();
		return event.getMessage().getChannel().flatMap(channel -> DiscordUtils.sendMessage(content, channel)).then();
	}
}
