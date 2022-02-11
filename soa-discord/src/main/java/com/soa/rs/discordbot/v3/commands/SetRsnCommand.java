package com.soa.rs.discordbot.v3.commands;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.util.DiscordUtils;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.GuildMemberEditSpec;
import reactor.core.publisher.Mono;

@Command(triggers = { "!setrsn", ".setrsn" })
public class SetRsnCommand extends AbstractCommand {

	private GuildUserUtility userUtility;
	private final String thumbsUp = "👍";
	private final String caution = "⚠";
	private final int len = "!setrsn".length();
	private boolean success;
	private final Pattern rsnPattern;

	public SetRsnCommand() {
		rsnPattern = Pattern.compile("^[A-Za-z0-9]+([ _-]{0,10}[A-Za-z0-9]+)*$");
	}

	@Override
	public void initialize() {
		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
			userUtility = new GuildUserUtility();
			setEnabled(true);
			addHelpMsg(".setrsn",
					"Set's a user's RSN for search purposes and also updates the user's nickname on the server. "
							+ "(use !setrsn to also affect RuneInfo, ask Staff if you also want nickname to include an emoji)");
		}
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		String content = Optional.of(event.getMessage().getContent()).orElse("").trim();
		String name = content.substring(len).trim();
		if (!isValidRsn(name)) {
			StringBuilder sb = new StringBuilder();
			event.getMessage().getAuthor().ifPresent(user -> sb.append(user.getMention()));
			sb.append(", ");
			sb.append(" the name entered does not appear to be a valid RSN.  ");
			sb.append("If you are using this command to attempt to set your server nickname to ");
			sb.append("something that isn't your RSN (e.g., to add an emoji or also add your IRL name),");
			sb.append(" then contact a member of staff for assistance.");
			return event.getMessage().getChannel().flatMap(channel -> DiscordUtils.sendMessage(sb.toString(), channel))
					.then();
		}
		SoaLogging.getLogger(this).debug("RuneScape name was determined to be valid, continuing...");
		success = false;
		event.getMember().ifPresent(member -> {
			SoaLogging.getLogger(this)
					.info("Assigning RSN: " + name + " for user " + member.getUsername() + "#" + member
							.getDiscriminator());
			userUtility.updateKnownNameForUser(name, member.getId().asLong(), member.getGuildId().asLong());
			userUtility.updateDisplayNameForUser(name, member.getId().asLong(), member.getGuildId().asLong());
			success = true;
		});
		if (success) {
			return event.getMessage().addReaction(ReactionEmoji.unicode(thumbsUp)).onErrorResume(throwable -> Mono
					.fromRunnable(() -> SoaLogging.getLogger(this)
							.error("Failed to react to the message - message deleted?")))
					.then(event.getMember().get().edit(GuildMemberEditSpec.builder().nicknameOrNull(name).build())
							.onErrorResume(throwable -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
									.error("Failed to change server nickname of user, don't have permission?",
											throwable)))).then();
		} else {
			if (event.getMessage().getAuthor().isPresent()) {
				SoaLogging.getLogger(this)
						.error("Unable to assign RSN for user " + event.getMessage().getAuthor().get().getUsername()
								+ "#" + event.getMessage().getAuthor().get().getDiscriminator());
				return event.getMessage().getChannel().flatMap(channel -> DiscordUtils
						.sendMessage("Unable to assign RSN - this command should not be sent via PM.", channel))
						.then(event.getMessage().addReaction(ReactionEmoji.unicode(caution))).then();
			} else {
				//No user, no member, something is wrong
				SoaLogging.getLogger(this).error("Received !setrsn but no user or member - invalid state, return");
				return Mono.empty();
			}
		}
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		return Mono.empty();
	}

	boolean isValidRsn(String name) {
		boolean isValid = false;
		if (name.length() <= 12) {
			Matcher matcher = rsnPattern.matcher(name);
			if (matcher.matches()) {
				isValid = true;
			} else {
				SoaLogging.getLogger(this).error("RuneScape name does not match RSN regex.");
			}
		} else {
			SoaLogging.getLogger(this).error("RuneScape name provided is longer than 12 chars");
		}
		return isValid;
	}

	void setUserUtility(GuildUserUtility utility) {
		this.userUtility = utility;
	}
}
