package com.soa.rs.discordbot.v3.jdbi.entities;

public class GuildServerUser extends GuildUser {

	private String guildName;

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}
}
