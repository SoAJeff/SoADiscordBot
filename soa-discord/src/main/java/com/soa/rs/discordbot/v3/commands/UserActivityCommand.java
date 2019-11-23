package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Command(triggers={".useractivity", ".user-activity"})
public class UserActivityCommand extends AbstractCommand {

	@Override
	public void initialize() {

	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return null;
	}
}
