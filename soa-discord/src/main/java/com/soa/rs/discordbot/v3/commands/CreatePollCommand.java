package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

@Command(triggers = { ".poll" })
public class CreatePollCommand extends AbstractCommand {

	private final String thumbsUp = "👍";
	private final String thumbsDown = "👎";
//	private int len = ".poll".length();

	@Override
	public void initialize() {
		setEnabled(true);
		addHelpMsg(".poll", "Creates a poll for a question asked, by adding " + thumbsUp + " and " + thumbsDown
				+ " reactions to it.");
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		/*String content = event.getMessage().getContent().orElse("").trim();
		String question = content.substring(len).trim();
		if (question.length() == 0) {
			return Mono.empty();
		}

		return event.getMessage().getChannel().flatMap(channel -> DiscordUtils.sendMessage(question, channel))
				.flatMap(message -> {
					message.addReaction(ReactionEmoji.unicode(thumbsUp));
					message.addReaction(ReactionEmoji.unicode(thumbsDown));
					return Mono.empty();
				}).then(event.getMessage().delete("Poll has been created with this message.")).then();*/
		return event.getMessage().addReaction(ReactionEmoji.unicode(thumbsUp))
				.then(event.getMessage().addReaction(ReactionEmoji.unicode(thumbsDown))).then();
	}
}
