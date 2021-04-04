package com.soa.rs.discordbot.v3.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.ipb.events.IpbEventListParser;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Command(triggers = { ".events" })
public class EventListCommand extends AbstractCommand {

	private IpbEventListParser parser;

	@Override
	public void initialize() {
		if (DiscordCfgFactory.getConfig().getEventListingEvent().isEnabled()) {
			parser = new IpbEventListParser();
			addHelpMsg(".events", "Displays an up-to-date listing of today's events.");
			setEnabled(true);
		} else {
			setEnabled(false);
		}

	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		List<Consumer<MessageCreateSpec>> createSpecs = new ArrayList<>();
		for (String msg : parser.generateListing()) {
			createSpecs.add(DiscordUtils.createMessageSpecWithMessage(msg));
		}
		return Flux.fromIterable(createSpecs).flatMapSequential(messageCreateSpecConsumer -> event.getMessage().getChannel()
				.flatMap(messageChannel -> messageChannel.createMessage(messageCreateSpecConsumer))).then();
	}
}
