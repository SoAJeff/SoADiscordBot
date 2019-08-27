package com.soa.rs.discordbot.v3.bot;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.GuildUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.usertrack.RecentlySeenCache;
import com.soa.rs.discordbot.v3.usertrack.UserTrackGuildCreate;

import discord4j.core.event.domain.guild.GuildCreateEvent;

public class GuildCreateHandler {

	private UserTrackGuildCreate guildCreate;

	public GuildCreateHandler() {
		this.guildCreate = new UserTrackGuildCreate();
		this.guildCreate.setGuildUtil(new GuildUtility());
		this.guildCreate.setNicknameUtility(new NicknameUtility());
		this.guildCreate.setUserUtility(new GuildUserUtility());
		this.guildCreate.setRecentActionUtility(new RecentActionUtility());
	}

	public void handleGuildCreate(GuildCreateEvent event) {
		if (DiscordCfgFactory.getConfig().getUserTrackingEvent() != null && DiscordCfgFactory.getConfig()
				.getUserTrackingEvent().isEnabled()) {
			guildCreate.handleJoinedGuild(event);
		}
	}

	public void setCache(RecentlySeenCache cache) {
		this.guildCreate.setCache(cache);
	}
}
