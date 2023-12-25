package com.soa.rs.discordbot.v3.jdbi;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GuildUserUtilityTest {

	private GuildUserUtility userUtility = new GuildUserUtility();

	@BeforeClass
	public static void setUp() {
		Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
		JdbiWrapper.getInstance().setJdbi(jdbi);
	}

	@Before
	public void createDb() {
		userUtility.createUsersTable();
	}

	@After
	public void tearDown() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute("drop table users"));
	}

	@Test
	public void addUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);
		user.setLastActive(date);

		userUtility.addNewUser(user);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);
		Assert.assertEquals(user2.getSnowflake(), 1234);
		Assert.assertEquals(user2.getGuildSnowflake(), 6789);
		Assert.assertEquals(user2.getUsername(), "@User#1234");
		Assert.assertEquals(user2.getKnownName(), "User");
		Assert.assertEquals(user2.getDisplayName(), "User");

		System.out.println("Joined Server: " + user.getJoinedServer());
		Assert.assertEquals(date, user2.getJoinedServer());
		Assert.assertEquals(date, user2.getLastSeen());
		Assert.assertEquals(date, user2.getLastActive());
		Assert.assertNull(user.getLeftServerAsDate());
	}

	@Test
	public void addTwoUsersWithSameSnowflakes() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		try {
			userUtility.addNewUser(user);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("Unique index or primary key violation"));
		}

	}

	@Test
	public void updateUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user.setKnownName("user2");

		userUtility.updateExistingUser(user);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		Assert.assertEquals(user2.getKnownName(), "user2");
	}

	@Test
	public void updateUsers() {
		List<GuildUser> updatedUsers = new ArrayList<>();
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);
		updatedUsers.add(user);

		GuildUser user2 = new GuildUser();
		user2.setSnowflake(5678);
		user2.setGuildSnowflake(6789);
		user2.setUsername("@User#1234");
		user2.setKnownName("User");
		user2.setDisplayName("User");
		user2.setJoinedServer(new Date());
		user2.setLastSeen(new Date());
		user2.setLastActive(new Date());

		userUtility.addNewUser(user2);
		updatedUsers.add(user2);

		for (GuildUser user3 : updatedUsers) {
			user3.setKnownName("User3");
		}

		userUtility.updateExistingUsers(updatedUsers);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		for (GuildUser user4 : users) {
			Assert.assertEquals("User3", user4.getKnownName());
		}
	}

	@Test
	public void updateKnownNameForExistingUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		userUtility.updateKnownNameForUser("User2", 1234, 6789);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		Assert.assertEquals(user2.getKnownName(), "User2");
	}

	@Test
	public void updateKnownNameForNonExistantUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		userUtility.updateKnownNameForUser("User2", 6789, 1234);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from users where knownname='User2'").mapToBean(GuildUser.class)
						.list());

		Assert.assertEquals(users.size(), 0);
	}

	@Test
	public void updateDisplayNameForExistingUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		userUtility.updateDisplayNameForUser("User2", 1234, 6789);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		Assert.assertEquals(user2.getDisplayName(), "User2");
	}

	@Test
	public void updateDisplayNameForNonExistantUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		userUtility.updateDisplayNameForUser("User2", 6789, 1234);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from users where displayname='User2'").mapToBean(GuildUser.class)
						.list());

		Assert.assertEquals(users.size(), 0);
	}

	@Test
	public void updateUsernameForExistingUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		userUtility.updateUserNameForUser("user2#5678", 1234, 6789);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		Assert.assertEquals(user2.getUsername(), "user2#5678");
	}

	@Test
	public void updateLastSeenForExistingUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);
		user.setLastActive(date);

		userUtility.addNewUser(user);

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		userUtility.updateLastSeenForUser(calendar.getTime(), 1234, 6789);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM");

		Assert.assertEquals(calendar.getTime(), user2.getLastSeen());
	}

	@Test
	public void updateLastSeenForExistingUserViaMap() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);
		user.setLastActive(date);

		userUtility.addNewUser(user);

		user.setSnowflake(5678);
		userUtility.addNewUser(user);

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		Map<Long, Date> map = new HashMap<>();
		map.put(1234L, calendar.getTime());
		map.put(5678L, calendar.getTime());

		userUtility.updateLastSeenForUser(6789, map);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(2, users.size());
		for (GuildUser finalUser : users) {
			Assert.assertEquals(calendar.getTime(), finalUser.getLastSeen());
		}

	}

	@Test
	public void updateLastSeenForExistingUserViaGuildUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);
		user.setLastActive(date);

		userUtility.addNewUser(user);

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user.setLastSeen(calendar.getTime());

		userUtility.updateLastSeenForUser(user);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM");

		Assert.assertEquals(calendar.getTime(), user2.getLastSeen());
	}

	@Test
	public void updateLastActiveForExistingUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);
		user.setLastActive(date);

		userUtility.addNewUser(user);

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		userUtility.updateLastActiveForUser(calendar.getTime(), 1234, 6789);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		Assert.assertEquals(calendar.getTime(), user2.getLastActive());
	}

	@Test
	public void updateLastActiveForExistingUserViaMap() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);
		user.setLastActive(date);

		userUtility.addNewUser(user);

		user.setSnowflake(5678);
		userUtility.addNewUser(user);

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		Map<Long, Date> map = new HashMap<>();
		map.put(1234L, calendar.getTime());
		map.put(5678L, calendar.getTime());

		userUtility.updateLastActiveForUser(6789, map);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(2, users.size());
		for (GuildUser finalUser : users) {
			Assert.assertEquals(calendar.getTime(), finalUser.getLastActive());
		}

	}

	@Test
	public void updateLastActiveForExistingUserViaGuildUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);
		user.setLastActive(date);

		userUtility.addNewUser(user);

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		user.setLastActive(calendar.getTime());

		userUtility.updateLastActiveForUser(user);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		Assert.assertEquals(calendar.getTime(), user2.getLastActive());
	}

	@Test
	public void getJoinDateForExistingUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		Calendar calendar = Calendar.getInstance();
		calendar.setWeekDate(2010, 5, 2);

		userUtility.setJoinDateForUser(calendar.getTime(), 1234, 6789);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM");

		Assert.assertEquals(calendar.getTime(), user2.getJoinedServer());
	}

	@Test
	public void getLeftDateForExistingUser() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		Date now = new Date();
		userUtility.setLeftDateForUser(now, 1234, 6789);

		List<GuildUser> users = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from users").mapToBean(GuildUser.class).list());

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);

		Assert.assertEquals(now, user2.getLeftServerAsDate());

	}

	@Test
	public void getGuildUserSnowflakeAndGuild() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		List<GuildUser> users = userUtility.getGuildUser(1234, 6789);

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);
		Assert.assertEquals(user2.getSnowflake(), 1234);
		Assert.assertEquals(user2.getGuildSnowflake(), 6789);
		Assert.assertEquals(user2.getUsername(), "@User#1234");
		Assert.assertEquals(user2.getKnownName(), "User");
		Assert.assertEquals(user2.getDisplayName(), "User");

		Assert.assertNotNull(user2.getJoinedServer());
		Assert.assertNotNull(user2.getLastSeen());
		Assert.assertNull(user2.getLeftServerAsDate());
		Assert.assertNotNull(user2.getLastActive());
	}

	@Test
	public void getGuildUserByUsername() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownName");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		List<GuildUser> users = userUtility.getGuildUser("User");

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);
		Assert.assertEquals(user2.getSnowflake(), 1234);
		Assert.assertEquals(user2.getGuildSnowflake(), 6789);
		Assert.assertEquals(user2.getUsername(), "@User#1234");
		Assert.assertEquals(user2.getKnownName(), "KnownName");
		Assert.assertEquals(user2.getDisplayName(), "User");

		Assert.assertNotNull(user2.getJoinedServer());
		Assert.assertNotNull(user2.getLastSeen());
		Assert.assertNull(user2.getLeftServerAsDate());
		Assert.assertNotNull(user2.getLastActive());
	}

	@Test
	public void getGuildUserByKnownName() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		List<GuildUser> users = userUtility.getGuildUser("KnownNameTest");

		Assert.assertEquals(users.size(), 1);
		GuildUser user2 = users.get(0);
		Assert.assertEquals(user2.getSnowflake(), 1234);
		Assert.assertEquals(user2.getGuildSnowflake(), 6789);
		Assert.assertEquals(user2.getUsername(), "@User#1234");
		Assert.assertEquals(user2.getKnownName(), "KnownNameTest");
		Assert.assertEquals(user2.getDisplayName(), "User");

		Assert.assertNotNull(user2.getJoinedServer());
		Assert.assertNotNull(user2.getLastSeen());
		Assert.assertNull(user2.getLeftServerAsDate());
		Assert.assertNotNull(user2.getLastActive());
	}

	@Test
	public void getGuildByNameNoneExists() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		List<GuildUser> users = userUtility.getGuildUser("Junkname");

		Assert.assertEquals(users.size(), 0);

	}

	@Test
	public void getGuildByNameInServer() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user.setGuildSnowflake(5678);

		userUtility.addNewUser(user);

		List<GuildUser> users = userUtility.getGuildUserWithNameInGuild("User", 6789);

		Assert.assertEquals(users.size(), 1);

	}

	@Test
	public void getGuildByNameInServerWhereNoOneMatchesName() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user.setGuildSnowflake(5678);

		userUtility.addNewUser(user);

		List<GuildUser> users = userUtility.getGuildUserWithNameInGuild("HELLO", 6789);

		Assert.assertEquals(users.size(), 0);

	}

	@Test
	public void getGuildUsersByServer() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(5678);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(9012);
		user.setGuildSnowflake(1234);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		List<GuildUser> users = userUtility.getUsersForGuildId(6789);

		Assert.assertEquals(users.size(), 2);
	}

	@Test
	public void getGuildUsersByUserId() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(5678);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(9012);
		user.setGuildSnowflake(1234);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		List<GuildUser> users = userUtility.getUsersForUserId(1234);

		Assert.assertEquals(users.size(), 2);
	}

	@Test
	public void getCurrentGuildUsersByUserId() {
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(5678);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(9012);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(new Date());
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		List<GuildUser> users = userUtility.getUsersCurrentlyInGuildForGuildId(6789);

		Assert.assertEquals(users.size(), 2);
	}

	@Test
	public void searchUserActivityUserNotInDB()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		user.setLastActive(new Date());

		userUtility.addNewUser(user);

		List<String> searchUsers = new ArrayList<>();
		searchUsers.add("SearchUsr");
		List<String> results = userUtility.getUserActivityDatesForUsername(searchUsers, 6789);

		Assert.assertEquals(1, results.size());

		Assert.assertEquals("**SearchUsr**: No activity data found.", results.get(0));
	}

	@Test
	public void searchUserActivityUserInDB()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		//Use Epoch so we can easily find it :)
		user.setLastActive(Date.from(Instant.EPOCH));

		userUtility.addNewUser(user);

		List<String> searchUsers = new ArrayList<>();
		searchUsers.add("KnownNameTest");
		List<String> results = userUtility.getUserActivityDatesForUsername(searchUsers, 6789);

		Assert.assertEquals(1, results.size());

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		Assert.assertEquals("**KnownNameTest**: @User#1234: " + sdf.format(Date.from(Instant.EPOCH)), results.get(0));
	}

	@Test
	public void searchUserActivityUserInDBFind2UsersForSingleSearch()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		//Use Epoch so we can easily find it :)
		user.setLastActive(Date.from(Instant.EPOCH));

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(5678);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#5678");
		user.setKnownName("KnownName");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		//Use Epoch so we can easily find it :)
		user.setLastActive(Date.from(Instant.EPOCH));

		userUtility.addNewUser(user);

		List<String> searchUsers = new ArrayList<>();
		searchUsers.add("KnownName");
		List<String> results = userUtility.getUserActivityDatesForUsername(searchUsers, 6789);

		Assert.assertEquals(1, results.size());

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		Assert.assertEquals("**KnownName**: @User#1234: " + sdf.format(Date.from(Instant.EPOCH)) + ", @User#5678: " + sdf.format(Date.from(Instant.EPOCH)), results.get(0));
	}

	@Test
	public void searchUserActivityUserInDBFind2Users2Searches()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		//Use Epoch so we can easily find it :)
		user.setLastActive(Date.from(Instant.EPOCH));

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(5678);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#5678");
		user.setKnownName("NewName");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		//Use Epoch so we can easily find it :)
		user.setLastActive(Date.from(Instant.EPOCH));

		userUtility.addNewUser(user);

		List<String> searchUsers = new ArrayList<>();
		searchUsers.add("KnownNameTest");
		searchUsers.add("NewName");
		List<String> results = userUtility.getUserActivityDatesForUsername(searchUsers, 6789);

		Assert.assertEquals(2, results.size());

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		Assert.assertEquals("**KnownNameTest**: @User#1234: " + sdf.format(Date.from(Instant.EPOCH)), results.get(0));
		Assert.assertEquals("**NewName**: @User#5678: " + sdf.format(Date.from(Instant.EPOCH)), results.get(1));

	}

	@Test
	public void searchUserActivityUserInDBFind1Users2SearchesAndOneDoesntExist()
	{
		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		//Use Epoch so we can easily find it :)
		user.setLastActive(Date.from(Instant.EPOCH));

		userUtility.addNewUser(user);

		user = new GuildUser();
		user.setSnowflake(5678);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#5678");
		user.setKnownName("NewName");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());
		user.setLeftServer(Date.from(Instant.EPOCH));
		//Use Epoch so we can easily find it :)
		user.setLastActive(Date.from(Instant.EPOCH));

		userUtility.addNewUser(user);

		List<String> searchUsers = new ArrayList<>();
		searchUsers.add("KnownNameTest");
		searchUsers.add("SearchName");
		List<String> results = userUtility.getUserActivityDatesForUsername(searchUsers, 6789);

		Assert.assertEquals(2, results.size());

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		Assert.assertEquals("**KnownNameTest**: @User#1234: " + sdf.format(Date.from(Instant.EPOCH)), results.get(0));
		Assert.assertEquals("**SearchName**: No activity data found.", results.get(1));

	}

}
