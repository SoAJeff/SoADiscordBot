package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.util.DiscordUtils;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

@Command(triggers = { "!setrsn", ".setrsn" })
public class SetRsnCommand extends AbstractCommand {

	private GuildUserUtility userUtility;
	private final String thumbsUp = "👍";
	private final String caution = "⚠";
	private int len = "!setrsn".length();
	private boolean success;

	@Override
	public void initialize() {
		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
			userUtility = new GuildUserUtility();
			setEnabled(true);
		}
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		String content = event.getMessage().getContent().orElse("").trim();
		String name = content.substring(len).trim();
		success = false;
		event.getMember().ifPresent(member -> {
			SoaLogging.getLogger(this)
					.info("Assigning RSN: " + name + " for user " + member.getUsername() + "#" + member
							.getDiscriminator());
			userUtility.updateKnownNameForUser(name, member.getId().asLong(), member.getGuildId().asLong());
			success = true;
		});
		if (success) {
			return event.getMessage().addReaction(ReactionEmoji.unicode(thumbsUp)).then();
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

	void setUserUtility(GuildUserUtility utility) {
		this.userUtility = utility;
	}
}
