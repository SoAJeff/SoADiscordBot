package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

@Command(triggers = { ".ban" })
public class UserBanEvent extends AbstractCommand {

	@Override
	public void initialize() {
		if (DiscordCfgFactory.getConfig().getAdminEvent() != null && DiscordCfgFactory.getConfig().getAdminEvent()
				.isEnabled()) {
			setEnabled(true);
			setMustHavePermission(DiscordCfgFactory.getConfig().getAdminEvent().getAllowedRoles().getRole());
			addHelpMsg(".ban", "Admin can tag user(s) and they will be banned from the server.");
		} else {
			setEnabled(false);
		}
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		SoaLogging.getLogger(this).info("Attempting to ban user from server");
		if (event.getMessage().getContent().get().contains("-id")) {
			String[] args = event.getMessage().getContent().get().trim().split(" ");
			return permittedToExecuteEvent(event.getMember().get())
					.flatMapMany(ignored -> event.getGuild())
					.flatMap(guildld -> guildld.ban(Snowflake.of(args[args.length - 1]),
							banQuerySpec -> banQuerySpec.setReason("Ban issued via admin ban request")
									.setDeleteMessageDays(7))
							.onErrorResume(err -> {
						SoaLogging.getLogger(this).error("Failed to ban user: " + err.getMessage());
						return Mono.empty();
					})).then(event.getMessage().delete()).then();
		} else {
			return permittedToExecuteEvent(event.getMember().get())
					.flatMapMany(ignored -> event.getMessage().getUserMentions())
					.flatMap(user -> user.asMember(event.getGuildId().get()))
					.flatMap(member1 -> member1.ban(banQuerySpec -> banQuerySpec.setReason("Ban issued via admin ban request")
									.setDeleteMessageDays(7))
							.onErrorResume(err -> {
								SoaLogging.getLogger(this).error("Failed to ban user: " + err.getMessage());
								return Mono.empty();
							})).then(event.getMessage().delete()).then();
		}
	}
}
