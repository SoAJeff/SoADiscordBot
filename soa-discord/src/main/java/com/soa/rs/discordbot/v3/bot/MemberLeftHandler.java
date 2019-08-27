package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.usertrack.UserTrackMemberLeft;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.guild.BanEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;

public class MemberLeftHandler {

	private UserTrackMemberLeft memberLeft;

	public MemberLeftHandler() {
		this.memberLeft = new UserTrackMemberLeft();
		this.memberLeft.setGuildUserUtility(new GuildUserUtility());
		this.memberLeft.setRecentActionUtility(new RecentActionUtility());
	}

	public void handle(MemberLeaveEvent memberLeaveEvent) {
		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled() && memberLeaveEvent.getMember().isPresent()) {
			if (!memberLeaveEvent.getMember().get().isBot()) {
				this.memberLeft.handleMemberLeft(memberLeaveEvent.getMember().get());
			}
		}
	}

	public void handle(BanEvent banEvent) {
		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
			SoaLogging.getLogger(this)
					.debug("Member [@" + banEvent.getUser().getUsername() + "#" + banEvent.getUser().getDiscriminator()
							+ ", " + banEvent.getUser().getId().asLong() + ", " + banEvent.getGuildId().asLong()
							+ "] was banned, marking as left server");
			this.memberLeft.handleUserBanned(banEvent.getUser().getId().asLong(), banEvent.getGuildId().asLong());
		}
	}
}
