package com.soa.rs.discordbot.v3.bot;

import java.time.Duration;

import com.soa.rs.discordbot.v3.api.command.CommandInitializer;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.usertrack.LastActiveCache;
import com.soa.rs.discordbot.v3.usertrack.LastSeenCache;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.event.domain.UserUpdateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.core.event.domain.guild.BanEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ReconnectEvent;
import discord4j.core.event.domain.lifecycle.ResumeEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import reactor.core.publisher.Flux;

public class SoaDiscordBot {

	private DiscordClient client;
	private LastSeenCache lastSeenCache;
	private LastActiveCache lastActiveCache;

	public void start() {
		SoaLogging.getLogger(this)
				.info("Logging-in bot with Token: " + DiscordCfgFactory.getConfig().getDiscordToken());
		DiscordClientBuilder builder = new DiscordClientBuilder(DiscordCfgFactory.getConfig().getDiscordToken());

		if (DiscordCfgFactory.getConfig().getDefaultStatus() != null && !DiscordCfgFactory.getConfig()
				.getDefaultStatus().trim().isEmpty()) {
			SoaLogging.getLogger(this).info("Setting Discord Initial Presence");
			builder.setInitialPresence(
					Presence.online(Activity.playing(DiscordCfgFactory.getConfig().getDefaultStatus())));
		}

		client = builder.build();

		CommandInitializer.init();
		registerEvents();

		/* Retry backoff is in place for if login fails due to some kind of exception (no internet)?
		 * Note: Will not try indefinitely, will try only 20 times, but this is only with initial startup
		 * Once logged in, if disconnected will try indefinitely
		 */
		SoaLogging.getLogger(this).info("Attempting connection to Discord...");
		client.login().retryBackoff(20, Duration.ofSeconds(20), Duration.ofMinutes(1)).block();
	}

	public void disconnect() {
		//Try to write caches to DB before shutting down if possible
		if (lastSeenCache != null)
			lastSeenCache.writeCacheToDatabase();
		if (lastActiveCache != null)
			lastActiveCache.writeCacheToDatabase();
		client.logout().block();
	}

	private void registerEvents() {
		ReadyEventHandler readyEventHandler = new ReadyEventHandler(client);
		MessageCreateHandler messageCreateHandler = new MessageCreateHandler();
		GuildCreateHandler guildCreateHandler = new GuildCreateHandler();
		TypingStartHandler typingStartHandler = new TypingStartHandler();
		MemberJoinHandler memberJoinHandler = new MemberJoinHandler();
		MemberLeftHandler memberLeftHandler = new MemberLeftHandler();
		MemberUpdateHandler memberUpdateHandler = new MemberUpdateHandler();
		VoiceStateUpdateHandler voiceStateUpdateHandler = new VoiceStateUpdateHandler();
		ReactionAddEventHandler reactionAddEventHandler = new ReactionAddEventHandler();

		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
			lastSeenCache = new LastSeenCache();
			lastActiveCache = new LastActiveCache();
			GuildUserUtility guildUserUtility = new GuildUserUtility();
			lastSeenCache.setGuildUserUtility(guildUserUtility);
			lastActiveCache.setGuildUserUtility(guildUserUtility);
			messageCreateHandler.setLastSeenCache(lastSeenCache);
			messageCreateHandler.setLastActiveCache(lastActiveCache);
			guildCreateHandler.setLastSeenCache(lastSeenCache);
			guildCreateHandler.setLastActiveCache(lastActiveCache);
			typingStartHandler.setCache(lastSeenCache);
			memberUpdateHandler.setCache(lastSeenCache);
			voiceStateUpdateHandler.setLastSeenCache(lastSeenCache);
			voiceStateUpdateHandler.setLastActiveCache(lastActiveCache);
			reactionAddEventHandler.setLastSeenCache(lastSeenCache);
			reactionAddEventHandler.setLastActiveCache(lastActiveCache);

			Flux.interval(Duration.ofMinutes(1)).doOnNext(ignored -> {
				lastSeenCache.writeCacheToDatabase();
				lastActiveCache.writeCacheToDatabase();
			}).subscribe(null, err -> SoaLogging.getLogger(this)
					.error("Error when running write cache task:" + err.getMessage(), err));
		}

