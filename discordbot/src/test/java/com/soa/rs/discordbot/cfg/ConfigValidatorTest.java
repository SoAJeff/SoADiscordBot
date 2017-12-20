package com.soa.rs.discordbot.cfg;

import java.io.File;

import org.junit.*;

import com.soa.rs.discordbot.jaxb.*;
import com.soa.rs.discordbot.util.SoaLogging;

public class ConfigValidatorTest {

	private ConfigValidator validator = new DefaultConfigValidator();

	@Before
	public void setUpTest() {
		SoaLogging.initializeLogging();
		RankList list = new RankList();
		list.getRole().add("Role 1");
		DiscordCfgFactory.getConfig().setStaffRoles(list);
	}

	@Test
	public void testListParserFakeUrl() {
		EventListingEvent event = new EventListingEvent();
		boolean testResult = false;
		event.setChannel("shoutbox");
		event.setEnabled(true);
		event.setUrl("Something that isn't a URL");

		try {
			testResult = validator.validateListingEvent(event);
		} catch (InvalidBotConfigurationException e) {
			SoaLogging.getLogger().error(e.getMessage());
		}

		Assert.assertFalse(testResult);

	}

	@Test
	public void testListParserValid() throws InvalidBotConfigurationException {
		EventListingEvent event = new EventListingEvent();
		boolean testResult = false;
		event.setChannel("shoutbox");
		event.setEnabled(true);
		event.setUrl("https://forums.soa-rs.com/calendar/events.xml");

		testResult = validator.validateListingEvent(event);

		Assert.assertTrue(testResult);
	}

	@Test
	public void testMusicNoCanDisableRankCheck() throws InvalidBotConfigurationException {
		MusicPlayer event = new MusicPlayer();
		event.setEnabled(true);

		validator.validateMusicPlayer(event);

		Assert.assertTrue(
				DiscordCfgFactory.getConfig().getMusicPlayer().getCanDisableRankCheck().getRole().size() == 1);
		Assert.assertEquals("Role 1",
				DiscordCfgFactory.getConfig().getMusicPlayer().getCanDisableRankCheck().getRole().get(0));
	}

	@Test
	public void testMusicValidConfig() throws InvalidBotConfigurationException {
		MusicPlayer event = new MusicPlayer();
		event.setEnabled(true);
		RankList list = new RankList();
		list.getRole().add("Role 1");
		event.setAllowedRoles(list);
		event.setCanDisableRankCheck(list);

		boolean valid = validator.validateMusicPlayer(event);
		Assert.assertTrue(valid);
	}

	@Test
	public void testAdminNoRanks() throws InvalidBotConfigurationException {
		AdminEvent event = new AdminEvent();
		event.setEnabled(true);

		validator.validateAdminEvent(event);

		Assert.assertTrue(DiscordCfgFactory.getConfig().getAdminEvent().getAllowedRoles().getRole().size() == 1);
		Assert.assertEquals("Role 1", DiscordCfgFactory.getConfig().getAdminEvent().getAllowedRoles().getRole().get(0));
	}

	@Test
	public void testValidAdminConfig() throws InvalidBotConfigurationException {
		AdminEvent event = new AdminEvent();
		event.setEnabled(true);
		RankList list = new RankList();
		list.getRole().add("Role 1");
		event.setAllowedRoles(list);

		boolean valid = validator.validateAdminEvent(event);
		Assert.assertTrue(valid);
	}

	@Test
	public void testUserTrackingNoRanksValidFile() throws InvalidBotConfigurationException {
		UserTrackingEvent event = new UserTrackingEvent();
		event.setEnabled(true);
		event.setTrackingFile(System.getProperty("user.home") + File.separatorChar + "trackingfile.xml");

		validator.validateUserTrackingEvent(event);

		Assert.assertTrue(
				DiscordCfgFactory.getConfig().getUserTrackingEvent().getCanUpdateQuery().getRole().size() == 1);
		Assert.assertEquals("Role 1",
				DiscordCfgFactory.getConfig().getUserTrackingEvent().getCanUpdateQuery().getRole().get(0));
	}

