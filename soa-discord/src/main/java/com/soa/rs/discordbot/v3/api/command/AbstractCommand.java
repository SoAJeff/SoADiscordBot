package com.soa.rs.discordbot.v3.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import reactor.core.publisher.Mono;

public abstract class AbstractCommand {

	private final List<String> triggers;

	private boolean isEnabled;

	/**
	 * List of ranks of which the user who triggered the event must be in order to
	 * execute it.
	 */
	private List<String> mustHavePermission = new ArrayList<>();

	public AbstractCommand() {
		final Command cmdAnnotation = this.getClass().getAnnotation(Command.class);
		this.triggers = new ArrayList<>(Arrays.asList(cmdAnnotation.triggers()));
	}

	public void setMustHavePermission(List<String> ranks) {
		this.mustHavePermission.addAll(ranks);
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

	protected Mono<Role> permittedToExecuteEvent(Member member) {
		return member.getRoles()
				.filter(role -> mustHavePermission.contains(role.getName()) || mustHavePermission.isEmpty()).next()
				.switchIfEmpty(Mono.fromRunnable(
						() -> SoaLogging.getLogger(this).debug("User does not have permission to run event")));
	}

	protected void addHelpMsg(String command, String text) {
		DiscordCfgFactory.getInstance().addHelpMessage(command, text);
	}

	/**
	 * An initialize method for the event to do whatever it needs to initialize that shouldn't be done in the constructor
	 */
	public abstract void initialize();

	public abstract Mono<Void> execute(MessageCreateEvent event);

	public abstract Mono<Void> execute(ChatInputInteractionEvent event);
}
