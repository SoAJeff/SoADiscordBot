package com.soa.rs.discordbot.v3.cfg;

import com.soa.rs.discordbot.v3.jaxb.AdminEvent;
import com.soa.rs.discordbot.v3.jaxb.DiscordConfiguration;
import com.soa.rs.discordbot.v3.jaxb.EventListingEvent;
import com.soa.rs.discordbot.v3.jaxb.RankList;
import com.soa.rs.discordbot.v3.jaxb.UserTrackingEvent;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
			SoaLogging.getLogger(this).error(e.getMessage());
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
	public void testUserTrackingNoRanksValidFile() {
		UserTrackingEvent event = new UserTrackingEvent();
		event.setEnabled(true);

		try
		{
			validator.validateUserTrackingEvent(event);
		}
		catch(InvalidBotConfigurationException ex)
		{
			Assert.assertEquals("Database connection is not configured, event cannot be initialized.", ex.getMessage());
		}

		Assert.assertTrue(
				DiscordCfgFactory.getConfig().getUserTrackingEvent().getCanUpdateQuery().getRole().size() == 1);
		Assert.assertEquals("Role 1",
				DiscordCfgFactory.getConfig().getUserTrackingEvent().getCanUpdateQuery().getRole().get(0));
	}

	@Test
	public void testUserTrackingValid() {
		UserTrackingEvent event = new UserTrackingEvent();
		boolean testResult = false;

		event.setEnabled(true);
		RankList list = new RankList();
		list.getRole().add("Role 1");
		event.setCanUpdateQuery(list);

		try {
			testResult = validator.validateUserTrackingEvent(event);
		} catch (InvalidBotConfigurationException e) {
			SoaLogging.getLogger(this).error(e.getMessage());
		}

		Assert.assertFalse(testResult);
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
