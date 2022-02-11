package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

@Command(triggers = { ".poll" })
public class CreatePollCommand extends AbstractCommand {

	private final String thumbsUp = "👍";
	private final String thumbsDown = "👎";

	@Override
	public void initialize() {
		setEnabled(true);
		addHelpMsg(".poll", "Creates a poll for a question asked, by adding " + thumbsUp + " and " + thumbsDown
				+ " reactions to it.");
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return event.getMessage().addReaction(ReactionEmoji.unicode(thumbsUp))
				.then(event.getMessage().addReaction(ReactionEmoji.unicode(thumbsDown))).then();
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		return Mono.empty();
	}
}
