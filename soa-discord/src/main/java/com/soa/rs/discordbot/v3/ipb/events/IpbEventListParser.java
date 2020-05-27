package com.soa.rs.discordbot.v3.ipb.events;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.DateAnalyzer;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;

public class IpbEventListParser {

	private static final String DAILY_RECURRING = "FREQ=DAILY;INTERVAL=1;";

	public String generateListing() {
		Map<Integer, List<Event>> eventsPerCategory = new TreeMap<>();
		Map<Integer, String> calendarType = new HashMap<>();
		CalendarResults results = downloadAndSeparateEvents(eventsPerCategory, calendarType);
		return buildEventsString(eventsPerCategory, calendarType, results);
	}

	CalendarResults downloadAndSeparateEvents(Map<Integer, List<Event>> eventsPerCategory,
			Map<Integer, String> calendarType) {
		CalendarResults results = downloadCalendarResults(1);
		separateEvents(eventsPerCategory, calendarType, results);
		if (results.getTotalPages() > 1) {
			for (int i = 2; i <= results.getTotalPages(); i++) {
				SoaLogging.getLogger(this).debug("Multiple pages of results, downloading page " + i);
				separateEvents(eventsPerCategory, calendarType, downloadCalendarResults(i));
			}
		}
		//Return first set of results, just need something to verify total results > 0
		return results;
	}

	void separateEvents(Map<Integer, List<Event>> eventsPerCategory, Map<Integer, String> calendarType,
			CalendarResults results) {
		for (Event event : results.getResults()) {
			SoaLogging.getLogger(this)
					.debug("Event: " + event.getTitle() + " found in calendar " + event.getCalendar().getName());
			int calendarId = event.getCalendar().getId();
			if (!eventsPerCategory.containsKey(calendarId)) {
				eventsPerCategory.put(calendarId, new ArrayList<>());
				calendarType.put(calendarId, event.getCalendar().getName());
			}
			eventsPerCategory.get(calendarId).add(event);
		}
	}

	String buildEventsString(Map<Integer, List<Event>> eventsPerCategory, Map<Integer, String> calendarType,
			CalendarResults results) {
		StringBuilder sb = new StringBuilder();
		sb.append(generateHeader());
		if (results.getTotalResults() == 0) {
			sb.append("No events to show for today.\n\n");
		} else {
			for (Map.Entry<Integer, List<Event>> entrySet : eventsPerCategory.entrySet()) {
				sb.append("**__");
				sb.append(calendarType.get(entrySet.getKey()));
				sb.append("__**");
				sb.append("\n");
				sb.append(handleEvents(entrySet.getValue(), new Date()));
			}
		}
		sb.append(generateFooter());
		return sb.toString();
	}

