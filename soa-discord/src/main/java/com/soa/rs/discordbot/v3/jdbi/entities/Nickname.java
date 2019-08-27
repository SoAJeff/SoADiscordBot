package com.soa.rs.discordbot.v3.jdbi.entities;

public class Nickname {

	private long userSnowflake;

	private long guildSnowflake;

	private String displayName;

	public long getUserSnowflake() {
		return userSnowflake;
	}

	public void setUserSnowflake(long userSnowflake) {
		this.userSnowflake = userSnowflake;
	}

	public long getGuildSnowflake() {
		return guildSnowflake;
	}

	public void setGuildSnowflake(long guildSnowflake) {
		this.guildSnowflake = guildSnowflake;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
