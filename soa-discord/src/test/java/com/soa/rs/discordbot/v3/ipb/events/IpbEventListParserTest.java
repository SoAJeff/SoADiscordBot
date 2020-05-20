package com.soa.rs.discordbot.v3.ipb.events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jaxb.EventListingEvent;

import org.junit.Assert;
import org.junit.Test;

public class IpbEventListParserTest {

	@Test
	public void testGenerateHeader() {
		DiscordCfgFactory.getConfig().setGuildAbbreviation("SoA");
		IpbEventListParser parser = new IpbEventListParser();
		Assert.assertEquals("**Today's SoA Events**\n", parser.generateHeader());
	}

	@Test
	public void testGenerateFooter() {
		EventListingEvent ele = new EventListingEvent();
		ele.getEventEndline().add("Line 1");
		ele.getEventEndline().add("Line 2");
		DiscordCfgFactory.getConfig().setEventListingEvent(ele);
		IpbEventListParser parser = new IpbEventListParser();
		Assert.assertEquals("Line 1\nLine 2", parser.generateFooter());
	}

	@Test
	public void testGenerateFooterNoLines() {
		EventListingEvent ele = new EventListingEvent();
		DiscordCfgFactory.getConfig().setEventListingEvent(ele);
		IpbEventListParser parser = new IpbEventListParser();
		Assert.assertEquals("", parser.generateFooter());
	}

	@Test
	public void testRecursiveEvent() throws ParseException {
		IpbEventListParser parser = new IpbEventListParser();
		Event event = new Event();
		event.setRecurrence("FREQ=WEEKLY;INTERVAL=1");
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		event.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));

		Date today = inputFormat.parse("2020-05-19T00:00:00Z");

		Assert.assertEquals(inputFormat.parse("2020-05-19T04:00:00Z"), parser.getDateForRecurringEvent(event, today));
	}

	@Test
	public void testRecursiveEventWithGarbageRecurrence() throws ParseException {
		IpbEventListParser parser = new IpbEventListParser();
		Event event = new Event();
		event.setRecurrence("this is garbage data");
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		event.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));

		Date today = inputFormat.parse("2020-05-19T00:00:00Z");

		Assert.assertEquals(inputFormat.parse("2020-05-19T00:00:00Z"), parser.getDateForRecurringEvent(event, today));
	}

	@Test
	public void testRecursiveEventNotInDateRange() throws ParseException {
		IpbEventListParser parser = new IpbEventListParser();
		Event event = new Event();
		event.setRecurrence("FREQ=WEEKLY;INTERVAL=1");
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		event.setStart(inputFormat.parse("2019-11-04T04:00:00Z"));

		Date today = inputFormat.parse("2020-05-19T00:00:00Z");

		Assert.assertEquals(inputFormat.parse("2020-05-19T00:00:00Z"), parser.getDateForRecurringEvent(event, today));
	}
}
