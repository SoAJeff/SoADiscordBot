package com.soa.rs.discordbot.v3.jdbi;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildEntry;

import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GuildUtilityTest {

	private GuildUtility guildUtil = new GuildUtility();

	@BeforeClass
	public static void setUp() {
		Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
		JdbiWrapper.getInstance().setJdbi(jdbi);
	}

	@Before
	public void createDb() {
		guildUtil.createGuildsTable();
	}

	@After
	public void tearDown() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute("drop table guilds"));
	}

	@Test
	public void addEntryViaArgs() {
		guildUtil.addNewGuild(12345, "Guild1");

		Map<String, Object> map = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from guilds").mapToMap().one());

		Assert.assertEquals(map.get("snowflake"), Integer.toUnsignedLong(12345));
		Assert.assertEquals(map.get("guildname"), "Guild1");
	}

	@Test
	public void addEntryViaObject() {
		GuildEntry entry = new GuildEntry();
		entry.setSnowflake(6789);
		entry.setGuildName("Guild2");

		guildUtil.addNewGuild(entry);

		Map<String, Object> map = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from guilds").mapToMap().one());

		Assert.assertEquals(map.get("snowflake"), Integer.toUnsignedLong(6789));
		Assert.assertEquals(map.get("guildname"), "Guild2");
	}

	@Test
	public void testUpdateEntry() {
		guildUtil.addNewGuild(12345, "Guild1");

		guildUtil.updateGuildInfo(12345, "UpdatedGuildName");

		Map<String, Object> map = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from guilds").mapToMap().one());

		Assert.assertEquals(map.get("snowflake"), Integer.toUnsignedLong(12345));
		Assert.assertEquals(map.get("guildname"), "UpdatedGuildName");
	}

	@Test
	public void testUpdateEntryViaObject() {
		GuildEntry entry = new GuildEntry();
		entry.setSnowflake(6789);
		entry.setGuildName("Guild2");

		guildUtil.addNewGuild(entry);

		entry.setGuildName("AnotherGuild");

		guildUtil.updateGuildInfo(entry);

		Map<String, Object> map = JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from guilds").mapToMap().one());

		Assert.assertEquals(map.get("snowflake"), Integer.toUnsignedLong(6789));
		Assert.assertEquals(map.get("guildname"), "AnotherGuild");
	}

	@Test
	public void testGetReturnedList() {
		guildUtil.addNewGuild(12345, "Guild1");
		guildUtil.addNewGuild(6789, "Guild2");

		List<GuildEntry> list = guildUtil.getAllGuilds();

		Assert.assertEquals(list.size(), 2);

	}

	@Test
	public void testValidateMapToBean() {
		guildUtil.addNewGuild(12345, "Guild1");
		List<GuildEntry> list = guildUtil.getAllGuilds();

		Assert.assertEquals(list.get(0).getSnowflake(), 12345);
		Assert.assertEquals(list.get(0).getGuildName(), "Guild1");
	}

	@Test
	public void testGetGuildname() {
		guildUtil.addNewGuild(12345, "Guild1");
		Optional<String> name = guildUtil.getNameForGuild(12345);

		Assert.assertEquals(name.get(), "Guild1");
	}

	@Test
	public void testGetGuildnameWhenSnowflakeDoesntMapToGuild() {
		guildUtil.addNewGuild(12345, "Guild1");
		Optional<String> name = guildUtil.getNameForGuild(6789);

		Assert.assertFalse(name.isPresent());

	}

	@Test
	public void testGetGuildById(){
		guildUtil.addNewGuild(12345, "Guild1");
		guildUtil.addNewGuild(6789, "Guild2");

		List<GuildEntry> list = guildUtil.getGuildById(12345);

		Assert.assertEquals(list.get(0).getGuildName(), "Guild1");

	}

	@Test
	public void testGetGuildByName()
	{
		guildUtil.addNewGuild(12345, "Guild1");
		guildUtil.addNewGuild(6789, "Guild2");

		List<GuildEntry> list = guildUtil.getGuildByName("Guild1");

		Assert.assertEquals(list.get(0).getSnowflake(), 12345);

	}

}
