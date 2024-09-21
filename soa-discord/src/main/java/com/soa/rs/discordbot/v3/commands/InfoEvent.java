package com.soa.rs.discordbot.v3.commands;

import java.time.Duration;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.DiscordUtils;
import com.soa.rs.discordbot.v3.util.SoaDiscordBotConstants;
import com.soa.rs.discordbot.v3.util.SoaLogging;
import com.soa.rs.discordbot.v3.util.UptimeUtility;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Command(triggers = { ".info" })
@Interaction(trigger = "info")
public class InfoEvent extends AbstractCommand {

	private final String version = this.getClass().getPackage().getImplementationVersion();
	private static final int MB_UNIT = 1024 * 1024;

	@Override
	public void initialize() {
		addHelpMsg(".info", "Provides information regarding the bot.");
		setEnabled(true);
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return event.getMessage().getChannel().flatMap(messageChannel -> DiscordUtils.sendMessage(generateInfo(), messageChannel)).then();
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		SoaLogging.getLogger(this).info("Made it to the chat event!");
		return event.reply().withEphemeral(true).withContent(generateInfo()).then();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ButtonInteractionEvent event) {
		return Mono.empty();
	}

	private String generateInfo()
	{
		Duration uptime = UptimeUtility.getUptime();
		StringBuilder sb = new StringBuilder();
		sb.append("Hi there!  I'm the " + DiscordCfgFactory.getConfig().getGuildAbbreviation() + " bot!\n");
		sb.append("My Version: " + version);
		sb.append("\n");
		sb.append("My Uptime: " + uptime.toDays() + " days, " + (uptime.toHours() % 24) + " hours, " + (uptime.toMinutes()
				% 60) + " minutes");
		sb.append("\n");

		sb.append("Info on me can be found on the forums: <" + SoaDiscordBotConstants.FORUMTHREAD_URL + ">");
		sb.append("\n");
		sb.append("The source for me can be found on GitHub: <" + SoaDiscordBotConstants.GITHUB_URL + ">");
		return sb.toString();
	}

}
