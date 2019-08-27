package com.soa.rs.discordbot.v3.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.soa.rs.discordbot.v3.api.annotation.MessageRcv;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public abstract class MsgRcvd {

	private final List<String> triggers;

	private boolean isEnabled;


	public MsgRcvd()
	{
		final MessageRcv cmdAnnotation = this.getClass().getAnnotation(MessageRcv.class);
		this.triggers = new ArrayList<>(Arrays.asList(cmdAnnotation.triggers()));
	}

	public List<String> getTriggers() {
		return triggers;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}

	/**
	 * An initialize method for the event to do whatever it needs to initialize that shouldn't be done in the constructor
	 */
	public abstract void initialize();

	public abstract Mono<Void> execute(MessageCreateEvent event);
}
