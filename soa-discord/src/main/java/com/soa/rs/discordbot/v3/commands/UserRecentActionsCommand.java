package com.soa.rs.discordbot.v3.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.UsernameRecentAction;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Command(triggers = { ".user-recent", ".user-recents", ".user-recentactions" })
@Interaction(trigger = "userrecentactions")
public class UserRecentActionsCommand extends AbstractCommand {

	private final RecentActionUtility recentActionUtility = new RecentActionUtility();

	@Override
	public void initialize() {
		setEnabled(DiscordCfgFactory.getInstance().isUserTrackingEnabled());
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		String[] args = event.getMessage().getContent().trim().split(" ");
		int num = 15;
		if (args.length == 2) {
			try {
				num = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				//Do nothing
			}
		}
		return Flux.fromIterable(lookupRecentActions(num,event.getGuildId())).flatMapSequential(
				s -> event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(s))).then();
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		int num = event.getOption("number").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asLong).orElse(15L).intValue();

		return event.deferReply().withEphemeral(true)
				.then(Flux.fromIterable(lookupRecentActions(num, event.getInteraction().getGuildId()))
						.flatMapSequential(s -> event.createFollowup(s).withEphemeral(true)).then()).then();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ButtonInteractionEvent event) {
		return Mono.empty();
	}

	public List<String> lookupRecentActions(int num, Optional<Snowflake> guildId) {
		List<UsernameRecentAction> actions;
		if (guildId.isPresent()) {
			actions = recentActionUtility.getRecentActionsLimitByNWithUsername(num, guildId.get().asLong());
		} else {
			actions = recentActionUtility.getRecentActionsLimitByNWithUsername(num);
		}
		return generateString(actions);
	}

	public List<String> generateString(List<UsernameRecentAction> actions) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		StringBuilder sb = new StringBuilder();
		List<String> actionsList = new ArrayList<>();
		sb.append("**Recent Actions**\n");
		Iterator<UsernameRecentAction> iter = actions.iterator();
		while(iter.hasNext()) {
			UsernameRecentAction action = iter.next();
			sb.append("On ");
			sb.append(sdf.format(action.getDate()));
			sb.append(", ");
			sb.append(action.getUsername());
			sb.append(" ");
			sb.append(action.getAction());
			if (action.getOriginalValue() != null && !action.getOriginalValue().isEmpty()) {
				sb.append(", Original Value: ");
				sb.append(action.getOriginalValue());
			}
			if (action.getNewValue() != null && !action.getNewValue().isEmpty()) {
				sb.append(", New Value: ");
				sb.append(action.getNewValue());
			}
			if(iter.hasNext()) {
				if (sb.length() > 1700) {
					actionsList.add(sb.toString());
					sb.setLength(0);
				} else {
					sb.append("\n");
				}
			}
		}
		actionsList.add(sb.toString());
		return actionsList;
	}
}
