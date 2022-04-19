package com.soa.rs.discordbot.v3.commands;

import java.io.InputStream;

import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.FileDownloader;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.apache.commons.io.IOUtils;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.UserEditSpec;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

@Interaction(trigger="adminchangeavatar")
public class AdminChangeAvatarInteraction extends AbstractCommand {

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
		String avatarUrl = event.getOption("avatarurl").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asString).get();

		return event.deferReply().withEphemeral(true).then(updateAvatar(avatarUrl, event))
				.then(event.createFollowup("Avatar Updated").withEphemeral(true).then())
				.onErrorResume(throwable -> event.createFollowup(throwable.getMessage()).withEphemeral(true).then())
				.then();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}

	private Mono<Void> updateAvatar(String avatarUrl, ChatInputInteractionEvent event)
	{
		try (InputStream is = FileDownloader.downloadFile(avatarUrl)) {
			Image avatar = getImage(is, avatarUrl);
			return permittedToExecuteEvent(event.getInteraction().getMember().orElse(null))
					.switchIfEmpty(Mono.error(new Throwable("You do not have the correct permissions to execute this command.")))
					.flatMap(ignored -> event.getClient().edit(UserEditSpec.builder().avatar(avatar).build())).then();
		} catch (Exception e) {
			SoaLogging.getLogger(this).error("Error downloading the external image", e);
			return Mono.empty();
		}
	}

	private Image getImage(InputStream is, String arg) throws Exception
	{
		Image.Format format = FileDownloader.getFormatForProvidedURL(arg);
		return Image.ofRaw(IOUtils.toByteArray(is), format);
	}
}
