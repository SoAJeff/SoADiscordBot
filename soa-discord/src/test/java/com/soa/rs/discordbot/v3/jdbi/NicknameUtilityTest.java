package com.soa.rs.discordbot.v3.jdbi;

import java.util.List;

import com.soa.rs.discordbot.v3.jdbi.entities.Nickname;

import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NicknameUtilityTest {

	NicknameUtility util = new NicknameUtility();

	@BeforeClass
	public static void setUp() {
		Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
		JdbiWrapper.getInstance().setJdbi(jdbi);
	}

	@Before
	public void createDb() {
		util.createNicknamesTable();
	}

	@After
	public void tearDown() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute("drop table nicknames"));
	}

	@Test
	public void testInsertViaFields() {
		util.addNickname(1234, 6789, "Test nickname");
		util.addNickname(0321, 6789, "Test Nickname 2");

		List<Nickname> nicknames = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from nicknames").mapToBean(Nickname.class).list());

		Assert.assertEquals(2, nicknames.size());
		Assert.assertEquals(1234, nicknames.get(0).getUserSnowflake());
		Assert.assertEquals(0321, nicknames.get(1).getUserSnowflake());
		Assert.assertEquals(6789, nicknames.get(0).getGuildSnowflake());
		Assert.assertEquals(6789, nicknames.get(1).getGuildSnowflake());
		Assert.assertEquals("Test nickname", nicknames.get(0).getDisplayName());
		Assert.assertEquals("Test Nickname 2", nicknames.get(1).getDisplayName());

	}

	@Test
	public void testInsertViaBean() {
		Nickname nickname = new Nickname();
		nickname.setUserSnowflake(1234);
		nickname.setGuildSnowflake(6789);
		nickname.setDisplayName("Test nickname");
		util.addNickname(nickname);

		nickname.setUserSnowflake(0321);
		nickname.setGuildSnowflake(6789);
		nickname.setDisplayName("Test Nickname 2");
		util.addNickname(nickname);

		List<Nickname> nicknames = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from nicknames").mapToBean(Nickname.class).list());

		Assert.assertEquals(2, nicknames.size());
		Assert.assertEquals(1234, nicknames.get(0).getUserSnowflake());
		Assert.assertEquals(0321, nicknames.get(1).getUserSnowflake());
		Assert.assertEquals(6789, nicknames.get(0).getGuildSnowflake());
		Assert.assertEquals(6789, nicknames.get(1).getGuildSnowflake());
		Assert.assertEquals("Test nickname", nicknames.get(0).getDisplayName());
		Assert.assertEquals("Test Nickname 2", nicknames.get(1).getDisplayName());

	}

	@Test
	public void testGetNicknamesForUser() {
		util.addNickname(1234, 6789, "Test nickname");
		util.addNickname(0321, 6789, "Test Nickname 2");

		List<String> nicknames = util.getNicknamesForUser(1234, 6789);

		Assert.assertEquals(1, nicknames.size());
		Assert.assertEquals("Test nickname", nicknames.get(0));
	}

	@Test
	public void testGetNicknamesForUserMultipleNicknames() {
		util.addNickname(1234, 6789, "Test nickname");
		util.addNickname(1234, 6789, "Test Nickname 2");

		List<String> nicknames = util.getNicknamesForUser(1234, 6789);

		Assert.assertEquals(2, nicknames.size());
		Assert.assertEquals("Test nickname", nicknames.get(0));
		Assert.assertEquals("Test Nickname 2", nicknames.get(1));
	}

	@Test
	public void testGetNicknamesForUserNonexistantUser() {
		util.addNickname(1234, 6789, "Test nickname");
		util.addNickname(1234, 6789, "Test Nickname 2");

		List<String> nicknames = util.getNicknamesForUser(0321, 6789);

		Assert.assertEquals(0, nicknames.size());
	}

	@Test
	public void testGetNicknameForUserWithName() {
		util.addNickname(1234, 6789, "Test nickname");
		util.addNickname(1234, 6789, "Random nick");

		List<Nickname> nicknames = util.getNicknameForUserWithName("nickname");

		Assert.assertEquals(1, nicknames.size());
		Assert.assertEquals("Test nickname", nicknames.get(0).getDisplayName());
	}

	@Test
	public void testGetNicknameForUserWithNameMultipleMatches()
	{
		util.addNickname(1234, 6789, "Test nickname");
		util.addNickname(1234, 6789, "Random nIck");

		List<Nickname> nicknames = util.getNicknameForUserWithName("nick");

		Assert.assertEquals(2, nicknames.size());
		Assert.assertEquals("Test nickname", nicknames.get(0).getDisplayName());
		Assert.assertEquals("Random nIck", nicknames.get(1).getDisplayName());
	}

	@Test
	public void testGetNicknameForUserWithNameNoUsersMatch() {
		util.addNickname(1234, 6789, "Test nickname");
		util.addNickname(1234, 6789, "Random nick");

		List<Nickname> nicknames = util.getNicknameForUserWithName("junk");

		Assert.assertEquals(0, nicknames.size());
	}

	@Test
	public void testGetNicknameForUserWithNameAndGuildId() {
		util.addNickname(1234, 6789, "Test nickname");
		util.addNickname(1234, 6789, "Random nick");

		List<Nickname> nicknames = util.getNicknameForUserWithName("nickname", 6789);

		Assert.assertEquals(1, nicknames.size());
		Assert.assertEquals("Test nickname", nicknames.get(0).getDisplayName());
	}
}
