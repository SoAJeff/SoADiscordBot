package com.soa.rs.discordbot.v3.usertrack;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.object.entity.Member;

public class UserTrackMemberJoin {

	private GuildUserUtility guildUserUtility;
	private NicknameUtility nicknameUtility;
	private RecentActionUtility recentActionUtility;

	public void handleAddUser(Member member) {
		if (checkIfMemberPreviouslyInGuild(member)) {
			//User was previously in, null out their left date and update everything else
			handleUpdateExistingUser(member);
		} else {
			//User never in guild, create a new one and populate it
			SoaLogging.getLogger(this)
					.debug("Member [" + member.getDisplayName() + ", " + member.getId().asLong() + ", " + member
							.getGuildId().asLong() + "] is new user, creating new user entry");
			GuildUser user = NewUserUtility.createNewUser(member);
			guildUserUtility.addNewUser(user);
			this.nicknameUtility.addNickname(user.getSnowflake(), user.getGuildSnowflake(), member.getDisplayName());
			this.recentActionUtility
					.addRecentAction(member.getGuildId().asLong(), member.getId().asLong(), "Joined the server");
		}
	}

	boolean checkIfMemberPreviouslyInGuild(Member member) {
		return guildUserUtility.getGuildUser(member.getId().asLong(), member.getGuildId().asLong()).size() == 1;
	}

	void handleUpdateExistingUser(Member member) {
		SoaLogging.getLogger(this)
				.debug("Member [" + member.getDisplayName() + ", " + member.getId().asLong() + ", " + member
						.getGuildId().asLong() + "] is existing user, updating user entry");
		GuildUser user = this.guildUserUtility.getGuildUser(member.getId().asLong(), member.getGuildId().asLong())
				.get(0);
		String memberName = "@" + member.getUsername() + "#" + member.getDiscriminator();
		user.setUsername(memberName);
		user.setDisplayName(member.getDisplayName());
		if(member.getJoinTime().isPresent()) {
			user.setJoinedServer(Date.from(member.getJoinTime().get()));
		}
		user.setLastSeen(Date.from(Instant.now()));
		user.setLeftServer(Date.from(Instant.EPOCH));

		List<String> nickNames = this.nicknameUtility
				.getNicknamesForUser(user.getSnowflake(), user.getGuildSnowflake());

		if (!nickNames.contains(member.getDisplayName())) {
			this.nicknameUtility.addNickname(user.getSnowflake(), user.getGuildSnowflake(), member.getDisplayName());
		}

		this.recentActionUtility
				.addRecentAction(member.getGuildId().asLong(), member.getId().asLong(), "Joined the server");

		this.guildUserUtility.updateExistingUser(user);

	}

	public void setGuildUserUtility(GuildUserUtility guildUserUtility) {
		this.guildUserUtility = guildUserUtility;
	}

	public void setNicknameUtility(NicknameUtility nicknameUtility) {
		this.nicknameUtility = nicknameUtility;
	}

	public void setRecentActionUtility(RecentActionUtility recentActionUtility) {
		this.recentActionUtility = recentActionUtility;
	}
}
