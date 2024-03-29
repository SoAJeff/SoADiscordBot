package com.soa.rs.discordbot.v3.util;

import java.io.InputStream;
import java.util.List;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class DiscordUtils {

	public static Mono<Boolean> hasPermission(Channel channel, Mono<Snowflake> userId, Permission permission) {
		// An user has all the permissions in a private channel
		if (channel instanceof PrivateChannel) {
			return Mono.just(true);
		}
		return userId.flatMap(snowflake -> GuildChannel.class.cast(channel).getEffectivePermissions(snowflake))
				.map(permissions -> permissions.contains(permission));
	}

	public static Mono<Message> sendMessage(String content, MessageChannel channel) {
		return sendMessage(content, null, channel);
	}

	public static Mono<Message> sendMessage(EmbedCreateSpec embed, MessageChannel channel) {
		return sendMessage(null, embed, channel);
	}

	public static Mono<Message> sendMessage(String content, EmbedCreateSpec embed, MessageChannel channel) {
		final Mono<Snowflake> selfId = Mono.just(channel.getClient().getSelfId());
		return Mono.zip(DiscordUtils.hasPermission(channel, selfId, Permission.SEND_MESSAGES),
				DiscordUtils.hasPermission(channel, selfId, Permission.EMBED_LINKS)).flatMap(tuple -> {
			final boolean canSendMessage = tuple.getT1();
			final boolean canSendEmbed = tuple.getT2();

			if (!canSendMessage) {
				SoaLogging.getLogger(DiscordUtils.class)
						.error("{Channel ID: %d} Missing permission: SEND_MESSAGES", channel.getId().asLong());
				return Mono.empty();
			}

			if (!canSendEmbed && embed != null) {
				SoaLogging.getLogger(DiscordUtils.class)
						.error("{Channel ID: %d} Missing permission: EMBED_LINKS", channel.getId().asLong());
				return Mono.empty();
			}

			MessageCreateSpec spec = MessageCreateSpec.create();
			if (content != null) {
				spec = spec.withContent(content);
			}
			if (embed != null) {
				spec = spec.withEmbeds(embed);
			}
			return channel.createMessage(spec);
		});
	}

	public static Mono<Message> sendMessage(String content, InputStream file, String filename, MessageChannel channel) {
		final Mono<Snowflake> selfId = Mono.just(channel.getClient().getSelfId());
		return Mono.zip(DiscordUtils.hasPermission(channel, selfId, Permission.SEND_MESSAGES),
				DiscordUtils.hasPermission(channel, selfId, Permission.ATTACH_FILES)).flatMap(tuple -> {
			final boolean canSendMessage = tuple.getT1();
			final boolean canAttachFiles = tuple.getT2();

			if (!canSendMessage) {
				SoaLogging.getLogger(DiscordUtils.class)
						.error("{Channel ID: %d} Missing permission: SEND_MESSAGES", channel.getId().asLong());
				return Mono.empty();
			}

			if (!canAttachFiles && file != null) {
				SoaLogging.getLogger(DiscordUtils.class)
						.error("{Channel ID: %d} Missing permission: ATTACH_FILE", channel.getId().asLong());
				return Mono.empty();
			}

			MessageCreateSpec spec = MessageCreateSpec.create();
			if (content != null) {
				spec = spec.withContent(content);
			}
			if (file != null) {
				spec = spec.withFiles(MessageCreateFields.File.of(filename, file));
			}
			return channel.createMessage(spec);
		});
	}

	public static String translateRoleList(List<String> roles) {
		return String.join(", ", roles);
	}

}
