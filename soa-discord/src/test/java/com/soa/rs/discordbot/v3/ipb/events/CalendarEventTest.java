package com.soa.rs.discordbot.v3.ipb.events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

public class CalendarEventTest {

	@Test
	public void testRecursiveEvent() throws ParseException {
		CalendarEvent calendarEvent = new CalendarEvent();
		Event event = new Event();
		event.setRecurrence("FREQ=WEEKLY;INTERVAL=1");
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		event.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));

		Date today = inputFormat.parse("2020-05-19T00:00:00Z");

		Assert.assertEquals(inputFormat.parse("2020-05-19T04:00:00Z"),
				calendarEvent.getDateForRecurringEvent(event, today));
	}

	@Test
	public void testRecursiveEventWithGarbageRecurrence() throws ParseException {
		CalendarEvent calendarEvent = new CalendarEvent();
		Event event = new Event();
		event.setRecurrence("this is garbage data");
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		event.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));

		Date today = inputFormat.parse("2020-05-19T00:00:00Z");

		Assert.assertEquals(inputFormat.parse("2020-05-19T00:00:00Z"),
				calendarEvent.getDateForRecurringEvent(event, today));
	}

	@Test
	public void testRecursiveEventNotInDateRange() throws ParseException {
		CalendarEvent calendarEvent = new CalendarEvent();
		Event event = new Event();
		event.setRecurrence("FREQ=WEEKLY;INTERVAL=1");
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		event.setStart(inputFormat.parse("2019-11-04T04:00:00Z"));

		Date today = inputFormat.parse("2020-05-19T00:00:00Z");

		Assert.assertEquals(inputFormat.parse("2020-05-19T00:00:00Z"),
				calendarEvent.getDateForRecurringEvent(event, today));
	}

	@Test
	public void testCanCreateEventString() throws ParseException {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Event event2 = new Event();
		event2.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));
		event2.setTitle("Forum Event 1");
		event2.setUrl("https://discord.bot");
		Member member1 = new Member();
		member1.setName("Applejuiceaj");
		event2.setAuthor(member1);
		Date date = new Date();
		CalendarEvent event = new CalendarEvent(event2, date);

		Assert.assertEquals(
				"Event Title: Forum Event 1\nEvent Date: 04-Nov 23:00 EST | 05-Nov 04:00 UTC (Game time) | 05-Nov 15:00 AEDT\nPosted by: Applejuiceaj\nFor details, visit: <https://discord.bot>",
				event.getEventInfo());
	}

	@Test
	public void testSortEvents() throws ParseException {
		List<CalendarEvent> calendarEvents = new ArrayList<>();
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Event event2 = new Event();
		event2.setStart(inputFormat.parse("2019-11-05T04:00:00Z"));
		event2.setTitle("Forum Event 1");
		event2.setUrl("https://discord.bot");
		Member member1 = new Member();
		member1.setName("Applejuiceaj");
		event2.setAuthor(member1);
		Date date = new Date();
		calendarEvents.add(new CalendarEvent(event2, date));

		inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Event event3 = new Event();
		event3.setStart(inputFormat.parse("2018-11-05T04:00:00Z"));
		event3.setTitle("Forum Event 1");
		event3.setUrl("https://discord.bot");
		event3.setAuthor(member1);
		calendarEvents.add(new CalendarEvent(event3, date));

		Collections.sort(calendarEvents);

		Assert.assertTrue(inputFormat.format(calendarEvents.get(0).getDate()).startsWith("2018"));
		Assert.assertTrue(inputFormat.format(calendarEvents.get(1).getDate()).startsWith("2019"));

	}
}
