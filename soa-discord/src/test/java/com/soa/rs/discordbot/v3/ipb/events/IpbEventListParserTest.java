package com.soa.rs.discordbot.v3.ipb.events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jaxb.EventListingEvent;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class IpbEventListParserTest {

	private static final String DAILY_RECURRING = "FREQ=DAILY;INTERVAL=1;";

	public CalendarResults buildBaseCalendarResults() throws ParseException {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		CalendarResults results = new CalendarResults();
		results.setTotalPages(1);
		results.setTotalResults(2);
		Calendar calendar1 = new Calendar();
		calendar1.setId(1);
		calendar1.setName("Game Events");
		Calendar calendar2 = new Calendar();
		calendar2.setId(2);
		calendar2.setName("Forum Events");
		Member member1 = new Member();
		member1.setName("Applejuiceaj");

		Event event1 = new Event();
		event1.setCalendar(calendar1);
		event1.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));
		event1.setAuthor(member1);
		event1.setTitle("Game Event 1");
		event1.setUrl("https://discord.bot");
		event1.setRecurrence(null);

		Event event2 = new Event();
		event2.setCalendar(calendar2);
		event2.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));
		event2.setAuthor(member1);
		event2.setTitle("Forum Event 1");
		event2.setUrl("https://discord.bot");
		event2.setRecurrence(DAILY_RECURRING);

		List<Event> events = new ArrayList<>();
		events.add(event1);
		events.add(event2);
		results.setResults(events);

		return results;
	}

	@Test
	public void testDownloadAndSeparateEvents() throws ParseException {
		Map<Integer, List<Event>> eventsPerCategory = new TreeMap<>();
		Map<Integer, String> calendarType = new HashMap<>();
		IpbEventListParser parser = Mockito.spy(IpbEventListParser.class);
		CalendarResults baseResults = buildBaseCalendarResults();
		Mockito.doReturn(baseResults).when(parser).downloadCalendarResults(1);
		Assert.assertEquals(baseResults, parser.downloadAndSeparateEvents(eventsPerCategory, calendarType));
		Assert.assertEquals(2, eventsPerCategory.keySet().size());
		Assert.assertEquals(2, calendarType.keySet().size());
		Assert.assertEquals("Game Event 1", eventsPerCategory.get(1).get(0).getTitle());
	}

	@Test
	public void testDownloadAndSeparateEvents2Pages() throws ParseException {
		Map<Integer, List<Event>> eventsPerCategory = new TreeMap<>();
		Map<Integer, String> calendarType = new HashMap<>();
		IpbEventListParser parser = Mockito.spy(IpbEventListParser.class);
		CalendarResults baseResults = buildBaseCalendarResults();
		baseResults.setTotalPages(2);
		Mockito.doReturn(baseResults).when(parser).downloadCalendarResults(1);
		Mockito.doReturn(baseResults).when(parser).downloadCalendarResults(2);
		parser.downloadAndSeparateEvents(eventsPerCategory, calendarType);
		Mockito.verify(parser, Mockito.times(2)).downloadCalendarResults(Mockito.anyInt());
		Assert.assertEquals(2, eventsPerCategory.keySet().size());
		Assert.assertEquals(2, eventsPerCategory.get(1).size());
	}

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
	public void testBuildEventsStringWithNoEvents()
	{
		DiscordCfgFactory.getConfig().setGuildAbbreviation("SoA");
		EventListingEvent ele = new EventListingEvent();
		ele.getEventEndline().add("Line 1");
		ele.getEventEndline().add("Line 2");
		DiscordCfgFactory.getConfig().setEventListingEvent(ele);
		IpbEventListParser parser = new IpbEventListParser();
		CalendarResults results = new CalendarResults();
		results.setTotalResults(0);
		String eventsString = parser.buildEventsString(null, null, results);
		Assert.assertTrue(eventsString.contains("No events to show for today."));
	}

	@Test
	public void testGenerateFooterNoLines() {
		EventListingEvent ele = new EventListingEvent();
		DiscordCfgFactory.getConfig().setEventListingEvent(ele);
		IpbEventListParser parser = new IpbEventListParser();
		Assert.assertEquals("", parser.generateFooter());
	}

	@Test
	public void testIsOngoingAsDailyRecurring() throws ParseException {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Event event2 = new Event();
		event2.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));
		event2.setTitle("Forum Event 1");
		event2.setUrl("https://discord.bot");
		event2.setRecurrence(DAILY_RECURRING);
		IpbEventListParser parser = new IpbEventListParser();
		Date date = new Date();

		Assert.assertTrue(parser.isOngoingEvent(event2, date));
	}

	@Test
	public void testIsOngoingMultiDayEvent() throws ParseException {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Event event2 = new Event();
		event2.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));
		event2.setTitle("Forum Event 1");
		event2.setUrl("https://discord.bot");
		IpbEventListParser parser = new IpbEventListParser();
		Date date = new Date();

		Assert.assertTrue(parser.isOngoingEvent(event2, date));
	}

	@Test
	public void testIsWeeklyEvent() throws ParseException {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Event event2 = new Event();
		event2.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));
		event2.setTitle("Forum Event 1");
		event2.setUrl("https://discord.bot");
		event2.setRecurrence("FREQ=WEEKLY;INTERVAL=1");
		IpbEventListParser parser = new IpbEventListParser();
		Date date = new Date();

		Assert.assertFalse(parser.isOngoingEvent(event2, date));
	}

	@Test
	public void testIsFirstDayOfOngoingEvent() throws ParseException {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Event event2 = new Event();
		event2.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));
		event2.setTitle("Forum Event 1");
		event2.setUrl("https://discord.bot");
		event2.setRecurrence(DAILY_RECURRING);
		IpbEventListParser parser = new IpbEventListParser();

		Assert.assertFalse(parser.isOngoingEvent(event2, inputFormat.parse("2019-11-05T04:00:00Z")));
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
