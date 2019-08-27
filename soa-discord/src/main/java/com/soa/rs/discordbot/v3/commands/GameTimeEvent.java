package com.soa.rs.discordbot.v3.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Command(triggers = { ".gametime" })
public class GameTimeEvent extends AbstractCommand {
	@Override
	public void initialize() {
		//Always enabled
		setEnabled(true);
		addHelpMsg(".gametime", "Displays the current RS Game Time (UTC)");
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return event.getMessage().getChannel().flatMap(messageChannel -> DiscordUtils
				.sendMessage("Game time is currently: " + sdf.format(new Date()), messageChannel)).then();
	}
}
