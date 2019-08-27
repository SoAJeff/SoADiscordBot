package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.usertrack.UserTrackMemberJoin;

import discord4j.core.event.domain.guild.MemberJoinEvent;

public class MemberJoinHandler {

	private UserTrackMemberJoin userTrackMemberJoin;

	public MemberJoinHandler() {
		this.userTrackMemberJoin = new UserTrackMemberJoin();
		this.userTrackMemberJoin.setGuildUserUtility(new GuildUserUtility());
		this.userTrackMemberJoin.setNicknameUtility(new NicknameUtility());
		this.userTrackMemberJoin.setRecentActionUtility(new RecentActionUtility());
	}

	public void handle(MemberJoinEvent event) {
		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled() && !event.getMember().isBot()) {
			userTrackMemberJoin.handleAddUser(event.getMember());
		}
	}
}