		client.getEventDispatcher().on(ReadyEvent.class) // Listen for ReadyEvent(s)
				.map(event -> event.getGuilds().size()) // Get how many guilds the bot is in
				.flatMap(
						size -> client.getEventDispatcher().on(GuildCreateEvent.class) // Listen for GuildCreateEvent(s)
								.take(size) // Take only the first `size` GuildCreateEvent(s) to be received
								.collectList()) // Take all received GuildCreateEvents and make it a List
				.subscribe(events -> readyEventHandler.handle());
		/* All guilds have been received, client is fully connected */

		client.getEventDispatcher().on(ReconnectEvent.class).subscribe(event -> {
			if (DiscordCfgFactory.getConfig().getDefaultStatus() != null && !DiscordCfgFactory.getConfig()
					.getDefaultStatus().trim().isEmpty())
				client.updatePresence(
						Presence.online(Activity.playing(DiscordCfgFactory.getConfig().getDefaultStatus())));
		});

		client.getEventDispatcher().on(ResumeEvent.class).subscribe(event -> {
			if (DiscordCfgFactory.getConfig().getDefaultStatus() != null && !DiscordCfgFactory.getConfig()
					.getDefaultStatus().trim().isEmpty())
				client.updatePresence(
						Presence.online(Activity.playing(DiscordCfgFactory.getConfig().getDefaultStatus())));
		});

		client.getEventDispatcher().on(MessageCreateEvent.class).flatMap(messageCreateHandler::handle).subscribe(null,
				err -> SoaLogging.getLogger(this).error("Unexpected error occurred during message create event.", err));

		client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(guildCreateHandler::handleGuildCreate,
				err -> SoaLogging.getLogger(this).error("Unexpected error occurred during guild create event.", err));

		client.getEventDispatcher().on(MemberJoinEvent.class).subscribe(memberJoinHandler::handle,
				err -> SoaLogging.getLogger(this).error("Unexpected error occurred during member join event.", err));

		client.getEventDispatcher().on(MemberLeaveEvent.class).subscribe(memberLeftHandler::handle,
				err -> SoaLogging.getLogger(this).error("Unexpected error occurred during member leave event.", err));

		client.getEventDispatcher().on(BanEvent.class).subscribe(memberLeftHandler::handle,
				err -> SoaLogging.getLogger(this).error("Unexpected error occurred during member leave event.", err));

		client.getEventDispatcher().on(MemberUpdateEvent.class).flatMap(memberUpdateHandler::handle).subscribe(null,
				err -> SoaLogging.getLogger(this).error("Unexpected error during member update event.", err));

		client.getEventDispatcher().on(UserUpdateEvent.class).subscribe(memberUpdateHandler::handle,
				err -> SoaLogging.getLogger(this).error("Unexpected error during user update event.", err));

		client.getEventDispatcher().on(PresenceUpdateEvent.class).flatMap(memberUpdateHandler::handle).subscribe(null,
				err -> SoaLogging.getLogger(this).error("Unexpected error during presence update event.", err));

		client.getEventDispatcher().on(VoiceStateUpdateEvent.class).flatMap(voiceStateUpdateHandler::handle)
				.subscribe(null, err -> SoaLogging.getLogger(this)
						.error("Unexpected error during voice state update event.", err));

		client.getEventDispatcher().on(ReactionAddEvent.class).flatMap(reactionAddEventHandler::handle).subscribe(null,
				err -> SoaLogging.getLogger(this).error("Unexpected error during reaction add event.", err));

		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
			client.getEventDispatcher().on(TypingStartEvent.class).flatMap(typingStartHandler::handleTypingStart)
					.subscribe(null, err -> SoaLogging.getLogger(this).error("Error processing typing event", err));
		}

	}
}
