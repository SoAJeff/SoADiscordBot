package com.soa.rs.discordbot.v3.ipb.events;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import com.soa.rs.discordbot.v3.util.DateAnalyzer;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;

public class CalendarEvent implements Comparable<CalendarEvent> {

	private Date date;
	private String eventInfo;

	private static final String DAILY_RECURRING = "FREQ=DAILY;INTERVAL=1;";

	CalendarEvent() {

	}

	public CalendarEvent(Event event, Date today) {
		StringBuilder sb = new StringBuilder();
		sb.append("Event Title: " + event.getTitle());
		if (event.getRecurrence() != null && !event.getRecurrence().startsWith(DAILY_RECURRING)) {
			this.date = getDateForRecurringEvent(event, today);
		} else {
			this.date = event.getStart();
		}
		sb.append("\nEvent Date: " + DateAnalyzer.showMultipleTimezonesForEvent(this.date));
		sb.append("\nPosted by: " + event.getAuthor().getName());
		sb.append("\nFor details, visit: <" + event.getUrl() + ">");
		this.eventInfo = sb.toString();
	}

	public Date getDate() {
		return date;
	}

	public String getEventInfo() {
		return eventInfo;
	}

	Date getDateForRecurringEvent(Event event, Date today) {
		LocalDate ld1 = LocalDateTime.ofInstant(today.toInstant(), TimeZone.getTimeZone("UTC").toZoneId())
				.toLocalDate();
		RecurrenceRule rule;
		try {
			SoaLogging.getLogger(this).debug("Recurrence Rule for event ["+event.getTitle() + "]: " + event.getRecurrence());
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

	@Override
	public int compareTo(CalendarEvent o) {
		return this.date.compareTo(o.getDate());
	}
}
