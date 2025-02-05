package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Command(triggers = { ".help" })
@Interaction(trigger = "help")
public class HelpEvent extends AbstractCommand {

	@Override
	public void initialize() {
		setEnabled(true);
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return event.getMessage().getChannel()
				.flatMap(messageChannel -> DiscordUtils.sendMessage(buildHelpString(), messageChannel)).then();
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		return event.reply().withEphemeral(true).withContent(buildHelpString()).then();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ButtonInteractionEvent event) {
		return Mono.empty();
	}

	String buildHelpString() {
		StringBuilder sb = new StringBuilder();
		sb.append("```Help: ");
		sb.append(DiscordCfgFactory.getConfig().getGuildAbbreviation());
		sb.append(" Commands\n");
		for (String key : DiscordCfgFactory.getInstance().getHelpMap().keySet()) {
			sb.append(key);
			sb.append(" - ");
			sb.append(DiscordCfgFactory.getInstance().getHelpMap().get(key));
			sb.append("\n");
		}
		sb.append("```");

		return sb.toString();
	}
}
