package com.soa.rs.discordbot.v3.usertrack;

import java.sql.Date;
import java.time.Instant;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.object.entity.Member;

public class UserTrackMemberLeft {

	private GuildUserUtility guildUserUtility;
	private RecentActionUtility recentActionUtility;

	public void handleMemberLeft(Member member) {
		SoaLogging.getLogger(this)
				.debug("Member [" + member.getDisplayName() + ", " + member.getId().asLong() + ", " + member
						.getGuildId().asLong() + "] has left the server");
		this.guildUserUtility
				.setLeftDateForUser(Date.from(Instant.now()), member.getId().asLong(), member.getGuildId().asLong());
		this.recentActionUtility
				.addRecentAction(member.getGuildId().asLong(), member.getId().asLong(), "Left the server");
	}

	public void handleUserBanned(long userId, long guildId) {
		this.guildUserUtility.setLeftDateForUser(Date.from(Instant.now()), userId, guildId);
		this.recentActionUtility.addRecentAction(guildId, userId, "Was banned from the server");
	}

	public void setGuildUserUtility(GuildUserUtility guildUserUtility) {
		this.guildUserUtility = guildUserUtility;
	}

	public void setRecentActionUtility(RecentActionUtility recentActionUtility) {
		this.recentActionUtility = recentActionUtility;
	}
}
