package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.usertrack.UserTrackMemberLeft;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.BanEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.object.entity.channel.MessageChannel;

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
		if(memberLeaveEvent.getGuildId().asLong() == 133922153010692096L)
		{
			memberLeaveEvent.getGuild().flatMap(guild -> guild.getChannelById(Snowflake.of(974413726063132793L)))
					.flatMap(guildChannel -> ((MessageChannel) guildChannel).createMessage(
							memberLeaveEvent.getUser().getUsername() + "#" + memberLeaveEvent.getUser()
									.getDiscriminator() + " has left the server.")).subscribe();
		}
	}

	public void handle(BanEvent banEvent) {
		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
			SoaLogging.getLogger(this)
					.debug("Member [@" + banEvent.getUser().getUsername() + "#" + banEvent.getUser().getDiscriminator()
							+ ", " + banEvent.getUser().getId().asLong() + ", " + banEvent.getGuildId().asLong()
							+ "] was banned, marking as left server");
			this.memberLeft.handleUserBanned(banEvent.getUser().getId().asLong(), banEvent.getGuildId().asLong());
			if (banEvent.getGuildId().asLong() == 133922153010692096L) {
				banEvent.getGuild().flatMap(guild -> guild.getChannelById(Snowflake.of(974413726063132793L))).flatMap(
						guildChannel -> ((MessageChannel) guildChannel).createMessage(
								banEvent.getUser().getUsername() + "#" + banEvent.getUser().getDiscriminator()
										+ " was banned from the server.")).subscribe();
			}
		}
	}
}
