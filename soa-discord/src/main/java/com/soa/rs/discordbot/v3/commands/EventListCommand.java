package com.soa.rs.discordbot.v3.commands;

import java.util.List;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.ipb.events.IpbEventListParser;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Command(triggers = { ".events" })
@Interaction(trigger="events")
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
		List<String> events = parser.generateListing();
		return Flux.fromIterable(events).flatMapSequential(
				s -> event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(s))).then();
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		return event.deferReply().then(getEvents(event));
	}

	private Mono<Void> getEvents(ChatInputInteractionEvent event) {
		List<String> events = parser.generateListing();
		return Flux.fromIterable(events).flatMapSequential(event::createFollowup).then();
	}
}
