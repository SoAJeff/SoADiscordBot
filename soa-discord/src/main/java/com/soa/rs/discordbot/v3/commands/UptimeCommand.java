package com.soa.rs.discordbot.v3.commands;

import java.time.Duration;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.util.UptimeUtility;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Command(triggers = { ".uptime" })
public class UptimeCommand extends AbstractCommand {
	@Override
	public void initialize() {
		setEnabled(true);
		addHelpMsg(".uptime", "Get the bot's uptime.");
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		Duration uptime = UptimeUtility.getUptime();
		String msg = "Uptime: " + uptime.toDays() + " days, " + (uptime.toHours() % 24) + " hours, " + (uptime.toMinutes()
				% 60) + " minutes";

		return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(msg)).then();
	}
}
