package com.soa.rs.discordbot.v3.cfg;

public enum EventTypes {

	EVENT_LISTING_EVENT("Event Listing Event"), NEWS_LISTING_EVENT("News Listing Event"), MUSIC_PLAYER(
			"Music Player"), DJ_PLS_EVENT("DJ Pls Event"), TRIVIA_EVENT(
					"Trivia Event"), ADMIN_EVENT("Admin Event"), USER_TRACKING_EVENT("User Tracking Event");

	private String friendlyName;

	EventTypes(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}
}
