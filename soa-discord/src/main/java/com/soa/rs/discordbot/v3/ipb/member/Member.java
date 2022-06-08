package com.soa.rs.discordbot.v3.ipb.member;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Member {
	private int id;
	private String name;
	private Group primaryGroup;
	private String birthday;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Group getPrimaryGroup() {
		return primaryGroup;
	}

	public String getBirthday() {
		return birthday;
	}
}
