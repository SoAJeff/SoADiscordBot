package com.soa.rs.discordbot.v3.jdbi.entities;

import java.util.Date;

public class RecentAction {

	private int actionId;
	private Date date;
	private long guildSnowflake;
	private long userSnowflake;
	private String action;
	private String originalValue;
	private String newValue;

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getGuildSnowflake() {
		return guildSnowflake;
	}

	public void setGuildSnowflake(long guildSnowflake) {
		this.guildSnowflake = guildSnowflake;
	}

	public long getUserSnowflake() {
		return userSnowflake;
	}

	public void setUserSnowflake(long userSnowflake) {
		this.userSnowflake = userSnowflake;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getOriginalValue() {
		return originalValue;
	}

	public void setOriginalValue(String originalValue) {
		this.originalValue = originalValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
}
