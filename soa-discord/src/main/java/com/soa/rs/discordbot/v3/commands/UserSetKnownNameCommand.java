package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildNicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Command(triggers = { ".user-setname" })
public class UserSetKnownNameCommand extends AbstractCommand {

	private GuildNicknameUtility guildNicknameUtility = new GuildNicknameUtility();
	private GuildUserUtility guildUserUtility = new GuildUserUtility();
	private final String thumbsUp = "👍";

	@Override
	public void initialize() {
		setEnabled(DiscordCfgFactory.getInstance().isUserTrackingEnabled());
		setMustHavePermission(DiscordCfgFactory.getConfig().getUserTrackingEvent().getCanUpdateQuery().getRole());
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		if (event.getMember().isPresent()) {
			Search search;
			try {
				search = determineSearch(event);
			} catch (Exception e) {
				return event.getMessage().getChannel()
						.flatMap(messageChannel -> messageChannel.createMessage(e.getMessage())).then();
			}

			if (search.getSearch() == null) {
				if (event.getMessage().getUserMentionIds().size() > 0) {
					return permittedToExecuteEvent(event.getMember().get())
							.flatMapIterable(ignored -> event.getMessage().getUserMentions()).flatMap(user -> Flux
									.fromIterable(guildUserUtility
											.getGuildUser("@" + user.getUsername() + "#" + user.getDiscriminator())))
							.flatMap(guildUser -> Mono.fromRunnable(() -> {
								guildUser.setKnownName(search.getName());
								guildUserUtility.updateExistingUser(guildUser);
							})).then(event.getMessage().addReaction(ReactionEmoji.unicode(thumbsUp)));
				} else {
					return event.getMessage().getChannel()
							.flatMap(messageChannel -> messageChannel.createMessage("Invalid arguments")).then();
				}
			}
			return permittedToExecuteEvent(event.getMember().get())
					.flatMapMany(ignored -> Flux.fromIterable(guildNicknameUtility.getGuildUser(search.getSearch())))
					.flatMap(guildUser -> Mono.fromRunnable(() -> {
						guildUser.setKnownName(search.getName());
						guildUserUtility.updateExistingUser(guildUser);
					})).then(event.getMessage().addReaction(ReactionEmoji.unicode(thumbsUp)));
		} else
			return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(
					"Sorry, this command can only be used in a guild by those with the appropriate role")).then();
	}

	public Search determineSearch(MessageCreateEvent event) throws Exception {
		String search = null;
		String name = null;
		int i = 1;
		String[] args = event.getMessage().getContent().trim().split(" ");

		StringBuilder sb = new StringBuilder();

		while (i < args.length) {
			if (args[i].equalsIgnoreCase("-search")) {
				i++;
				while (args.length > i && !args[i].equalsIgnoreCase("-name")) {
					sb.append(args[i]);
					sb.append(" ");
					i++;
				}
				search = sb.toString().trim();
				sb.setLength(0);
			} else if (args[i].equalsIgnoreCase("-name")) {
				i++;
				while (args.length > i && !args[i].equalsIgnoreCase("-search")) {
					sb.append(args[i]);
					sb.append(" ");
					i++;
				}
				name = sb.toString().trim();
				sb.setLength(0);

			}
		}

		if (search == null || name == null) {
			throw new Exception("Invalid arguments");
		} else if (search.startsWith("<@") && search.endsWith(">")) {
			search = null;
		}
		return new Search(search, name);
	}

	class Search {
		private String search;
		private String name;

		public Search(String search, String name) {
			this.search = search;
			this.name = name;
		}

		public String getSearch() {
			return search;
		}

		public String getName() {
			return name;
		}
	}
}


