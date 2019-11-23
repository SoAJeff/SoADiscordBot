package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.usertrack.RecentCache;
import com.soa.rs.discordbot.v3.usertrack.UserTrackMemberUpdated;

import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.event.domain.UserUpdateEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import reactor.core.publisher.Mono;

/**
 * Handles all 'update' events that affect a user - Member, User, and Presence
 */
public class MemberUpdateHandler {

	private UserTrackMemberUpdated userTrackMemberUpdated;

	public MemberUpdateHandler() {
		this.userTrackMemberUpdated = new UserTrackMemberUpdated();
		this.userTrackMemberUpdated.setGuildUserUtility(new GuildUserUtility());
		this.userTrackMemberUpdated.setNicknameUtility(new NicknameUtility());
		this.userTrackMemberUpdated.setRecentActionUtility(new RecentActionUtility());
	}

	public Mono<Void> handle(MemberUpdateEvent event) {
		return event.getMember().filter(ignored -> DiscordCfgFactory.getInstance().isUserTrackingEnabled())
				.filter(member -> !member.isBot())
				.flatMap(member -> Mono.fromRunnable(() -> userTrackMemberUpdated.handleMemberUpdate(member))).then();
	}

	public void handle(UserUpdateEvent event) {
		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled() && !event.getCurrent().isBot()) {
			userTrackMemberUpdated.handleUserUpdate(event.getCurrent());
		}
	}

	public Mono<Void> handle(PresenceUpdateEvent event) {
		return event.getMember().filter(ignored -> DiscordCfgFactory.getInstance().isUserTrackingEnabled())
				.filter(member -> !member.isBot())
				.flatMap(member -> userTrackMemberUpdated.handlePresenceUpdate(member, event)).then();
	}

	public void setCache(RecentCache cache) {
		this.userTrackMemberUpdated.setCache(cache);
	}
}
