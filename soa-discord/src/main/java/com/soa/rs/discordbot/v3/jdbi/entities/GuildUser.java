package com.soa.rs.discordbot.v3.jdbi.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GuildUser {

	private long snowflake;

	private long guildSnowflake;

	private String username;

	private String knownName;

	private String displayName;

	private Date lastSeen;

	private Date joinedServer;

	private Date leftServer;

	private Date lastActive;

	public long getSnowflake() {
		return snowflake;
	}

	public void setSnowflake(long snowflake) {
		this.snowflake = snowflake;
	}

	public long getGuildSnowflake() {
		return guildSnowflake;
	}

	public void setGuildSnowflake(long guildSnowflake) {
		this.guildSnowflake = guildSnowflake;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getKnownName() {
		return knownName;
	}

	public void setKnownName(String knownName) {
		this.knownName = knownName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public Date getJoinedServer() {
		return joinedServer;
	}

	public void setJoinedServer(Date joinedServer) {
		this.joinedServer = joinedServer;
	}

	public Date getLeftServer() {
		return leftServer;
	}

	public void setLeftServer(Date leftServer) {
		this.leftServer = leftServer;
	}

	public Date getLastActive() {
		return lastActive;
	}

	public void setLastActive(Date lastActive) {
		this.lastActive = lastActive;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GuildUser))
			return false;
		GuildUser comparedUser = (GuildUser) obj;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		if (!(comparedUser.getSnowflake() == getSnowflake()))
			return false;
		if (!(comparedUser.getGuildSnowflake() == getGuildSnowflake()))
			return false;
		if (!(sdf.format(comparedUser.getJoinedServer()).equals(sdf.format(getJoinedServer()))))
			return false;
		if(!(sdf.format(comparedUser.getLastActive()).equals(sdf.format(getLastActive()))))
			return false;

		//KnownName can be null
		if (comparedUser.getKnownName() != null) {
			if (getKnownName() != null) {
				if (!comparedUser.getKnownName().equals(getKnownName())) {
					return false;
				}
			} else
				return false;
		} else if (getKnownName() != null)
			return false;

		if (!(comparedUser.getUsername().equals(getUsername())))
			return false;
		if (!(sdf.format(comparedUser.getLastSeen()).equals(sdf.format(getLastSeen()))))
			return false;
		if(!(comparedUser.getDisplayName().equals(getDisplayName())))
			return false;

		//Leftserver WILL be null if someone is in the server
		if (comparedUser.getLeftServer() != null) {
			if (getLeftServer() != null) {
				if (!(sdf.format(comparedUser.getLeftServer()).equals(sdf.format(getLeftServer()))))
					return false;
			} else {
				return false;
			}
		} else if (getLeftServer() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return 10;
	}
}
