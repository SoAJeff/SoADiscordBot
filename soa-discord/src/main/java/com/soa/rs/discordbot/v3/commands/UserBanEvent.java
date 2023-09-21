package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.BanQuerySpec;
import reactor.core.publisher.Mono;

@Command(triggers = { ".ban" })
@Interaction(trigger = "userban")
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
		if (!event.getInteraction().getGuildId().isPresent()) {
			return event.reply("This can only be executed from a guild.").withEphemeral(true).then();
		}
		Mono<Member> user = event.getOption("user").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asUser).orElse(Mono.empty())
				.flatMap(u -> u.asMember(event.getInteraction().getGuildId().get()));
		boolean userIsPresent = event.getOption("user").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asUser).isPresent();

		String id = event.getOption("id").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asString).orElse(null);

		String banReason = event.getOption("reason").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asString).orElse("Ban issued via admin ban request");

		boolean deleteHistory = event.getOption("deletehistory").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asBoolean).orElse(false);

		if (!userIsPresent && id == null) {
			return event.reply("Cannot execute command without a tagged user or an ID of a user").withEphemeral(true)
					.then();
		}

		BanQuerySpec spec = BanQuerySpec.create();
		spec = spec.withReason(banReason);

		if (deleteHistory) {
			spec.withDeleteMessageSeconds(60*60*24*7);
		}

		BanQuerySpec finalSpec = spec;
		if (userIsPresent) {
			return event.deferReply().withEphemeral(true)
					.then(permittedToExecuteEvent(event.getInteraction().getMember().orElse(null))
							.switchIfEmpty(Mono.error(new Throwable("You do not have the correct permissions to execute this command.")))
							.flatMap(ignored -> user.flatMap(m -> m.ban(finalSpec)))
							.then(event.createFollowup("User banned").withEphemeral(true))
							.onErrorResume(throwable -> {
								SoaLogging.getLogger(this)
										.error("Unable to ban user: " + throwable.getMessage(), throwable);
								return event.createFollowup("Failed to ban user - you may be missing permissions!")
										.withEphemeral(true).then(Mono.empty());
							}).then())
					.onErrorResume(throwable -> event.createFollowup(throwable.getMessage()).withEphemeral(true).then())
					.then();
		} else {
			return event.deferReply().withEphemeral(true)
					.then(permittedToExecuteEvent(event.getInteraction().getMember().orElse(null))
							.switchIfEmpty(Mono.error(new Throwable("You do not have the correct permissions to execute this command.")))
							.flatMap(ignored -> event.getInteraction().getGuild())
							.flatMap(guild -> guild.ban(Snowflake.of(id), finalSpec))
							.then(event.createFollowup("User banned").withEphemeral(true))
							.onErrorResume(throwable -> {
								SoaLogging.getLogger(this).error("Unable to ban user: " + throwable.getMessage(), throwable);
								return event.createFollowup("Failed to ban user - you may be missing permissions!").withEphemeral(true).then(Mono.empty());
							}).then()).
							onErrorResume(throwable -> event.createFollowup(throwable.getMessage()).withEphemeral(true).then())
					.then();
		}
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}
}
