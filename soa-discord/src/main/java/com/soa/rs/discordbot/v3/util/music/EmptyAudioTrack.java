package com.soa.rs.discordbot.v3.util.music;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;

public class EmptyAudioTrack implements AudioTrack {
	@Override
	public AudioTrackInfo getInfo() {
		return new AudioTrackInfo("Nothing currently playing.", null, 0, null, false, null);
	}

	@Override
	public String getIdentifier() {
		return null;
	}

	@Override
	public AudioTrackState getState() {
		return null;
	}

	@Override
	public void stop() {

	}

	@Override
	public boolean isSeekable() {
		return false;
	}

	@Override
	public long getPosition() {
		return 0;
	}

	@Override
	public void setPosition(long position) {

	}

	@Override
	public void setMarker(TrackMarker marker) {

	}

	@Override
	public long getDuration() {
		return 0;
	}

	@Override
	public AudioTrack makeClone() {
		return null;
	}

	@Override
	public AudioSourceManager getSourceManager() {
		return null;
	}

	@Override
	public void setUserData(Object userData) {

	}

	@Override
	public Object getUserData() {
		return null;
	}

	@Override
	public <T> T getUserData(Class<T> klass) {
		return null;
	}
}
