package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.usertrack.UserTrackMemberJoin;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import reactor.core.publisher.Mono;

public class MemberJoinHandler {

	private UserTrackMemberJoin userTrackMemberJoin;

	public MemberJoinHandler() {
		this.userTrackMemberJoin = new UserTrackMemberJoin();
		this.userTrackMemberJoin.setGuildUserUtility(new GuildUserUtility());
		this.userTrackMemberJoin.setNicknameUtility(new NicknameUtility());
		this.userTrackMemberJoin.setRecentActionUtility(new RecentActionUtility());
	}

	public Mono<Void> handle(MemberJoinEvent event) {
		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled() && !event.getMember().isBot()) {
			return Mono.fromRunnable(()->userTrackMemberJoin.handleAddUser(event.getMember()));
		}
		return Mono.empty();
	}
}
