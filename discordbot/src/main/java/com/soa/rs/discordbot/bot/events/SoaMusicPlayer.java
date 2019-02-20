package com.soa.rs.discordbot.bot.events;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.soa.rs.discordbot.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.util.GuildMusicManager;
import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.util.SoaLogging;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * The SoaMusicPlayer is a slightly modified version of the implementation
 * example of the lavaplayer library which can be found on Github:
 * https://github.com/sedmelluq/lavaplayer
 * <p>
 * NOTE: The commands in this class are only permitted to be run by:
 * <ul>
 * <li>Eldars</li>
 * <li>Lians</li>
 * <li>Arquendi</li>
 * <li>Anyone temporarily assigned the role of "DJ"</li>
 * </ul>
 */
public class SoaMusicPlayer {

	private IMessage msg;
	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;
	private boolean disableRankCheck = true;

	public SoaMusicPlayer(IMessage msg) {
		this.playerManager = new DefaultAudioPlayerManager();
		this.musicManagers = new HashMap<>();

		// For use of playing YT, Soundcloud, etc
		AudioSourceManagers.registerRemoteSources(playerManager);

		this.msg = msg;
	}

	/**
	 * Sets the message parameter for the music player event
	 *
	 * @param msg IMessage object for the received message
	 */
	public void setMsg(IMessage msg) {
		this.msg = msg;
	}

