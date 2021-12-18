package com.soa.rs.discordbot.v3.commands;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.DiscordUtils;
import com.soa.rs.discordbot.v3.util.FileDownloader;
import com.soa.rs.discordbot.v3.util.SoaLogging;
import com.soa.rs.discordbot.v3.util.music.EmptyAudioTrack;
import com.soa.rs.discordbot.v3.util.music.GuildMusicManager;

import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.VoiceChannelJoinSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Command(triggers = { ".music" })
public class MusicPlayer extends AbstractCommand {

	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;
	private boolean disableRankCheck;
	private static final String YTSEARCH = "ytsearch:";

	public MusicPlayer() {
		//Initialization things for Lavaplayer only.  Most other will be in the initialize method.
		this.playerManager = new DefaultAudioPlayerManager();
		this.musicManagers = new ConcurrentHashMap<>();
		AudioSourceManagers.registerRemoteSources(playerManager);
	}

	@Override
	public void initialize() {
		disableRankCheck = true;

		if (DiscordCfgFactory.getConfig().getMusicPlayer() != null && DiscordCfgFactory.getConfig().getMusicPlayer()
				.isEnabled()) {
			setEnabled(true);
			setMustHavePermission(DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles().getRole());
			if (DiscordCfgFactory.getConfig().getMusicPlayer().isEnforceOnlyAllowedRoles() != null) {
				disableRankCheck = !DiscordCfgFactory.getConfig().getMusicPlayer().isEnforceOnlyAllowedRoles();
			}
			addHelpMsg(".music", "Use .music help for music commands.");
		} else
			setEnabled(false);
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		String[] args = Optional.of(event.getMessage().getContent()).orElse("").trim().split(" ");
		if (args.length <= 1) {
			return handleHelp(event);
		}

		if (!event.getMember().isPresent()) {
			return Mono.empty();
		}

		switch (args[1].toLowerCase()) {
		case "join":
			return handleJoin(event);
		case "leave":
			return handleLeave(event);
		case "play":
			return handlePlay(event, args);
		case "stop":
			return handleStop(event);
		case "pause":
			return handlePause(event);
		case "resume":
			return handleResume(event);
		case "volume":
			return handleVolume(event, args);
		case "skip":
			return handleSkip(event, args);
		case "nowplaying":
		case "playing":
			return handleNowPlaying(event);
		case "playlist":
			return handleListQueue(event);
		case "help":
			return handleHelp(event);
		case "disablerankcheck":
			return handleChangeRankCheck(event, args);
		default:
			return Mono.empty();
		}
	}

	private Mono<Void> handleJoin(MessageCreateEvent event) {
		return checkMusicRoles(event).switchIfEmpty(Mono.fromRunnable(() -> {
			SoaLogging.getLogger(this)
					.info("User attempted to run the join command but the user did not have permission to.");
			sendMissingRoleMessage(event.getMessage()).subscribe();
		})).flatMap(ignored -> Mono.justOrEmpty(event.getMember())).flatMap(Member::getVoiceState)
				.flatMap(VoiceState::getChannel)
				// join returns a VoiceConnection which would be required if we were
				// adding disconnection features, but for now we are just ignoring it.
				.flatMap(channel -> event.getGuild().map(this::getGuildAudioPlayer).flatMap(guildMusicManager -> {
					guildMusicManager.setVoiceChannel(channel);
					return channel.join(VoiceChannelJoinSpec.builder().provider(guildMusicManager.provider).build());
				})).flatMap(connection -> {
					SoaLogging.getLogger(this).debug("Joined voice channel");
					GuildMusicManager guildMusicManager = musicManagers
							.get(event.getGuildId().orElse(Snowflake.of(1L)).asLong());
					guildMusicManager.setVoiceConnection(connection);
					guildMusicManager.player.setVolume(15);
					final VoiceChannel channel = guildMusicManager.getVoiceChannel();

					// The bot itself has a VoiceState; 1 VoiceState signals bot is alone
					final Publisher<Boolean> voiceStateCounter = channel.getVoiceStates().count()
							.map(count -> 1L == count);

					// After 10 seconds, check if the bot is alone. This is useful if
					// the bot joined alone, but no one else joined since connecting
					final Mono<Void> onDelay = Mono.delay(Duration.ofSeconds(10L))
							.filterWhen(ignored -> voiceStateCounter).switchIfEmpty(Mono.never()).then();

					// As people join and leave `channel`, check if the bot is alone.
					// Note the first filter is not strictly necessary, but it does prevent many unnecessary cache calls
					final Mono<Void> onEvent = channel.getClient().getEventDispatcher().on(VoiceStateUpdateEvent.class)
							.filter(event1 -> event1.getOld().flatMap(VoiceState::getChannelId)
									.map(channel.getId()::equals).orElse(false))
							.filterWhen(ignored -> voiceStateCounter).next().then();

					// Disconnect the bot if either onDelay or onEvent are completed!
					return Mono.firstWithSignal(onDelay, onEvent).then(connection.disconnect()).then(Mono.fromRunnable(
							() -> handleDisconnection(event.getGuildId().orElse(Snowflake.of(1L)).asLong())));
				}).then();
	}