	String generateHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("**Today's ");
		if (DiscordCfgFactory.getConfig().getGuildAbbreviation() != null) {
			sb.append(DiscordCfgFactory.getConfig().getGuildAbbreviation());
		}
		sb.append(" Events**\n");
		return sb.toString();
	}

	String generateFooter() {
		StringBuilder sb = new StringBuilder();
		if (DiscordCfgFactory.getConfig().getEventListingEvent().getEventEndline() != null && !DiscordCfgFactory
				.getConfig().getEventListingEvent().getEventEndline().isEmpty()) {
			Iterator<String> lineIter = DiscordCfgFactory.getConfig().getEventListingEvent().getEventEndline()
					.iterator();
			while (lineIter.hasNext()) {
				sb.append(lineIter.next());
				if (lineIter.hasNext())
					sb.append("\n");
			}
		}
		return sb.toString();
	}

	String handleEvents(List<Event> events, Date today) {
		StringBuilder sb = new StringBuilder();
		for (Event event : events) {
			//Check if ongoing
			boolean isOngoing = isOngoingEvent(event, today);
			//Check if skill competition
			if (isOngoing && event.getTitle().toLowerCase().contains("comp")) {
				sb.append("The following competition is ongoing!\n");
				sb.append(event.getTitle());
				sb.append("\nPosted by: " + event.getAuthor().getName());
				sb.append("\nFor details, visit: <" + event.getUrl() + ">");
				sb.append("\n\n");
			} else if (isOngoing) {
				sb.append("The following event is ongoing!\n");
				sb.append(event.getTitle());
				sb.append("\nPosted by: " + event.getAuthor().getName());
				sb.append("\nFor details, visit: <" + event.getUrl() + ">");
				sb.append("\n\n");
			} else {
				sb.append("Event Title: " + event.getTitle());
				if (event.getRecurrence() != null && !event.getRecurrence().startsWith(DAILY_RECURRING)) {
					Date recurringDate = getDateForRecurringEvent(event, today);
					sb.append("\nEvent Date: " + DateAnalyzer.showMultipleTimezonesForEvent(recurringDate));
				} else {
					sb.append("\nEvent Date: " + DateAnalyzer.showMultipleTimezonesForEvent(event.getStart()));
				}
				sb.append("\nPosted by: " + event.getAuthor().getName());
				sb.append("\nFor details, visit: <" + event.getUrl() + ">");
				sb.append("\n\n");
			}
		}
		return sb.toString();
	}

	Date getDateForRecurringEvent(Event event, Date today) {
		LocalDate ld1 = LocalDateTime.ofInstant(today.toInstant(), TimeZone.getTimeZone("UTC").toZoneId())
				.toLocalDate();
		RecurrenceRule rule;
		try {
			rule = new RecurrenceRule(event.getRecurrence());
		} catch (InvalidRecurrenceRuleException e) {
			SoaLogging.getLogger(this).error("Failed to parse recurrence rule, just returning midnight tonight.", e);
			return new Date(ld1.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
		}
		DateTime startDate = new DateTime(event.getStart().toInstant().toEpochMilli());
		RecurrenceRuleIterator it = rule.iterator(startDate);
		int maxInstances = 300; //Likely overkill, but covers our bases

		while (it.hasNext() && (!rule.isInfinite() || maxInstances-- > 0)) {
			DateTime nextInstance = it.nextDateTime();
			LocalDate ld2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(nextInstance.getTimestamp()),
					TimeZone.getTimeZone("UTC").toZoneId()).toLocalDate();

			if (ld1.equals(ld2)) {
				SoaLogging.getLogger(this)
						.debug("Date for recurring event " + event.getTitle() + " found to be " + ld2.toString());
				return new Date(nextInstance.getTimestamp());
			}
		}
		//Unable to determine, just return today
		return new Date(ld1.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
	}

	boolean isOngoingEvent(Event event, Date today) {
		java.util.Calendar cal1 = java.util.Calendar.getInstance();
		java.util.Calendar cal2 = Calendar.getInstance();
		cal1.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal2.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal2.setTime(today);
		cal1.setTime(event.getStart());
		//Daily recurring event
		if (event.getRecurrence() != null && event.getRecurrence().startsWith(DAILY_RECURRING) && !DateAnalyzer
				.isSameDay(cal1, cal2)) {
			SoaLogging.getLogger(this).debug("Event is daily recurring event: " + event.getTitle());
			return true;
		}
		//Multi-day event
		if (DateAnalyzer.isBeforeDay(cal1, cal2) && event.getRecurrence() == null) {
			SoaLogging.getLogger(this).debug("Event is multi-day event: " + event.getTitle());
			return true;
		}
		return false;
	}

	CalendarResults downloadCalendarResults(int page) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Client client = ClientBuilder.newClient();
		HttpAuthenticationFeature feature = HttpAuthenticationFeature
				.basic(DiscordCfgFactory.getConfig().getEventListingEvent().getApiKey(), "");
		client.register(feature);
		client.register(JacksonJsonProvider.class);
		WebTarget target = client.target(DiscordCfgFactory.getConfig().getEventListingEvent().getUrl())
				.queryParam("rangeStart", sdf.format(date)).queryParam("rangeEnd", sdf.format(date))
				.queryParam("sortBy", "start").queryParam("page", page);
		return target.request(MediaType.APPLICATION_JSON_TYPE).get(CalendarResults.class);
	}
}
