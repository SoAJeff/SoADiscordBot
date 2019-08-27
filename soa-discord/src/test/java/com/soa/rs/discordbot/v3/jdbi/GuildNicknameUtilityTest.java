package com.soa.rs.discordbot.v3.jdbi;

import java.util.Date;
import java.util.List;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildServerUser;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GuildNicknameUtilityTest {

	private GuildUserUtility userUtility = new GuildUserUtility();
	private NicknameUtility nicknameUtility = new NicknameUtility();
	private GuildNicknameUtility guildNicknameUtility = new GuildNicknameUtility();
	private GuildUtility guildUtility = new GuildUtility();

	@BeforeClass
	public static void setUp() {
		Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
		JdbiWrapper.getInstance().setJdbi(jdbi);
	}

	@Before
	public void createDb() {
		userUtility.createUsersTable();
		nicknameUtility.createNicknamesTable();
		guildUtility.createGuildsTable();
	}

	@After
	public void tearDown() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> {
			handle.execute("drop table users");
			handle.execute("drop table nicknames");
			handle.execute("drop table guilds");
		});
	}

	@Test
	public void testGetDistinctByUsername()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		List<GuildUser> users = guildNicknameUtility.getGuildUser("User");

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByKnown()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		List<GuildUser> users = guildNicknameUtility.getGuildUser("Known");

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByDisplay()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		List<GuildUser> users = guildNicknameUtility.getGuildUser("Display");

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByDisplayHistory()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		List<GuildUser> users = guildNicknameUtility.getGuildUser("DispHistory");

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByAllWithSame()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "User");

		List<GuildUser> users = guildNicknameUtility.getGuildUser("User");

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByUsernameAndGuild()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		List<GuildUser> users = guildNicknameUtility.getGuildUserWithNameInGuild("User", 6789);

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByKnownAndGuild()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		List<GuildUser> users = guildNicknameUtility.getGuildUserWithNameInGuild("Known", 6789);

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByDisplayAndGuild()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		List<GuildUser> users = guildNicknameUtility.getGuildUserWithNameInGuild("Display", 6789);

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByDisplayHistoryAndGuild()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DisplayHistory");

		List<GuildUser> users = guildNicknameUtility.getGuildUserWithNameInGuild("DisplayHistory", 6789);

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByAllWithSameAndGuild()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "User");

		List<GuildUser> users = guildNicknameUtility.getGuildUserWithNameInGuild("User", 6789);

		Assert.assertEquals(1, users.size());
	}

	@Test
	public void testGetDistinctByAllWithSameAndNotMatchingGuild()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "User");

		List<GuildUser> users = guildNicknameUtility.getGuildUserWithNameInGuild("User", 1234);

		Assert.assertEquals(0, users.size());
	}

	//-------------------------

	@Test
	public void testGetDistinctByUsernameAndServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithServerName("User");

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());
	}

	@Test
	public void testGetDistinctByKnownAndServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithServerName("Known");

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());

	}

	@Test
	public void testGetDistinctByDisplayAndServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithServerName("Display");

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());
	}

	@Test
	public void testGetDistinctByDisplayHistoryWithServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");

		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithServerName("DispHistory");

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());
	}

	@Test
	public void testGetDistinctByAllWithSameWithServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "User");
		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithServerName("User");

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());
	}

	@Test
	public void testGetDistinctByUsernameAndGuildWithServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");
		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithNameInGuildWithServerName("User", 6789);

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());
	}

	@Test
	public void testGetDistinctByKnownAndGuildWithServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");
		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithNameInGuildWithServerName("Known", 6789);

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());
	}

	@Test
	public void testGetDistinctByDisplayAndGuildWithServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DispHistory");
		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithNameInGuildWithServerName("Display", 6789);

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());

	}

	@Test
	public void testGetDistinctByDisplayHistoryAndGuildWithServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("Known");
		user.setDisplayName("Display");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "DisplayHistory");
		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithNameInGuildWithServerName("DisplayHistory", 6789);

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());
	}

	@Test
	public void testGetDistinctByAllWithSameAndGuildWithServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "User");
		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithNameInGuildWithServerName("User", 6789);

		Assert.assertEquals(1, users.size());
		Assert.assertEquals("Test Guild", users.get(0).getGuildName());
	}

	@Test
	public void testGetDistinctByAllWithSameAndNotMatchingGuildWithServerName()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		nicknameUtility.addNickname(1234, 6789, "User");
		guildUtility.addNewGuild(6789, "Test Guild");

		List<GuildServerUser> users = guildNicknameUtility.getGuildUserWithNameInGuildWithServerName("User", 1234);

		Assert.assertEquals(0, users.size());
	}
}
