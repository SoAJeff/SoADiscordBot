package com.soa.rs.discordbot.v3.jdbi;

import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SettingsUtilityTest {

	private SettingsUtility settingsUtility = new SettingsUtility();

	@BeforeClass
	public static void setUp() {
		Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
		JdbiWrapper.getInstance().setJdbi(jdbi);
	}

	@Before
	public void createDb() {
		settingsUtility.createSettingsTable();
	}

	@After
	public void tearDown() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute("drop table settings"));
	}

	@Test
	public void testGetValueForKey() {
		JdbiWrapper.getInstance().getJdbi().useHandle(
				handle -> handle.createUpdate("insert into settings values ('settingkey1', 'settingvalue1')")
						.execute());

		String value = settingsUtility.getValueForKey("settingkey1");
		Assert.assertEquals("settingvalue1", value);
	}

	@Test
	public void testUpdateValueForKey()
	{
		JdbiWrapper.getInstance().getJdbi().useHandle(
				handle -> handle.createUpdate("insert into settings values ('settingkey1', 'settingvalue1')")
						.execute());

		settingsUtility.updateValueForKey("settingkey1", "settingvalue2");

		String value = settingsUtility.getValueForKey("settingkey1");
		Assert.assertEquals("settingvalue2", value);
	}

}
