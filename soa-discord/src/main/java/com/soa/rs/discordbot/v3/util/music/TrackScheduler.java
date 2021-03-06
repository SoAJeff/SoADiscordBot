package com.soa.rs.discordbot.v3.util.music;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.soa.rs.discordbot.v3.util.SoaLogging;

/**
 * This class schedules tracks for the audio player. It contains the queue of
 * tracks.
 * 
 * This code is taken from the sample code by lavaplayer; see
 * https://github.com/sedmelluq/lavaplayer for more information
 */
public class TrackScheduler extends AudioEventAdapter {
	private final AudioPlayer player;
	private final BlockingQueue<AudioTrack> queue;
	private final long guildId;

	private Optional<AudioTrack> currentTrack;

	/**
	 * @param player
	 *            The audio player this scheduler uses
	 */
	public TrackScheduler(AudioPlayer player, long guildId) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
		this.guildId = guildId;
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track
	 *            The track to play or add to queue.
	 */
	public void queue(AudioTrack track) {
		// Calling startTrack with the noInterrupt set to true will start the
		// track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case
		// the player was already playing so this
		// track goes to the queue instead.
		if (!player.startTrack(track.makeClone(), true)) {
			queue.offer(track);
		} else {
			currentTrack = Optional.ofNullable(track);
		}
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing
		// or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply
		// stop the player.
		if (queue.peek() != null) {
			currentTrack = Optional.of(queue.poll().makeClone());
			player.startTrack(currentTrack.orElse(null), false);
		} else {
			currentTrack = Optional.empty();
			player.startTrack(null, false);
			SoaLogging.getLogger(this)
					.debug("Track queue is now empty, stopping playback for guild [" + guildId + "].");
		}
	}

	public void skipNumTracks(int num) {
		for (int i = 0; i < num; i++) {
			queue.poll();
		}
		nextTrack();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// Log if moving to next track because this one failed to load
		if (endReason == AudioTrackEndReason.LOAD_FAILED) {
			SoaLogging.getLogger(this)
					.warn("Attempted to start track [" + track.getInfo().title + "] for guild [" + guildId
							+ "] but it failed to load.");
		}
		// Only start the next track if the end reason is suitable for it
		// (FINISHED or LOAD_FAILED)
		if (endReason.mayStartNext) {
			nextTrack();
		}
	}

	/**
	 * Returns the queue of AudioTracks
	 * 
	 * @return The queue of tracks
	 */
	public BlockingQueue<AudioTrack> getQueue() {
		return queue;
	}

	/**
	 * Empties the queue of tracks to be played
	 */
	public void emptyQueue() {
		queue.clear();
		currentTrack = Optional.empty();
	}

	/**
	 * Gets the current track
	 * 
	 * @return The current audio track
	 */
	public Optional<AudioTrack> getCurrentTrack() {
		return currentTrack;
	}
}
