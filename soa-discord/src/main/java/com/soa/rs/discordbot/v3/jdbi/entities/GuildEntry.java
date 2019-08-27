package com.soa.rs.discordbot.v3.jdbi.entities;

public class GuildEntry {

	private long snowflake;

	private String guildName;

	public long getSnowflake() {
		return snowflake;
	}

	public void setSnowflake(long snowflake) {
		this.snowflake = snowflake;
	}

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}
}
