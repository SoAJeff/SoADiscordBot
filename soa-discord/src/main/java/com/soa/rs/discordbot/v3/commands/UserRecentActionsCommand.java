package com.soa.rs.discordbot.v3.commands;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.UsernameRecentAction;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Command(triggers = { ".user-recent", ".user-recents", ".user-recentactions" })
public class UserRecentActionsCommand extends AbstractCommand {

	private RecentActionUtility recentActionUtility = new RecentActionUtility();

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
		List<UsernameRecentAction> actions;
		if (event.getGuildId().isPresent()) {
			actions = recentActionUtility.getRecentActionsLimitByNWithUsername(num, event.getGuildId().get().asLong());
		} else {
			actions = recentActionUtility.getRecentActionsLimitByNWithUsername(num);
		}
		String replyContent = generateString(actions);
		return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(replyContent))
				.then();
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		return Mono.empty();
	}

	public String generateString(List<UsernameRecentAction> actions) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		StringBuilder sb = new StringBuilder();
		sb.append("**Recent Actions**\n");
		for (UsernameRecentAction action : actions) {
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
			sb.append("\n");
		}
		return sb.toString().trim();
	}
}
