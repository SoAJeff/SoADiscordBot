package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

@Interaction(trigger="adminnews")
public class AdminNewsInteraction extends AbstractCommand {

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
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		String channel = event.getOption("channel").flatMap(ApplicationCommandInteractionOption::getValue).map(
				ApplicationCommandInteractionOptionValue::asString).get();
		String message = event.getOption("message").flatMap(ApplicationCommandInteractionOption::getValue).map(
				ApplicationCommandInteractionOptionValue::asString).get();

		return event.deferReply().withEphemeral(true).then(permittedToExecuteEvent(event.getInteraction().getMember().orElse(null))
						.switchIfEmpty(Mono.error(new Throwable("You do not have the correct permissions to execute this command.")))
				.flatMapMany(ignored -> event.getClient().getGuilds()).flatMap(Guild::getChannels)
				.filter(guildChannel -> guildChannel.getName().equalsIgnoreCase(channel))
				.filter(guildChannel -> guildChannel.getType().equals(Channel.Type.GUILD_TEXT))
				.map(guildChannel -> (MessageChannel) guildChannel)
				.flatMap(messageChannel -> messageChannel.createMessage(message))
						.then(event.createFollowup("Message sent").withEphemeral(true)).then())
				.onErrorResume(throwable -> event.createFollowup(throwable.getMessage()).withEphemeral(true).then())
				.then();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ButtonInteractionEvent event) {
		return Mono.empty();
	}
}
