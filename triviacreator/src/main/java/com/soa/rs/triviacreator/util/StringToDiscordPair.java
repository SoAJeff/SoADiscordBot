package com.soa.rs.triviacreator.util;

public class StringToDiscordPair {

	private String name;
	private String id;

	public StringToDiscordPair(String name, String id)
	{
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}