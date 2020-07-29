package com.soa.rs.discordbot.v3.util.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;

public class GuildMusicManager {
	/**
	 * Audio player for the guild.
	 */
	public final AudioPlayer player;
	/**
	 * Track scheduler for the player.
	 */
	public final TrackScheduler scheduler;

	public final D4jAudioProvider provider;

	private volatile VoiceConnection voiceConnection;

	private VoiceChannel voiceChannel;


	/**
	 * Creates a player and a track scheduler.
	 * @param manager Audio player manager to use for creating the player.
	 */
	public GuildMusicManager(AudioPlayerManager manager, long guildId) {
		player = manager.createPlayer();
		scheduler = new TrackScheduler(player, guildId);
		player.addListener(scheduler);
		provider = new D4jAudioProvider(player);
	}

	public VoiceConnection getVoiceConnection() {
		return voiceConnection;
	}

	public void setVoiceConnection(VoiceConnection voiceConnection) {
		this.voiceConnection = voiceConnection;
	}

	public VoiceChannel getVoiceChannel() {
		return voiceChannel;
	}

	public void setVoiceChannel(VoiceChannel voiceChannel) {
		this.voiceChannel = voiceChannel;
	}
}