	/**
	 * Get the GuildMusicManager for the provided guild. If one does not exist, one
	 * will be created.
	 *
	 * @param guild The guild for which a music manager will be retrieved
	 * @return The music manager for the guild
	 */
	private synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild) {
		long guildId = guild.getLongID();
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager);
			musicManagers.put(guildId, musicManager);
		}

		guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

		return musicManager;
	}

	/**
	 * Checks to determine if the person who has issued a music command is permitted
	 * to run the MusicPlayer
	 *
	 * @param event MessageReceivedEvent
	 * @return true if user is allowed to issue these commands, false otherwise
	 */
	private boolean checkMusicRoles(MessageReceivedEvent event) {
		IGuild guild = event.getMessage().getGuild();
		if (guild == null) {
			return false;
		}
		if (disableRankCheck) {
			return true;
		}

		/*
		 * If no roles are specified in the config, assume that all are allowed to
		 * activate the player
		 */
		if (DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles() == null) {
			return true;
		}

		/*
		 * Check if the user has one of the roles listed within the config
		 */
		if (SoaClientHelper.isRank(event.getMessage(),
				DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles().getRole())) {
			return true;
		}

		/*
		 * User does not meet the critera, reject
		 */
		return false;

	}

	/**
	 * Handles the arguments sent to the MusicPlayer.
	 *
	 * @param event MessageReceivedEvent
	 * @param args  The additional arguments other than ".music" sent to the bot.
	 */
	public void handleMusicArgs(MessageReceivedEvent event, String[] args) {

		StringBuilder sb = new StringBuilder();
		if (!checkMusicRoles(event) && !args[1].equalsIgnoreCase("playlist") && !args[1]
				.equalsIgnoreCase("nowplaying")) {
			sb.append(event.getMessage().getAuthor().getName());
			sb.append(" attempted to run a music command but did not have the appropriate rank.");
			SoaLogging.getLogger().info(sb.toString());
			sendMessageToChannel(msg.getChannel(),
					"Sorry, only the following roles can run the music player: " + SoaClientHelper.translateRoleList(
							DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles().getRole()));
			return;
		}

		if (args.length <= 1) {
			handleHelp();
			return;
		}

		sb.append(event.getMessage().getAuthor().getName());
		sb.append(" executed music command: ");
		sb.append(args[1]);
		String message = args[1];

		if (message.equals("join")) {
			SoaLogging.getLogger().info(sb.toString());
			handleJoinChannel();
		}

		if (message.equals("leave")) {
			SoaLogging.getLogger().info(sb.toString());
			handleLeaveChannel();
		}

		if (message.equals("help")) {
			SoaLogging.getLogger().info(sb.toString());
			handleHelp();
		}

		if (message.equals("play")) {
			if (args.length > 2)
				sb.append(" " + args[2]);
			else
				sb.append(" uploaded track");
			SoaLogging.getLogger().info(sb.toString());
			handlePlay(args, event.getMessage().getChannel());
		}

		if (message.equals("playlist")) {
			SoaLogging.getLogger().info(sb.toString());
			handleListQueue(event.getMessage().getChannel());
		}

		if (message.equals("nowplaying")) {
			SoaLogging.getLogger().info(sb.toString());
			handleNowPlaying(event.getMessage().getChannel());
		}

		if (message.equals("stop")) {
			SoaLogging.getLogger().info(sb.toString());
			handleStop(event.getMessage().getChannel());
		}

		if (message.equals("pause")) {
			SoaLogging.getLogger().info(sb.toString());
			handlePause(event.getMessage().getChannel());
		}

		if (message.equals("resume")) {
			SoaLogging.getLogger().info(sb.toString());
			handleResume(event.getMessage().getChannel());
		}

		if (message.equals("skip")) {
			int skipVal = 1;
			if (args.length == 3) {
				skipVal = Integer.parseInt(args[2]);
			}
			SoaLogging.getLogger().info(sb.toString());
			handleSkip(event.getMessage().getChannel(), skipVal);
		}

		if (message.equals("volume")) {
			if (args.length == 3) {
				sb.append(" " + args[2]);
			}
			SoaLogging.getLogger().info(sb.toString());
			handleVolume(event.getMessage().getChannel(), args);
		}
		if (message.equalsIgnoreCase("disableRankCheck")) {

			if (SoaClientHelper
					.isRank(msg, DiscordCfgFactory.getConfig().getMusicPlayer().getCanDisableRankCheck().getRole())) {
				if (args[2].equals("true")) {
					disableRankCheck = true;
				} else if (args[2].equals("false")) {
					disableRankCheck = false;
				}
				SoaClientHelper.sendMsgToChannel(msg.getChannel(),
						"Settings updated, disableRankCheck set to " + disableRankCheck);
			}

		}
	}

	/**
	 * Handle playing a music track
	 *
	 * @param args    The arguments containing the track to play
	 * @param channel The text channel the command was entered in
	 */
	private void handlePlay(String[] args, IChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

		if (!this.msg.getAttachments().isEmpty() && this.msg.getAttachments().get(0).getUrl() != null) {
			playerManager.loadItemOrdered(musicManager, this.msg.getAttachments().get(0).getUrl(),
					new DefaultAudioLoadResultHandler(musicManager, channel, null));
		} else if (args.length > 2) {
			playerManager.loadItemOrdered(musicManager, args[2],
					new DefaultAudioLoadResultHandler(musicManager, channel, args[2]));
		}

	}

	/**
	 * List out all entries in the current music queue
	 *
	 * @param channel The channel the message will be entered into
	 */
	private void handleListQueue(IChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

		BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();
		AudioTrack track;

		Iterator<AudioTrack> iter = queue.iterator();

		StringBuilder sb = new StringBuilder();
		sb.append("Currently within the Music Queue:\n\n");
		int i = 1;

		if (!iter.hasNext()) {
			sb.append("Queue is empty.");
		} else {
			while (iter.hasNext()) {
				track = iter.next();
				sb.append(i + ": " + track.getInfo().title + "\n");
				i++;

				if (sb.length() > 1800) {
					sendMessageToChannel(channel, sb.toString());
					sb = new StringBuilder();
				}
			}
		}
		sendMessageToChannel(channel, sb.toString());

	}

	private void handleNowPlaying(IChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		AudioTrack track = musicManager.scheduler.getCurrentTrack();
		if (track != null) {
			sendMessageToChannel(channel, "Now playing: " + track.getInfo().title);
		} else {
			sendMessageToChannel(channel, "Nothing is currently playing.");
		}
	}

	/**
	 * Send a message to the specified channel
	 *
	 * @param channel The channel the message will be entered into
	 * @param message The message to be entered
	 */
	private void sendMessageToChannel(IChannel channel, String message) {
		SoaClientHelper.sendMsgToChannel(channel, message);
	}

	/**
	 * Stop a music track from playing
	 *
	 * @param channel The channel the command was entered in
	 */
	private void handleStop(IChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		musicManager.player.stopTrack();
		musicManager.scheduler.emptyQueue();
	}

	/**
	 * Pause a currently playing music track
	 *
	 * @param channel The channel the command was entered in
	 */
	private void handlePause(IChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		if (!musicManager.player.isPaused()) {
			musicManager.player.setPaused(true);
			sendMessageToChannel(channel, "Playback paused");
		}
	}

	/**
	 * Resume a currently paused music track
	 *
	 * @param channel The channel the command was entered in
	 */
	private void handleResume(IChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		if (musicManager.player.isPaused()) {
			musicManager.player.setPaused(false);
			sendMessageToChannel(channel, "Playback resumed");
		}
	}

	/**
	 * Change the volume of the player
	 *
	 * @param channel The text channel the command was entered in
	 * @param args    The arguments containing volume to set the music at
	 */
	private void handleVolume(IChannel channel, String[] args) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

		if (args.length == 3) {
			int volume = Integer.parseInt(args[2]);
			musicManager.player.setVolume(volume);
			sendMessageToChannel(channel, "Volume set to " + volume);
		} else if (args.length == 2) {
			sendMessageToChannel(channel, "Current volume is " + musicManager.player.getVolume());
		}

	}

	/**
	 * Skip a currently playing music track
	 *
	 * @param channel   The text channel the command was entered in
	 * @param numToSkip The number of tracks to skip.
	 */
	private void handleSkip(IChannel channel, int numToSkip) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		if (numToSkip < 0) {
			SoaClientHelper.sendMsgWithFileToChannel(channel, "... You just tried to skip 0 or less tracks... ",
					"https://i.imgur.com/Tf2DfkS.jpg", "ytho.jpg");
		} else {
			for (int i = 0; i < numToSkip; i++)
				musicManager.scheduler.nextTrack();
			sendMessageToChannel(channel, numToSkip + " track(s) skipped");
		}
	}

	/**
	 * Handles the help command
	 */
	private void handleHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("```Help: Music (command: .music [args])\n");
		if (!disableRankCheck) {

			if (DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles() != null) {
				sb.append(
						"Note - This menu and these commands will only work for users assigned one of the following roles: "
								+ SoaClientHelper.translateRoleList(
								DiscordCfgFactory.getConfig().getMusicPlayer().getAllowedRoles().getRole()) + "\n\n");

			}

		}
		sb.append(".music join - Bot joins the voice channel you are in.\n");
		sb.append(".music play <url> - Bot queues up the URL provided.\n");
		sb.append(
				".music play <attachment> - Bot will play uploaded file; comment with attachment must be the play command.  Discord enforces an 8mb max file size.\n");
		sb.append(".music pause - Bot pauses playback.\n");
		sb.append(".music resume - Bot resumes playback.\n");
		sb.append(".music skip [number]- Bot skips the currently playing song.  If an (optional) number is provided, the bot will skip that many songs in a row.\n");
		sb.append(".music stop - Bot stops playing and empties playlist.\n");
		sb.append(".music playlist - Bot lists currently queued playlist.\n");
		sb.append(".music nowplaying - Bot lists currently playing track.\n");
		sb.append(".music volume <0-100> - Sets volume to appropriate level.\n");
		sb.append(".music leave - Bot leaves the voice channel.\n");
		sb.append(".music help - Bot displays this menu.```");

		sendMessageToChannel(msg.getChannel(), sb.toString());
	}

	/**
	 * Handles joining a Discord Audio channel. The channel that the caller is in is
	 * the one that will be joined.
	 */
	private void handleJoinChannel() {
		try {
			IVoiceChannel voicechannel = msg.getAuthor().getVoiceStateForGuild(msg.getGuild()).getChannel();
			voicechannel.join();

			// Purposely set the volume to be low, so that no one's ears are
			// blasted out if a track is loud
			GuildMusicManager musicManager = getGuildAudioPlayer(msg.getGuild());
			musicManager.player.setVolume(5);
		} catch (ArrayIndexOutOfBoundsException e) {
			sendMessageToChannel(msg.getChannel(), msg.getAuthor().getName() + ", you aren't in a channel!");
		} catch (MissingPermissionsException e) {
			sendMessageToChannel(msg.getChannel(),
					msg.getAuthor().getName() + ", I don't have permission to join that channel.  Sorry!");
		}
	}

	/**
	 * Handles leaving the currently joined Discord channel
	 */
	private void handleLeaveChannel() {
		handleStop(msg.getChannel());
		IDiscordClient client = msg.getClient();
		IVoiceChannel chan = client.getConnectedVoiceChannels().stream().filter(c -> c.getGuild() == msg.getGuild())
				.findFirst().orElse(null);
		if (chan != null)
			chan.leave();

	}

	class DefaultAudioLoadResultHandler implements AudioLoadResultHandler {
		private GuildMusicManager musicManager;
		private IChannel channel;
		private String musicArg;

		public DefaultAudioLoadResultHandler(GuildMusicManager manager, IChannel channel, String musicArg) {
			this.musicManager = manager;
			this.channel = channel;
			if (musicArg != null) {
				this.musicArg = musicArg;
			} else {
				this.musicArg = "Uploaded Track";
			}
		}

		@Override
		public void trackLoaded(AudioTrack track) {
			sendMessageToChannel(channel, "Adding to queue " + track.getInfo().title);

			musicManager.scheduler.queue(track);
		}

		@Override
		public void playlistLoaded(AudioPlaylist playlist) {
			AudioTrack firstTrack = playlist.getSelectedTrack();

			if (firstTrack == null) {
				firstTrack = playlist.getTracks().get(0);
			}

			sendMessageToChannel(channel,
					"Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName()
							+ ")");

			musicManager.scheduler.queue(firstTrack);

			for (int i = 1; i < playlist.getTracks().size(); i++) {
				firstTrack = playlist.getTracks().get(i);

				musicManager.scheduler.queue(firstTrack);
				SoaLogging.getLogger().info("Adding song from Playlist: " + firstTrack.getInfo().title);
			}
		}

		@Override
		public void noMatches() {
			sendMessageToChannel(channel, "Nothing found by " + musicArg);
		}

		@Override
		public void loadFailed(FriendlyException exception) {
			sendMessageToChannel(channel, "Could not play: " + exception.getMessage());
		}
	}
}
