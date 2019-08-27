package com.soa.rs.discordbot.v3.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

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
		//return member.getRoles().toStream().anyMatch(role -> mustHavePermission.contains(role.getName()));

		//Shadbot started doing this, but i can't get it to work...
		//		event.getMember().filter(member -> !member.isBot()).flatMap(member -> member.getRoles().collectList())
		//				.filter(roles -> roles.stream)

		//		Mono<Member> member = event.getMessage().getAuthorAsMember();
		//		return member.flatMapMany(Membember::getRoles).any(role -> mustHavePermission.contains(role.getName()));

		//		return member.getRoles().map(Role::getName).any(::equals);
		//		List<Role> roleList = event.getMessage().getAuthorAsMember().flatMapMany(user -> user.getRoles()).;
		//		event.getMessage().getAuthorAsMember().map(user -> user.getRoles()).map(Role::getName)).filter(s -> mustHavePermission.containins(s));
		/*return event.getMessage().getAuthorAsMember().flatMapMany(user -> user.getRoles())
				.flatMap(role -> Mono.just(role.getName())).filter(s -> mustHavePermission.contains(s));*/
/*		Mono<List<Role>> guildRoles = event.getGuild().flatMapMany(Guild::getRoles).collectList();
		Mono<List<Role>> membersRoles = event.getMember().filter(member -> member.getRoles().any(guildRoles));*/
		//		return event.getMember().
/*		if (SoaClientHelper.isRank(event.getMessage(), mustHavePermission)) {
			return true;
		} else {
			return false;
		}*/
	}

	protected void addHelpMsg(String command, String text) {
		DiscordCfgFactory.getInstance().addHelpMessage(command, text);
	}

	/**
	 * An initialize method for the event to do whatever it needs to initialize that shouldn't be done in the constructor
	 */
	public abstract void initialize();

	public abstract Mono<Void> execute(MessageCreateEvent event);
}
