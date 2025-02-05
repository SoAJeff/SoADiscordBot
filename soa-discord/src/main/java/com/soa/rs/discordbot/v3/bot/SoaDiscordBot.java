package com.soa.rs.discordbot.v3.bot;

import java.time.Duration;

import com.soa.rs.discordbot.v3.api.command.CommandInitializer;
import com.soa.rs.discordbot.v3.api.interaction.GlobalCommandRegistrar;
import com.soa.rs.discordbot.v3.api.interaction.GuildCommandRegistrar;
import com.soa.rs.discordbot.v3.api.interaction.InteractionInitializer;
import com.soa.rs.discordbot.v3.api.interaction.InteractionProcessor;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.usertrack.LastActiveCache;
import com.soa.rs.discordbot.v3.usertrack.LastSeenCache;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.event.domain.UserUpdateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.core.event.domain.guild.BanEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ReconnectEvent;
import discord4j.core.event.domain.lifecycle.ResumeEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class SoaDiscordBot {

	private LastSeenCache lastSeenCache;
	private LastActiveCache lastActiveCache;
	private GatewayDiscordClient discordClient;

	public void start() {
		if(DiscordCfgFactory.getConfig().isEnableTextCommands()) {
			CommandInitializer.init();
		}
		InteractionInitializer.init();
		SoaLogging.getLogger(this)
				.info("Logging-in bot with Token: " + DiscordCfgFactory.getConfig().getDiscordToken());
		DiscordClient client = DiscordClient.create(DiscordCfgFactory.getConfig().getDiscordToken());
		client.gateway().setInitialPresence(
				shardInfo -> ClientPresence.online(ClientActivity.playing(DiscordCfgFactory.getConfig().getDefaultStatus())))
				.setEnabledIntents(IntentSet
						.of(Intent.GUILDS, Intent.GUILD_MEMBERS, Intent.GUILD_MODERATION, Intent.GUILD_VOICE_STATES,
								Intent.GUILD_PRESENCES, Intent.GUILD_MESSAGES, Intent.GUILD_MESSAGE_REACTIONS,
								Intent.GUILD_MESSAGE_TYPING, Intent.DIRECT_MESSAGES))
				.withGateway(gatewayDiscordClient -> {
					this.discordClient = gatewayDiscordClient;
					registerEvents(gatewayDiscordClient);
					registerCommands(gatewayDiscordClient);
					return gatewayDiscordClient.onDisconnect();
				})
				/* Retry backoff is in place for if login fails due to some kind of exception (no internet)?
				 * Note: Will not try indefinitely, will try only 20 times, but this is only with initial startup
				 * Once logged in, if disconnected will try indefinitely
				 */
				.retryWhen(Retry.backoff(20, Duration.ofSeconds(5))).block();

	}

	public void disconnect() {
		this.discordClient.logout().block();
		//Try to write caches to DB before shutting down if possible
		if (lastSeenCache != null)
			lastSeenCache.writeCacheToDatabase();
		if (lastActiveCache != null)
			lastActiveCache.writeCacheToDatabase();
	}

	private void registerEvents(GatewayDiscordClient gatewayDiscordClient) {
		ReadyEventHandler readyEventHandler = new ReadyEventHandler(gatewayDiscordClient);
		MessageCreateHandler messageCreateHandler = new MessageCreateHandler();
		InteractionProcessor interactionProcessor = new InteractionProcessor();
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
			interactionProcessor.setLastSeenCache(lastSeenCache);
			interactionProcessor.setLastActiveCache(lastActiveCache);
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

		gatewayDiscordClient.on(ReadyEvent.class) // Listen for ReadyEvent(s)
				.map(event -> event.getGuilds().size()) // Get how many guilds the bot is in
				.flatMap(size -> gatewayDiscordClient.on(GuildCreateEvent.class) // Listen for GuildCreateEvent(s)
						.take(size) // Take only the first `size` GuildCreateEvent(s) to be received
						.collectList()) // Take all received GuildCreateEvents and make it a List
				.subscribe(events -> readyEventHandler.handle());
		/* All guilds have been received, client is fully connected */

		gatewayDiscordClient.on(ReconnectEvent.class).subscribe(event -> {
			if (DiscordCfgFactory.getConfig().getDefaultStatus() != null && !DiscordCfgFactory.getConfig()
					.getDefaultStatus().trim().isEmpty())
				gatewayDiscordClient.updatePresence(
						ClientPresence.online(ClientActivity.playing(DiscordCfgFactory.getConfig().getDefaultStatus())));
		});

		gatewayDiscordClient.on(ResumeEvent.class).subscribe(event -> {
			if (DiscordCfgFactory.getConfig().getDefaultStatus() != null && !DiscordCfgFactory.getConfig()
					.getDefaultStatus().trim().isEmpty())
				gatewayDiscordClient.updatePresence(
						ClientPresence.online(ClientActivity.playing(DiscordCfgFactory.getConfig().getDefaultStatus())));
		});

		gatewayDiscordClient.on(MessageCreateEvent.class).flatMap(event -> messageCreateHandler.handle(event)
				.onErrorResume(err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during message create event.", err)))).subscribe();

		gatewayDiscordClient.on(ChatInputInteractionEvent.class, interactionProcessor::processInteraction).subscribe();

		gatewayDiscordClient.on(ModalSubmitInteractionEvent.class, interactionProcessor::processModal).subscribe();

		gatewayDiscordClient.on(ButtonInteractionEvent.class, interactionProcessor::processButton).subscribe();

		gatewayDiscordClient.on(GuildCreateEvent.class).flatMap(guildCreateHandler::handleGuildCreate).onErrorResume(
				err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during guild create event.", err))).subscribe();

		gatewayDiscordClient.on(MemberJoinEvent.class).flatMap(memberJoinHandler::handle).onErrorResume(
				err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during member join event.", err))).subscribe();

		gatewayDiscordClient.on(MemberLeaveEvent.class).flatMap(memberLeftHandler::handle).onErrorResume(
				err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during member leave event.", err))).subscribe();

		gatewayDiscordClient.on(BanEvent.class).flatMap(memberLeftHandler::handle).onErrorResume(
				err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during member leave event.", err))).subscribe();

		gatewayDiscordClient.on(MemberUpdateEvent.class).flatMap(memberUpdateHandler::handle).onErrorResume(
				err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during member update event.", err))).subscribe();

		gatewayDiscordClient.on(UserUpdateEvent.class).flatMap(memberUpdateHandler::handle).onErrorResume(
				err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during user update event.", err))).subscribe();

		gatewayDiscordClient.on(PresenceUpdateEvent.class).flatMap(memberUpdateHandler::handle).onErrorResume(
				err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during presence update event.", err))).subscribe();

		gatewayDiscordClient.on(VoiceStateUpdateEvent.class).flatMap(voiceStateUpdateHandler::handle).onErrorResume(
				err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during voice state update event.", err))).subscribe();

		gatewayDiscordClient.on(ReactionAddEvent.class).flatMap(reactionAddEventHandler::handle).onErrorResume(
				err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
						.error("Unexpected error occurred during reaction add event.", err))).subscribe();

		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
			gatewayDiscordClient.on(TypingStartEvent.class).flatMap(typingStartHandler::handleTypingStart)
					.onErrorResume(err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
							.error("Unexpected error occurred during Typing Start event.", err))).subscribe();
		}

	}

	private void registerCommands(GatewayDiscordClient gatewayDiscordClient) {
		try {
			if (DiscordCfgFactory.getConfig().isUseGuildInteractions()) {
				new GuildCommandRegistrar(gatewayDiscordClient.getRestClient()).registerCommands(
						DiscordCfgFactory.getInstance().getCommandFileNames(),
						DiscordCfgFactory.getConfig().getDefaultGuildId());
			} else {
				new GlobalCommandRegistrar(gatewayDiscordClient.getRestClient()).registerCommands(
						DiscordCfgFactory.getInstance().getCommandFileNames());
			}
		} catch (Exception e) {
			SoaLogging.getLogger(this).error("Failed to register commands: " + e.getMessage(), e);
		}

	}

}