	private void handleDisconnection(long guildId) {
		GuildMusicManager manager = musicManagers.get(guildId);
		manager.player.stopTrack();
		manager.scheduler.emptyQueue();
		if (manager.getVoiceChannel() != null)
			SoaLogging.getLogger(this).debug("Automatically left voice channel for guild [" + guildId + "] as channel was empty");
		manager.setVoiceChannel(null);
	}

	private Mono<Void> handleLeave(MessageCreateEvent event) {
		return checkMusicRoles(event).switchIfEmpty(Mono.fromRunnable(() -> {
			SoaLogging.getLogger(this)
					.info("User attempted to run the leave command but the user did not have permission to.");
			sendMissingRoleMessage(event.getMessage()).subscribe();
		})).flatMap(ignored -> event.getGuild()).map(this::getGuildAudioPlayer).flatMap(guildMusicManager -> {
			if (guildMusicManager.getVoiceConnection() != null) {
				guildMusicManager.player.stopTrack();
				guildMusicManager.scheduler.emptyQueue();
				guildMusicManager.setVoiceChannel(null);
				SoaLogging.getLogger(this).debug("Left voice channel");
				return guildMusicManager.getVoiceConnection().disconnect();
			} else {
				SoaLogging.getLogger(this).debug("Voice connection is null?");
				return Mono.empty();
			}
		}).then();
	}

	private Mono<Void> handlePlay(MessageCreateEvent event, String[] args) {
		if (!event.getMessage().getAttachments().isEmpty()) {
			return checkMusicRoles(event).switchIfEmpty(Mono.fromRunnable(() -> {
				SoaLogging.getLogger(this)
						.info("User attempted to add a track to the queue but the user did not have permission to.");
				sendMissingRoleMessage(event.getMessage()).subscribe();
			})).flatMap(ignored -> event.getGuild()).map(this::getGuildAudioPlayer)
					.flatMap(guildMusicManager -> Mono.fromRunnable(() -> {
						for (Attachment attachment : event.getMessage().getAttachments()) {
							playerManager.loadItemOrdered(guildMusicManager, attachment.getUrl(),
									new DefaultAudioLoadResultHandler(guildMusicManager, event.getMessage(), null));
						}
					})).then();
		} else if (args.length > 2) {
			return checkMusicRoles(event).switchIfEmpty(Mono.fromRunnable(() -> {
				SoaLogging.getLogger(this)
						.info("User attempted to add a track to the queue but the user did not have permission to.");
				sendMissingRoleMessage(event.getMessage()).subscribe();
			})).flatMap(ignored -> event.getGuild()).map(this::getGuildAudioPlayer)
					.flatMap(guildMusicManager -> Mono.fromRunnable(() -> {
						String argument = args[2];
						if (!isValidUrl(args[2])) {
							argument = getYoutubeSearchString(args);
						}
						playerManager.loadItemOrdered(guildMusicManager, argument,
								new DefaultAudioLoadResultHandler(guildMusicManager, event.getMessage(), argument));
					})).then();

		} else if (event.getMessage().getAttachments().isEmpty()) {
			SoaLogging.getLogger(this).info("Attempted to run play with invalid number of args");
			return sendMessageToChannelReactively(event.getMessage(), "Invalid args provided - use `.music play <url>`")
					.then();
		} else {
			SoaLogging.getLogger(this)
					.info("User attempted to add a track to the queue but the user did not have permission to.");
			return sendMissingRoleMessage(event.getMessage()).then();
		}
	}

