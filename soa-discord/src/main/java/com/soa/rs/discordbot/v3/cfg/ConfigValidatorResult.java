package com.soa.rs.discordbot.v3.cfg;

public class ConfigValidatorResult {

	private boolean eventWasInvalid = false;

	private boolean configValidSchema = false;

	public boolean isEventWasInvalid() {
		return eventWasInvalid;
	}

	public void setEventWasInvalid(boolean eventWasInvalid) {
		this.eventWasInvalid = eventWasInvalid;
	}

	public boolean isConfigValidSchema() {
		return configValidSchema;
	}

	public void setConfigFailedSchema(boolean configFailedSchema) {
		this.configValidSchema = configFailedSchema;
	}

}
