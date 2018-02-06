package com.soa.rs.triviacreator.util;

public enum DiscordType {
	SERVER("Server"),
	CHANNEL("Channel");

	private String type;

	DiscordType(String type)
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}
}
