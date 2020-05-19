package com.soa.rs.discordbot.v3.ipb.events;

import java.text.SimpleDateFormat;
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

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;

public class IpbEventListParser {

	private static final String DAILY_RECURRING = "FREQ=DAILY;INTERVAL=1;";

	public String generateListing() {
		CalendarResults results = downloadCalendarResults();
		Map<Integer, List<Event>> eventsPerCategory = new TreeMap<>();
		Map<Integer, String> calendarType = new HashMap<>();
		separateEvents(eventsPerCategory, calendarType, results);
		if (results.getTotalPages() > 1) {
			//TODO: Handle getting another page...
		}
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

	void separateEvents(Map<Integer, List<Event>> eventsPerCategory, Map<Integer, String> calendarType,
			CalendarResults results) {
		for (Event event : results.getResults()) {
			int calendarId = event.getCalendar().getId();
			if (!eventsPerCategory.containsKey(calendarId)) {
				eventsPerCategory.put(calendarId, new ArrayList<>());
				calendarType.put(calendarId, event.getCalendar().getName());
			}
			eventsPerCategory.get(calendarId).add(event);
		}
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
				//TODO: This needs to understand weekly recurring events instead of reporting dates in the past.
				sb.append("Event Title: " + event.getTitle());
				sb.append("\nEvent Date: " + DateAnalyzer.showMultipleTimezonesForEvent(event.getStart()));
				sb.append("\nPosted by: " + event.getAuthor().getName());
				sb.append("\nFor details, visit: <" + event.getUrl() + ">");
				sb.append("\n\n");
			}
		}
		return sb.toString();
	}

	boolean isOngoingEvent(Event event, Date today) {
		java.util.Calendar cal1 = java.util.Calendar.getInstance();
		java.util.Calendar cal2 = Calendar.getInstance();
		cal1.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal2.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal2.setTime(today);
		cal1.setTime(event.getStart());
		if (event.getRecurrence() != null && event.getRecurrence().startsWith(DAILY_RECURRING) && !DateAnalyzer
				.isSameDay(cal1, cal2)) {
			return true;
		}
		if (DateAnalyzer.isBeforeDay(cal1, cal2) && event.getRecurrence() == null) {
			return true;
		}
		if (DateAnalyzer.isBeforeDay(cal1, cal2) && event.getEnd() != null && !event.getRecurrence()
				.startsWith(DAILY_RECURRING)) {
			return true;
		}
		return false;
	}

	private CalendarResults downloadCalendarResults() {
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
				.queryParam("sortBy", "start");
		return target.request(MediaType.APPLICATION_JSON_TYPE).get(CalendarResults.class);
	}
}
