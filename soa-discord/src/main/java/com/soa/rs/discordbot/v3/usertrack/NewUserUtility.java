package com.soa.rs.discordbot.v3.usertrack;

import java.time.Instant;
import java.util.Date;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

import discord4j.core.object.entity.Member;

public class NewUserUtility {

	public static GuildUser createNewUser(Member member) {
		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(member.getId().asLong());
		newUser.setGuildSnowflake(member.getGuildId().asLong());
		String memberName = "@" + member.getUsername() + "#" + member.getDiscriminator();
		newUser.setUsername(memberName);
		newUser.setDisplayName(member.getDisplayName());
		if(member.getJoinTime().isPresent()) {
			newUser.setJoinedServer(Date.from(member.getJoinTime().get()));
			newUser.setLastSeen(Date.from(member.getJoinTime().get()));
			newUser.setLastActive(Date.from(member.getJoinTime().get()));
		}
		newUser.setLeftServer(Date.from(Instant.EPOCH));

		return newUser;
	}
}