	/**
	 * Format of Youtube search string is "ytsearch: Title of track here"
	 *
	 * @param args Arguments that will be searched
	 * @return Properly formatted YT search string
	 */
	private String getYoutubeSearchString(String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append(YTSEARCH);
		for (int i = 2; i < args.length; i++) {
			sb.append(" ");
			sb.append(args[i]);
		}
		return sb.toString();
	}

	private Mono<Void> handleStop(MessageCreateEvent event) {
		return checkMusicRoles(event).switchIfEmpty(Mono.fromRunnable(() -> {
			SoaLogging.getLogger(this)
					.info("User attempted to run the stop command but the user did not have permission to.");
			sendMissingRoleMessage(event.getMessage()).subscribe();
		})).flatMap(ignored -> event.getGuild()).map(this::getGuildAudioPlayer)
				.flatMap(guildMusicManager -> Mono.fromRunnable(() -> {
					guildMusicManager.player.stopTrack();
					guildMusicManager.scheduler.emptyQueue();
				})).then();
	}

	private Mono<Void> handlePause(MessageCreateEvent event) {
		return checkMusicRoles(event).switchIfEmpty(Mono.fromRunnable(() -> {
			SoaLogging.getLogger(this)
					.info("User attempted to run the pause command but the user did not have permission to.");
			sendMissingRoleMessage(event.getMessage()).subscribe();
		})).flatMap(ignored -> event.getGuild()).map(this::getGuildAudioPlayer)
				.flatMap(guildMusicManager -> Mono.fromRunnable(() -> {
					if (!guildMusicManager.player.isPaused()) {
						guildMusicManager.player.setPaused(true);
					}
				})).then(sendMessageToChannelReactively(event.getMessage(), "Playback paused")).then();
	}

	private Mono<Void> handleResume(MessageCreateEvent event) {
		return checkMusicRoles(event).switchIfEmpty(Mono.fromRunnable(() -> {
			SoaLogging.getLogger(this)
					.info("User attempted to run the resume command but the user did not have permission to.");
			sendMissingRoleMessage(event.getMessage()).subscribe();
		})).flatMap(ignored -> event.getGuild()).map(this::getGuildAudioPlayer)
				.flatMap(guildMusicManager -> Mono.fromRunnable(() -> {
					if (guildMusicManager.player.isPaused()) {
						guildMusicManager.player.setPaused(false);
					}
				})).then(sendMessageToChannelReactively(event.getMessage(), "Playback resumed")).then();
	}

	private Mono<Void> handleVolume(MessageCreateEvent event, String[] args) {
		if (args.length == 3) {
			try {
				final int volume = Integer.parseInt(args[2]);
				return checkMusicRoles(event).switchIfEmpty(Mono.fromRunnable(() -> {
					SoaLogging.getLogger(this).info("User attempted to change the player volume command but the user did not have permission to.");
					sendMissingRoleMessage(event.getMessage()).subscribe();
				})).flatMap(ignored -> event.getGuild()).map(this::getGuildAudioPlayer).flatMap(
						guildMusicManager -> Mono.fromRunnable(() -> guildMusicManager.player.setVolume(volume)))
						.flatMap(ignored -> sendMessageToChannelReactively(event.getMessage(),
								"Volume set to " + volume)).then();
			}
			//If someone for some odd reason gives something that isn't a number
			catch (NumberFormatException e) {
				return sendMessageToChannelReactively(event.getMessage(),
						"Please provide a valid volume - a number 1-150.").then();
			}
		} else if (args.length == 2) {
			return event.getGuild().map(this::getGuildAudioPlayer).flatMap(
					guildMusicManager -> sendMessageToChannelReactively(event.getMessage(),
							"Current volume is " + guildMusicManager.player.getVolume())).then();
		}
		return Mono.empty();
	}

