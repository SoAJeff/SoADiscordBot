package com.soa.rs.discordbot.v3.usertrack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;

public class UserTrackMemberJoinTest {

	@Test
	public void handleCheckIfPreviouslyInGuildTest()
	{
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		GuildUser user = new GuildUser();
		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));

		List<GuildUser> guildUsers = new ArrayList<>();
		guildUsers.add(user);

		Mockito.when(guildUserUtility.getGuildUser(member.getId().asLong(), member.getGuildId().asLong())).thenReturn(guildUsers);

		UserTrackMemberJoin utmj = new UserTrackMemberJoin();
		utmj.setGuildUserUtility(guildUserUtility);
		Assert.assertTrue(utmj.checkIfMemberPreviouslyInGuild(member));
	}

	@Test
	public void handleCheckIfPreviouslyInGuildTestAndReturnsFalse()
	{
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));

		List<GuildUser> guildUsers = new ArrayList<>();

		Mockito.when(guildUserUtility.getGuildUser(member.getId().asLong(), member.getGuildId().asLong())).thenReturn(guildUsers);

		UserTrackMemberJoin utmj = new UserTrackMemberJoin();
		utmj.setGuildUserUtility(guildUserUtility);
		Assert.assertFalse(utmj.checkIfMemberPreviouslyInGuild(member));
	}

	@Test
	public void handleUpdateExistingUserTest()
	{
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setLeftServer(Date.from(Instant.EPOCH));

		List<GuildUser> users = new ArrayList<>();
		users.add(user);

		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		List<String> names = new ArrayList<>();
		names.add("user2");

		Mockito.when(nicknameUtility.getNicknamesForUser(1234, 6789)).thenReturn(names);

		UserTrackMemberJoin utmj = new UserTrackMemberJoin();
		utmj.setGuildUserUtility(guildUserUtility);
		utmj.setNicknameUtility(nicknameUtility);
		utmj.setRecentActionUtility(recentActionUtility);
		utmj.handleUpdateExistingUser(member);

		Mockito.verify(guildUserUtility).updateExistingUser(user);
		Mockito.verify(nicknameUtility).addNickname(1234, 6789, "User1");
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Joined the server");

		Assert.assertEquals(Date.from(Instant.EPOCH), user.getLeftServerAsDate());

	}

	@Test
	public void handleUpdateExistingUserTestSameDisplayName()
	{
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setLeftServer(new Date());

		List<GuildUser> users = new ArrayList<>();
		users.add(user);

		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		List<String> names = new ArrayList<>();
		names.add("User1");

		Mockito.when(nicknameUtility.getNicknamesForUser(1234, 6789)).thenReturn(names);

		UserTrackMemberJoin utmj = new UserTrackMemberJoin();
		utmj.setGuildUserUtility(guildUserUtility);
		utmj.setNicknameUtility(nicknameUtility);
		utmj.setRecentActionUtility(recentActionUtility);
		utmj.handleUpdateExistingUser(member);

		Mockito.verify(guildUserUtility).updateExistingUser(user);
		Mockito.verify(nicknameUtility, Mockito.never()).addNickname(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Joined the server");

		Assert.assertEquals(Date.from(Instant.EPOCH), user.getLeftServerAsDate());

	}

	@Test
	public void handleAddUserWithExistingUser()
	{
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setLeftServer(Date.from(Instant.EPOCH));

		List<GuildUser> users = new ArrayList<>();
		users.add(user);

		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		List<String> names = new ArrayList<>();
		names.add("User1");

		Mockito.when(nicknameUtility.getNicknamesForUser(1234, 6789)).thenReturn(names);

		UserTrackMemberJoin utmj = new UserTrackMemberJoin();
		utmj.setGuildUserUtility(guildUserUtility);
		utmj.setNicknameUtility(nicknameUtility);
		utmj.setRecentActionUtility(recentActionUtility);

		utmj.handleAddUser(member);

		Mockito.verify(guildUserUtility).updateExistingUser(user);
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Joined the server");
	}

	@Test
	public void handleAddUserTestNewUser()
	{
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		List<GuildUser> users = new ArrayList<>();

		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		UserTrackMemberJoin utmj = new UserTrackMemberJoin();
		utmj.setGuildUserUtility(guildUserUtility);
		utmj.setNicknameUtility(nicknameUtility);
		utmj.setRecentActionUtility(recentActionUtility);

		utmj.handleAddUser(member);

		Mockito.verify(guildUserUtility).addNewUser(Mockito.any(GuildUser.class));
		Mockito.verify(nicknameUtility).addNickname(1234, 6789, "User1");
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Joined the server");
	}
}
