package com.soa.rs.discordbot.v3.usertrack;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;

public class NewUserUtilityTest {

	@Test
	public void createNewUserTest()
	{
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLastActive(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));

		NewUserUtility utility = new NewUserUtility();
		GuildUser user = NewUserUtility.createNewUser(member);
		Assert.assertTrue(user.equals(newUser));
	}
}
