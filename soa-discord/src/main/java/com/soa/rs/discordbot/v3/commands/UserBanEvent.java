package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.BanQuerySpec;
import reactor.core.publisher.Mono;

@Command(triggers = { ".ban" })
public class UserBanEvent extends AbstractCommand {

	@Override
	public void initialize() {
		if (DiscordCfgFactory.getConfig().getAdminEvent() != null && DiscordCfgFactory.getConfig().getAdminEvent()
				.isEnabled()) {
			setEnabled(true);
			setMustHavePermission(DiscordCfgFactory.getConfig().getAdminEvent().getAllowedRoles().getRole());
			addHelpMsg(".ban",
					"Admin can tag user(s) and they will be banned from the server.  Or provide args -id and -history to ban by ID, and remove 7 days history.");
		} else {
			setEnabled(false);
		}
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		SoaLogging.getLogger(this).info("Attempting to ban user from server");
		if (event.getMessage().getContent().contains("-id") && event.getMessage().getContent().contains("-history")) {
			String[] args = event.getMessage().getContent().trim().split(" ");
			return permittedToExecuteEvent(event.getMember().get())
					.flatMapMany(ignored -> event.getGuild())
					.flatMap(guildld -> guildld.ban(Snowflake.of(args[args.length - 1]),
							BanQuerySpec.builder().reason("Ban issued via admin ban request").deleteMessageDays(7).build())
							.onErrorResume(err -> {
						SoaLogging.getLogger(this).error("Failed to ban user: " + err.getMessage());
						return Mono.empty();
					})).then(event.getMessage().delete()).then();
		} else if (event.getMessage().getContent().contains("-id")) {
			String[] args = event.getMessage().getContent().trim().split(" ");
			return permittedToExecuteEvent(event.getMember().get()).flatMapMany(ignored -> event.getGuild()).flatMap(
					guildld -> guildld.ban(Snowflake.of(args[args.length - 1]),
							BanQuerySpec.builder().reason("Ban issued via admin ban request").build())
							.onErrorResume(err -> {
						SoaLogging.getLogger(this).error("Failed to ban user: " + err.getMessage());
						return Mono.empty();
					})).then(event.getMessage().delete()).then();
		}
		else {
			return permittedToExecuteEvent(event.getMember().get())
					.flatMapIterable(ignored -> event.getMessage().getUserMentions())
					.flatMap(user -> user.asMember(event.getGuildId().get()))
					.flatMap(member1 -> member1.ban(BanQuerySpec.builder().reason("Ban issued via admin ban request").deleteMessageDays(7).build())
							.onErrorResume(err -> {
								SoaLogging.getLogger(this).error("Failed to ban user: " + err.getMessage());
								return Mono.empty();
							})).then(event.getMessage().delete()).then();
		}
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		return Mono.empty();
	}
}
