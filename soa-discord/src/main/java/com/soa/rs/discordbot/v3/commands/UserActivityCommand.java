package com.soa.rs.discordbot.v3.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Command(triggers = { ".useractivity", ".user-activity" })
public class UserActivityCommand extends AbstractCommand {

	private static final String USERACTIVITY = ".useractivity";
	private static final String USER_ACTIVITY = ".user-activity";
	private final int USERACTIVITY_LEN = USERACTIVITY.length();
	private final int USER_ACTIVITY_LEN = USER_ACTIVITY.length();
	private GuildUserUtility guildUserUtility = new GuildUserUtility();

	@Override
	public void initialize() {
		setEnabled(DiscordCfgFactory.getInstance().isUserTrackingEnabled());
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		if (event.getGuildId().isPresent()) {
			List<String> names = parseNames(event.getMessage().getContent().orElse(""));
			if (names.size() == 0) {
				return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel
						.createMessage("No names were provided to search for activity results.")).then();
			}
			List<String> parsedActivity = guildUserUtility
					.getUserActivityDatesForUsername(names, event.getGuildId().get().asLong());
			List<Consumer<MessageCreateSpec>> specs = createMsgSpecs(createParsedOutput(parsedActivity));
			return Flux.fromIterable(specs).flatMapSequential(
					s -> event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(s)))
					.then();
		} else
			return event.getMessage().getChannel()
					.flatMap(messageChannel -> messageChannel.createMessage("Please send this message in a guild."))
					.then();
	}

	List<String> parseNames(String names) {
		List<String> parsedNames = new ArrayList<>();
		int length = 0;
		if (names.toLowerCase().startsWith(USERACTIVITY))
			length = USERACTIVITY_LEN;
		else if (names.toLowerCase().startsWith(USER_ACTIVITY))
			length = USER_ACTIVITY_LEN;

		try {
			names = names.substring(length + 1);
		} catch (StringIndexOutOfBoundsException e) {
			return parsedNames;
		}

		String[] splitNames = names.split(System.lineSeparator());
		for (String name : splitNames) {
			if (name.contains("~")) {
				int idx = name.indexOf("~");
				String finalName = name.substring(0, idx).trim();
				if (finalName.length() <= 12) {
					parsedNames.add(name.substring(0, idx).trim());
				}
			} else {
				if (name.length() <= 12)
					parsedNames.add(name.trim());
			}
		}
		return parsedNames;
	}

	List<String> createParsedOutput(List<String> output) {
		List<String> messages = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		sb.append("**Activity data for provided users:**");
		sb.append(System.lineSeparator());
		for (String s : output) {
			sb.append("-> ");
			sb.append(s);
			sb.append(System.lineSeparator());
			if (sb.length() > 1500) {
				messages.add(sb.toString().trim());
				sb.setLength(0);
			}
		}
		messages.add(sb.toString().trim());
		return messages;
	}

	private List<Consumer<MessageCreateSpec>> createMsgSpecs(List<String> messages) {
		List<Consumer<MessageCreateSpec>> specs = new ArrayList<>();
		for (String message : messages)
			specs.add(DiscordUtils.createMessageSpecWithMessage(message));
		return specs;
	}
}
