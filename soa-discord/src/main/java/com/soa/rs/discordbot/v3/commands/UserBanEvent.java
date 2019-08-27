package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

@Command(triggers = { ".ban" })
public class UserBanEvent extends AbstractCommand {

	@Override
	public void initialize() {
		if (DiscordCfgFactory.getConfig().getAdminEvent() != null && DiscordCfgFactory.getConfig().getAdminEvent()
				.isEnabled()) {
			setEnabled(true);
			setMustHavePermission(DiscordCfgFactory.getConfig().getAdminEvent().getAllowedRoles().getRole());
		} else {
			setEnabled(false);
		}
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		Member member = event.getMember().get();
		SoaLogging.getLogger(this).info("Attempting to ban user from server");
		return permittedToExecuteEvent(event.getMember().get())
				.flatMapMany(ignored -> event.getMessage().getUserMentions())
				.flatMap(user -> user.asMember(event.getGuildId().get()))
				.flatMap(member1 -> member1
						.ban(banQuerySpec -> banQuerySpec.setReason("Ban issued via admin ban request")
								.setDeleteMessageDays(7)).onErrorResume(err -> {
							SoaLogging.getLogger(this).error("Failed to ban user: " + err.getMessage());
							return Mono.empty();
						})).then(event.getMessage().delete()).then();
	}
}
