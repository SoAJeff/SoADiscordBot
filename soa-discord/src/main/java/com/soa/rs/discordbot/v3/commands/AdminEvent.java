package com.soa.rs.discordbot.v3.commands;

import java.io.InputStream;
import java.util.Optional;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.DiscordUtils;
import com.soa.rs.discordbot.v3.util.FileDownloader;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.apache.commons.io.IOUtils;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.UserEditSpec;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

@Command(triggers = { ".admin" })
public class AdminEvent extends AbstractCommand {

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
		String[] args = Optional.of(event.getMessage().getContent()).orElse("").trim().split(" ");

		if (args.length > 2) {
			String action = args[1];
			switch (action) {
			case "news":
				return handleNewsEvent(event, args);
			case "change-name":
				return handleChangeNameEvent(event, args);
			case "change-avatar":
				return changeAvatarEvent(event, args);
			case "change-presence-text":
				return changePresenceText(event, args);
			default:
				return Mono.empty();
			}

		} else {
			return Mono.empty();
		}

	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}

	private Mono<Void> handleNewsEvent(MessageCreateEvent event, String[] args) {
		if (args.length > 3 && event.getMember().isPresent()) {
			String channelName = args[2];
			StringBuilder sb = new StringBuilder();
			int i;
			for (i = 3; i < args.length; i++) {
				sb.append(args[i] + " ");
			}

			return permittedToExecuteEvent(event.getMember().get())
					.flatMapMany(ignored -> event.getClient().getGuilds()).flatMap(Guild::getChannels)
					.filter(guildChannel -> guildChannel.getName().equalsIgnoreCase(channelName))
					.filter(guildChannel -> guildChannel.getType().equals(Channel.Type.GUILD_TEXT))
					.map(guildChannel -> (MessageChannel) guildChannel)
					.flatMap(messageChannel -> DiscordUtils.sendMessage(sb.toString(), messageChannel)).then();
		} else
			return Mono.empty();
	}

	private Mono<Void> handleChangeNameEvent(MessageCreateEvent event, String[] args) {
		if (args.length > 2 && event.getMember().isPresent() && event.getGuildId().isPresent()) {
			StringBuilder sb = new StringBuilder();
			for (int i = 2; i < args.length; i++) {
				sb.append(args[i] + " ");
			}

			SoaLogging.getLogger(this)
					.info("Changing bot nickname in guild " + event.getGuildId().get() + " to " + sb.toString());
			return permittedToExecuteEvent(event.getMember().get()).flatMap(ignored -> event.getGuild())
					.flatMap(guild -> guild.changeSelfNickname(sb.toString()))
					.doOnError(err -> SoaLogging.getLogger(this).error("Unable to change nickname")).then();
		} else
			return Mono.empty();
	}

	private Mono<Void> changeAvatarEvent(MessageCreateEvent event, String[] args) {
		if (args.length == 3 && event.getMember().isPresent()) {
			try (InputStream is = FileDownloader.downloadFile(args[2])) {
				Image avatar = getImage(is, args[2]);
				return permittedToExecuteEvent(event.getMember().get())
						.flatMap(ignored -> event.getClient().edit(UserEditSpec.builder().avatar(avatar).build())).then();
			} catch (Exception e) {
				SoaLogging.getLogger(this).error("Error downloading the external image", e);
				return Mono.empty();
			}
		} else
			return Mono.empty();
	}

	private Mono<Void> changePresenceText(MessageCreateEvent event, String[] args) {
		if (args.length >= 2 && event.getMember().isPresent()) {
			StringBuilder sb = new StringBuilder();
			for (int i = 2; i < args.length; i++) {
				sb.append(args[i] + " ");
			}
			DiscordCfgFactory.getConfig().setDefaultStatus(sb.toString());
			return permittedToExecuteEvent(event.getMember().get()).flatMap(
					ignored -> event.getClient().updatePresence(ClientPresence.online(ClientActivity.playing(sb.toString()))))
					.then();
		} else
			return Mono.empty();
	}

	private Image getImage(InputStream is, String arg) throws Exception
	{
		Image.Format format = FileDownloader.getFormatForProvidedURL(arg);
		return Image.ofRaw(IOUtils.toByteArray(is), format);
	}

}
