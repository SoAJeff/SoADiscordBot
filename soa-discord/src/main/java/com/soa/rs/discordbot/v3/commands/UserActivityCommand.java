package com.soa.rs.discordbot.v3.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.object.component.TextInput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Command(triggers = { ".useractivity", ".user-activity" })
@Interaction(trigger = "useractivity")
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
			List<String> names = parseNames(Optional.of(event.getMessage().getContent()).orElse("").trim());
			if (names.size() == 0) {
				return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel
						.createMessage("No names were provided to search for activity results.")).then();
			}
			List<String> parsedActivity = guildUserUtility
					.getUserActivityDatesForUsername(names, event.getGuildId().get().asLong());
			List<String> properOutput = createParsedOutput(parsedActivity);
			return Flux.fromIterable(properOutput).flatMapSequential(
					s -> event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(s)))
					.then();
		} else
			return event.getMessage().getChannel()
					.flatMap(messageChannel -> messageChannel.createMessage("Please send this message in a guild."))
					.then();
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		if(!event.getInteraction().getGuildId().isPresent())
		{
			return event.reply("Please send this message in a guild.").withEphemeral(true).then();
		}
		List<LayoutComponent> modalComponents = new ArrayList<>();
		modalComponents.add(ActionRow.of(TextInput.paragraph("activtytext",
				"Enter list of users, one per line", 1, 4000).required()));
		return event.presentModal("User Activity", "useractivity", modalComponents).then();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event)
	{
		TextInput activityComponent = event.getComponents(TextInput.class).get(0);

		return event.deferReply().withEphemeral(true)
				.then(Flux.fromIterable(createParsedOutput(parseNames(activityComponent.getValue().get()), event.getInteraction().getGuildId().get()))
								.flatMapSequential(s -> event.createFollowup(s).withEphemeral(true)).then()).then();
	}

	List<String> createParsedOutput(List<String> names, Snowflake guildId)
	{
		List<String> parsedActivity = guildUserUtility
				.getUserActivityDatesForUsername(names, guildId.asLong());
		return createParsedOutput(parsedActivity);
	}

	List<String> parseNames(String names) {
		List<String> parsedNames = new ArrayList<>();
		int length = 0;
		if (names.toLowerCase().startsWith(USERACTIVITY))
			length = USERACTIVITY_LEN;
		else if (names.toLowerCase().startsWith(USER_ACTIVITY))
			length = USER_ACTIVITY_LEN;

		if(length != 0) {
			try {
				names = names.substring(length + 1);
			} catch (StringIndexOutOfBoundsException e) {
				return parsedNames;
			}
		}

		//String[] splitNames = names.split(System.lineSeparator());
		//Discord seems to now send \n no matter what platform its running on.
		String[] splitNames = names.split("\n");
		SoaLogging.getLogger(this).debug("Names array is of length " + splitNames.length);

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

}