	@Test
	public void testUserTrackingWithRanksInvalidFileIsDirectory() {
		UserTrackingEvent event = new UserTrackingEvent();
		boolean testResult = false;

		event.setEnabled(true);
		RankList list = new RankList();
		list.getRole().add("Role 1");
		event.setCanUpdateQuery(list);

		event.setTrackingFile(System.getProperty("user.home"));

		try {
			testResult = validator.validateUserTrackingEvent(event);
		} catch (InvalidBotConfigurationException e) {
			SoaLogging.getLogger().error(e.getMessage());
		}

		Assert.assertFalse(testResult);
	}

	@Test
	public void testUserTrackingWithRanksInvalidFile() {
		UserTrackingEvent event = new UserTrackingEvent();
		boolean testResult = false;

		event.setEnabled(true);
		RankList list = new RankList();
		list.getRole().add("Role 1");
		event.setCanUpdateQuery(list);

		if (System.getProperty("os.name").contains("Windows")) {
			event.setTrackingFile("M:" + File.separatorChar + "fakedir" + File.separatorChar + "file.xml");
		} else {
			event.setTrackingFile("/junk/file.xml");
		}

		try {
			testResult = validator.validateUserTrackingEvent(event);
		} catch (InvalidBotConfigurationException e) {
			SoaLogging.getLogger().error(e.getMessage());
		}

		Assert.assertFalse(testResult);
	}

	@Test
	public void testUserTrackingWithRanksNoPermission() {
		UserTrackingEvent event = new UserTrackingEvent();
		boolean testResult = false;

		event.setEnabled(true);
		RankList list = new RankList();
		list.getRole().add("Role 1");
		event.setCanUpdateQuery(list);

		if (System.getProperty("os.name").contains("Windows")) {
			// Unknown where Windows can't read or write from
			event.setTrackingFile("C:\\Program Files\\file.xml");
		} else {
			// As long as this isn't run as root, this spot should not be writable.
			event.setTrackingFile("/usr/file.xml");
		}

		try {
			testResult = validator.validateUserTrackingEvent(event);
		} catch (InvalidBotConfigurationException e) {
			SoaLogging.getLogger().error(e.getMessage());
		}

		if (System.getProperty("os.name").contains("Windows"))
			Assert.assertTrue(testResult);
		else
			Assert.assertFalse(testResult);

	}

	@Test
	public void testUserTrackingWithNoFile() {
		UserTrackingEvent event = new UserTrackingEvent();
		boolean testResult = false;

		event.setEnabled(true);
		RankList list = new RankList();
		list.getRole().add("Role 1");
		event.setCanUpdateQuery(list);

		try {
			testResult = validator.validateUserTrackingEvent(event);
		} catch (InvalidBotConfigurationException e) {
			SoaLogging.getLogger().error(e.getMessage());
		}

		Assert.assertFalse(testResult);
	}

	@Test
	public void testUserTrackingValid() {
		UserTrackingEvent event = new UserTrackingEvent();
		boolean testResult = false;

		event.setEnabled(true);
		RankList list = new RankList();
		list.getRole().add("Role 1");
		event.setCanUpdateQuery(list);

		event.setTrackingFile(System.getProperty("user.home") + File.separatorChar + "trackingfile.xml");

		try {
			testResult = validator.validateUserTrackingEvent(event);
		} catch (InvalidBotConfigurationException e) {
			SoaLogging.getLogger().error(e.getMessage());
		}

		Assert.assertTrue(testResult);
	}

	@Test
	public void testImproperObjectSchemaValidate() {
		Assert.assertFalse(validator.validateConformsToSchema(DiscordCfgFactory.getConfig()));
	}

	@Test
	public void testProperObjectSchemaValidate() {
		DiscordConfiguration cfg = new DiscordConfiguration();
		cfg.setDiscordToken("1234567890");
		RankList list = new RankList();
		list.getRole().add("Role 1");
		cfg.setStaffRoles(list);
		cfg.setDefaultGuildId(1234567890);
		cfg.setGuildAbbreviation("abbr");

		Assert.assertTrue(validator.validateConformsToSchema(cfg));
	}

}
