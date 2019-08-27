package com.soa.rs.discordbot.v3.jdbi.entities;

public class UsernameRecentAction extends RecentAction {

	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
