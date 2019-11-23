package com.soa.rs.discordbot.v3.jdbi.entities;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GuildUserTest {

	GuildUser user;
	Date date = new Date();

	@Before
	public void setUp() {
		user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		user.setJoinedServer(date);
		user.setLastSeen(date);
		user.setLeftServer(date);
		user.setLastActive(date);
	}

	@Test
	public void testEquals() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertTrue(user2.equals(user));
	}

	@Test
	public void testEqualsWithNullLeftDate() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLastActive(date);

		GuildUser user3 = new GuildUser();
		user3.setSnowflake(1234);
		user3.setGuildSnowflake(6789);
		user3.setUsername("@User#1234");
		user3.setKnownName("User");
		user3.setDisplayName("User");

		user3.setJoinedServer(date);
		user3.setLastSeen(date);
		user3.setLastActive(date);

		Assert.assertTrue(user2.equals(user3));
	}

	@Test
	public void testNotEqualsDifferentSnowflake() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(5678);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNotEqualsDifferentGuildSnowflake() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(1234);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNotEqualsDifferentUsername() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#5678");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNotEqualsDifferentKnownName() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("Some weird user");
		user2.setDisplayName("User");

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNotEqualsDifferentDisplayName()
	{
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("Some weird user");
		user2.setDisplayName("User2");

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNotEqualsDifferentJoinedDate() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user2.setJoinedServer(calendar.getTime());
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNotEqualsDifferentLastSeenDate() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user2.setJoinedServer(date);
		user2.setLastSeen(calendar.getTime());
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNotEqualsDifferentLeftServerDate() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(calendar.getTime());
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNotEqualsDifferentLastActiveDate() {
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(calendar.getTime());

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNotEqualsBecauseDifferentTypesOfObjects() {
		Assert.assertFalse(user.equals("Random"));
	}

	@Test
	public void testNullLeftServerDate()
	{
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNullLeftServerDateWithUser()
	{
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLastActive(date);

		Assert.assertFalse(user.equals(user2));
	}

	@Test
	public void testNullKnownName()
	{
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setDisplayName("User");

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertFalse(user2.equals(user));
	}

	@Test
	public void testNullKnownNameWithUser()
	{
		GuildUser user2 = new GuildUser();
		user2.setSnowflake(1234);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setDisplayName("User");

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user2.setJoinedServer(date);
		user2.setLastSeen(date);
		user2.setLeftServer(date);
		user2.setLastActive(date);

		Assert.assertFalse(user.equals(user2));
	}

}
