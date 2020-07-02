package com.soa.rs.discordbot.v3.usertrack;

import java.util.Date;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;

import org.junit.Test;
import org.mockito.Mockito;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;

public class UserTrackMemberLeftTest {

	@Test
	public void testMemberLeft() {
		GuildUserUtility utility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));

		UserTrackMemberLeft memberLeft = new UserTrackMemberLeft();
		memberLeft.setGuildUserUtility(utility);
		memberLeft.setRecentActionUtility(recentActionUtility);

		memberLeft.handleMemberLeft(member);

		Mockito.verify(utility, Mockito.times(1))
				.setLeftDateForUser(Mockito.any(Date.class), Mockito.eq(1234L), Mockito.eq(6789L));
		Mockito.verify(recentActionUtility, Mockito.times(1)).addRecentAction(6789, 1234, "Left the server");
	}

	@Test
	public void testUserBanned() {
		GuildUserUtility utility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);

		UserTrackMemberLeft memberLeft = new UserTrackMemberLeft();
		memberLeft.setGuildUserUtility(utility);
		memberLeft.setRecentActionUtility(recentActionUtility);

		memberLeft.handleUserBanned(1234, 6789);

		Mockito.verify(utility, Mockito.times(1))
				.setLeftDateForUser(Mockito.any(Date.class), Mockito.eq(1234L), Mockito.eq(6789L));
		Mockito.verify(recentActionUtility, Mockito.times(1)).addRecentAction(6789, 1234, "Was banned from the server");

	}
}
