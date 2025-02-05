package com.soa.rs.discordbot.v3.usertrack;

import java.util.List;
import java.util.stream.Collectors;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.GuildUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildEntry;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public class UserTrackGuildCreate {

	private GuildUtility guildUtil;
	private GuildUserUtility userUtility;
	private NicknameUtility nicknameUtility;
	private RecentActionUtility recentActionUtility;
	private RecentCache lastSeenCache;
	private RecentCache lastActiveCache;

	public void setGuildUtil(GuildUtility guildUtil) {
		this.guildUtil = guildUtil;
	}

	public void setUserUtility(GuildUserUtility userUtility) {
		this.userUtility = userUtility;
	}

	public void setNicknameUtility(NicknameUtility nicknameUtility) {
		this.nicknameUtility = nicknameUtility;
	}

	public void setLastSeenCache(RecentCache lastSeenCache) {
		this.lastSeenCache = lastSeenCache;
	}

	public void setLastActiveCache(RecentCache lastActiveCache) {
		this.lastActiveCache = lastActiveCache;
	}

	public void setRecentActionUtility(RecentActionUtility recentActionUtility) {
		this.recentActionUtility = recentActionUtility;
	}

	public void handleJoinedGuild(GuildCreateEvent event) {
		Guild guild = event.getGuild();
		SoaLogging.getLogger(this).info("Joined server [" + guild.getName() + ", " + guild.getId().asLong() + "]");
		checkAndUpdateGuildDb(guild.getId().asLong(), guild.getName());
		GuildCreateMemberReviewer reviewer = createReviewer();
		lastSeenCache.addNewGuild(guild.getId().asLong());
		lastActiveCache.addNewGuild(guild.getId().asLong());
		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		reviewer.setGuildId(guild.getId().asLong());
		reviewer.setGuildUsers(userUtility.getUsersCurrentlyInGuildForGuildId(guild.getId().asLong()).stream()
				.map(GuildUser::getSnowflake).collect(Collectors.toList()));
		reviewer.setAllUsers(userUtility.getUsersCurrentlyInGuildForGuildId(guild.getId().asLong()));
		reviewer.setLeftUsers(userUtility.getLeftUsersInGuildForGuildId(guild.getId().asLong()));
		guild.getMembers().filter(member -> !member.isBot()).flatMap(reviewer::reviewMember)
				.doOnComplete(reviewer::submitUsers).doOnComplete(reviewer::removeRemainingUsers).onErrorResume(
						err -> Mono.fromRunnable(() -> SoaLogging.getLogger(this)
								.error("Unexpected error occurred during guild create event.", err))).subscribe();

	}

	void checkAndUpdateGuildDb(long snowflake, String guildName) {
		List<GuildEntry> guildsWithId = guildUtil.getGuildById(snowflake);

		if (guildsWithId.size() == 1) {
			if (!guildName.equals(guildsWithId.get(0).getGuildName())) {
				guildUtil.updateGuildInfo(snowflake, guildName);
			}
		} else {
			guildUtil.addNewGuild(snowflake, guildName);
		}
	}

	GuildCreateMemberReviewer createReviewer() {
		return new GuildCreateMemberReviewer();
	}

}