	private Mono<Void> handleSkip(MessageCreateEvent event, String[] args) {
		int skipVal = 1;
		if (args.length == 3) {
			try {
				skipVal = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				//Keep at 1, send message.
				sendMessageToChannel(event.getMessage(),
						"`" + args[2] + "` is not a number of tracks to skip, defaulting to 1.");
			}
		}
		final int finalSkipVal = skipVal;

		if (skipVal < 1) {
			/*
			 * While it may seem repetitive to take data from one input stream and then transfer
			 * it to another input stream, this is necessary because otherwise the try/with will
			 * close the inputStream before reactor has a chance to grab the data.  A
			 * ByteArrayInputStream being closed does nothing,so seems safe for this use case.
			 */
			try (InputStream is = FileDownloader.downloadFile("https://i.imgur.com/Tf2DfkS.jpg");
					ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(is))) {
				return event.getMessage().getChannel().flatMap(messageChannel -> DiscordUtils
						.sendMessage("... You just tried to skip 0 or less tracks...", bais, "ytho.jpg",
								messageChannel)).onErrorResume(ignore -> event.getMessage().getChannel().flatMap(
						messageChannel -> messageChannel
								.createMessage("... You just tried to skip 0 or less tracks... "))).then();
			} catch (Exception e) {
				//Error downloading the file, just send the message
				return sendMessageToChannelReactively(event.getMessage(),
						"... You just tried to skip 0 or less tracks... ").then();
			}

		} else {
			return checkMusicRoles(event).switchIfEmpty(Mono.fromRunnable(() -> {
				SoaLogging.getLogger(this)
						.info("User attempted to skip a track but the user did not have permission to.");
				sendMissingRoleMessage(event.getMessage()).subscribe();
			})).flatMap(ignored -> event.getGuild()).map(this::getGuildAudioPlayer)
					.flatMap(guildMusicManager -> Mono.fromCallable(() -> {
						String message = finalSkipVal + " track(s) skipped.";
						try {
							guildMusicManager.scheduler.skipNumTracks(finalSkipVal);
						} catch (Exception e) {
							SoaLogging.getLogger(this).error("Attempted to skip tracks but hit an error", e);
							guildMusicManager.scheduler.nextTrack();
							return "Attempted to skip tracks but encountered an error - will try and skip 1 track instead.  You may need to manually skip again if bot is unable to skip.";
						}
						return message;
					})).flatMap(message -> sendMessageToChannelReactively(event.getMessage(), message)).then();
		}
	}

	private Mono<Void> handleNowPlaying(MessageCreateEvent event) {
		return event.getGuild().map(this::getGuildAudioPlayer)
				.flatMap(guildMusicManager -> Mono.justOrEmpty(guildMusicManager.scheduler.getCurrentTrack()))
				.switchIfEmpty(Mono.just(new EmptyAudioTrack())).flatMap(
						audioTrack -> sendMessageToChannelReactively(event.getMessage(),
								"Now playing: " + audioTrack.getInfo().title)).then();
	}

	private Mono<Void> handleListQueue(MessageCreateEvent event) {
		return event.getGuild().map(this::getGuildAudioPlayer)
				.flatMap(guildMusicManager -> Mono.fromCallable(() -> getAudioTrackList(guildMusicManager)))
				.flatMapMany(Flux::fromIterable).flatMapSequential(
						messageCreateSpecConsumer -> event.getMessage().getChannel()
								.flatMap(messageChannel -> messageChannel.createMessage(messageCreateSpecConsumer)))
				.then();
	}

	private List<String> getAudioTrackList(GuildMusicManager guildMusicManager) {
		BlockingQueue<AudioTrack> queue = guildMusicManager.scheduler.getQueue();
		AudioTrack track;

		Iterator<AudioTrack> iter = queue.iterator();

		StringBuilder sb = new StringBuilder();
		sb.append("Currently within the Music Queue:\n\n");
		int i = 1;

		List<String> createSpecs = new ArrayList<>();

		if (!iter.hasNext()) {
			sb.append("Queue is empty.");
			createSpecs.add(sb.toString());

		} else {
			while (iter.hasNext()) {
				track = iter.next();
				sb.append(i + ": " + track.getInfo().title + "\n");
				i++;

				if (sb.length() > 1800) {
					createSpecs.add(sb.toString());
					sb.setLength(0);
				}
			}
			createSpecs.add(sb.toString());
		}
		SoaLogging.getLogger(this).trace("Size of createSpecs upon return: " + createSpecs.size());
		return createSpecs;
	}

	private Mono<Void> handleHelp(MessageCreateEvent event) {
		StringBuilder sb = new StringBuilder();
		sb.append("```Help: Music (command: .music [args])\n");
		if (!disableRankCheck) {

			if (DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles() != null) {
				sb.append(
						"Note - This menu and these commands will only work for users assigned one of the following roles: ")
						.append(DiscordUtils.translateRoleList(
								DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles().getRole()))
						.append("\n\n");

			}

		}
		sb.append(".music join - Bot joins the voice channel you are in.\n");
		sb.append(".music play <url> - Bot queues up the URL provided.\n");
		sb.append(
				".music play <search term> - Bot searches Youtube for the given search term and plays the first result.\n");
		sb.append(
				".music play <attachment> - Bot will play uploaded file; comment with attachment must be the play command.  Discord enforces an 8mb max file size.\n");
		sb.append(".music pause - Bot pauses playback.\n");
		sb.append(".music resume - Bot resumes playback.\n");
		sb.append(
				".music skip [number]- Bot skips the currently playing song.  If an (optional) number is provided, the bot will skip that many songs in a row.\n");
		sb.append(".music stop - Bot stops playing and empties playlist.\n");
		sb.append(".music playlist - Bot lists currently queued playlist.\n");
		sb.append(".music nowplaying - Bot lists currently playing track.\n");
		sb.append(".music playing - Bot lists currently playing track.\n");
		sb.append(".music volume <0-100> - Sets volume to appropriate level.\n");
		sb.append(".music leave - Bot leaves the voice channel.  Bot will also leave automatically if no one is left in the voice channel.\n");
		sb.append(".music help - Bot displays this menu.```");

		return sendMessageToChannelReactively(event.getMessage(), sb.toString()).then();
	}

	private Mono<Void> handleChangeRankCheck(MessageCreateEvent event, String[] args) {
		if (event.getMember().isPresent() && args.length == 3) {
			return event.getMember().get().getRoles()
					.filter(role -> DiscordCfgFactory.getConfig().getMusicPlayer().getCanDisableRankCheck().getRole()
							.contains(role.getName())).next().flatMap(role -> Mono.fromRunnable(() -> {
						disableRankCheck = Boolean.parseBoolean(args[2]);
						SoaLogging.getLogger(this)
								.debug("Music player disableRankCheck is now set to " + disableRankCheck);
					})).then(sendMessageToChannelReactively(event.getMessage(),
							"Settings updated, disableRankCheck set to " + disableRankCheck)).then();
		} else {
			SoaLogging.getLogger(this)
					.error("Error encountered when updating disableRankCheck property - not sent in ");
			return Mono.empty();
		}
	}

	private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
		long guildId = guild.getId().asLong();
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager, guildId);
			musicManagers.put(guildId, musicManager);
		}

		return musicManager;
	}

	private Mono<Role> checkMusicRoles(MessageCreateEvent event) {
		boolean hasPermission = false;
		if (disableRankCheck) {
			hasPermission = true;
		}

		if (!hasPermission && DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles() == null) {
			hasPermission = true;
		}

		if (hasPermission)
			//Emit Everyone role, in case for some reason someone doesn't have a role
			return event.getGuild().flatMap(Guild::getEveryoneRole);
		else {
			return permittedToExecuteEvent(event.getMember().get());
		}
	}

	class DefaultAudioLoadResultHandler implements AudioLoadResultHandler {
		private final GuildMusicManager musicManager;
		private final Message message;
		private final String musicArg;

		DefaultAudioLoadResultHandler(GuildMusicManager manager, Message message, String musicArg) {
			this.musicManager = manager;
			this.message = message;
			if (musicArg != null) {
				this.musicArg = musicArg;
			} else {
				this.musicArg = "Uploaded Track";
			}
		}

		@Override
		public void trackLoaded(AudioTrack track) {
			sendMessageToChannel(message, "Adding to queue " + track.getInfo().title);
			SoaLogging.getLogger(this)
					.debug("Track added to queue: " + track.getInfo().title + ", " + musicManager.scheduler.getQueue()
							.size() + " tracks in queue.");
			musicManager.scheduler.queue(track);
		}

		@Override
		public void playlistLoaded(AudioPlaylist playlist) {
			AudioTrack firstTrack = playlist.getSelectedTrack();

			if (firstTrack == null) {
				firstTrack = playlist.getTracks().get(0);
			}

			//Searches seem to grab whatever it finds and add ALL of them.  Just add the first one
			if (musicArg.startsWith(YTSEARCH)) {
				SoaLogging.getLogger(this).debug("Attempting to add track " + firstTrack.getInfo().title
						+ ", which came from search term " + musicArg + ", to queue.");
				sendMessageToChannel(message, "Adding to queue " + firstTrack.getInfo().title);
				musicManager.scheduler.queue(firstTrack);

				return;
			}

			SoaLogging.getLogger(this)
					.debug("Attempting to add playlist with " + playlist.getTracks().size() + " tracks to queue.");
			sendMessageToChannel(message,
					"Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName()
							+ ")");

			musicManager.scheduler.queue(firstTrack);

			for (int i = 0; i < playlist.getTracks().size(); i++) {
				AudioTrack nextTrack = playlist.getTracks().get(i);

				//Per playlist.getSelectedTrack, the selectedTrack is ALSO in the playlist all tracks list.  Don't re-insert it.
				if (!nextTrack.equals(firstTrack)) {
					musicManager.scheduler.queue(nextTrack);
					SoaLogging.getLogger(this).trace("Adding song from Playlist: " + nextTrack.getInfo().title);
				} else {
					SoaLogging.getLogger(this).trace("This is the same track (" + nextTrack.getInfo().title
							+ ") as the first one, so not putting it in again.");
				}
			}
			SoaLogging.getLogger(this)
					.debug("Tracks added to queue, " + +musicManager.scheduler.getQueue().size() + " tracks in queue.");
		}

		@Override
		public void noMatches() {
			SoaLogging.getLogger(this).debug("Attempted to add track with argument " + musicArg + " but nothing found");
			sendMessageToChannel(message, "Nothing found by " + musicArg);
		}

		@Override
		public void loadFailed(FriendlyException exception) {
			SoaLogging.getLogger(this).error("Unable to play track: " + exception.getMessage(), exception);
			sendMessageToChannel(message, "Could not play: " + exception.getMessage());
		}
	}

	private void sendMessageToChannel(Message message, String content) {
		message.getChannel().flatMap(messageChannel -> DiscordUtils.sendMessage(content, messageChannel)).subscribe();
	}

	private Mono<Message> sendMessageToChannelReactively(Message message, String content) {
		return message.getChannel().flatMap(messageChannel -> DiscordUtils.sendMessage(content, messageChannel));
	}

	private Mono<Message> sendMissingRoleMessage(Message message) {
		return message.getChannel().flatMap(messageChannel -> DiscordUtils.sendMessage(
				"Sorry, only the following roles can run the music player: " + DiscordUtils
						.translateRoleList(DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles().getRole()),
				messageChannel));
	}

	private boolean isValidUrl(String url) {
		try {
			new URL(url).toURI();
		} catch (Exception e) {
			return false;
		}
		return url.toLowerCase().startsWith("https://www.youtube") || url.toLowerCase().startsWith("https://youtube")
				|| url.toLowerCase().startsWith("https://youtu.be");
	}
}
