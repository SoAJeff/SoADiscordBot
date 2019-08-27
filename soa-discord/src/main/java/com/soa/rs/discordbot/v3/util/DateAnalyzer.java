package com.soa.rs.discordbot.v3.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * The DateAnalyzer handles simple tasks such as determining if a date is on the
 * same day, previous day, or the number of days in between two dates.
 */
public class DateAnalyzer {

	/**
	 * Checks if <tt>cal1</tt> is before <tt>cal2</tt>
	 *
	 * @param cal1 Calendar containing the earlier date
	 * @param cal2 Calendar containing the later date
	 * @return true if <tt>cal1</tt>is before <tt>cal2</tt>, false if otherwise
	 */
	public static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA))
			return true;
		if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA))
			return false;
		if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR))
			return true;
		if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR))
			return false;
		return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Checks if <tt>cal1</tt> is the same date as <tt>cal2</tt>
	 *
	 * @param cal1 Calendar 1
	 * @param cal2 Calendar 2
	 * @return true if the dates are the same, false if otherwise
	 */
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}

	/**
	 * Gets the days in between two dates
	 *
	 * @param d1 Date object containing the earlier date
	 * @param d2 Date Object containing the later date
	 * @return the number of days in between the dates
	 */
	public static int daysBetween(Date d1, Date d2) {
		return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	}

	/**
	 * Shows a date in multiple time zones.  Currently will show times in Eastern, UTC, and Australian Eastern
	 *
	 * @param date Date object to convert into timezones
	 * @return A string containing the date in the timezones.
	 */
	public static String showMultipleTimezonesForEvent(Date date) {
		StringBuilder sb = new StringBuilder();

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm z");
		sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));

		sb.append(sdf.format(date));
		sb.append(" | ");

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		sb.append(sdf.format(date));
		sb.append(" (Game time)");
		sb.append(" | ");

		sdf.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));

		sb.append(sdf.format(date));

		return sb.toString();
	}

	/**
	 * Gets the number of seconds until an interval of "10 minutes" is hit
	 *
	 * @returnt The number of seconds
	 */
	public static long calculateMinutesUntil10() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		calendar.set(Calendar.SECOND, 0);
		if (calendar.get(Calendar.MINUTE % 10) == 0) {
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 10);
		} else {
			int minutesToAdd = 10 - calendar.get(Calendar.MINUTE) % 10;
			calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE) + minutesToAdd));
		}
		return (calendar.getTime().getTime() - new Date().getTime()) / 1000;

	}

	/**
	 * Computes the number of seconds between now and the desired specified time.
	 * Note that this uses UTC to compute the time.
	 *
	 * @param targetHour Targeted hour
	 * @param targetMin  Targeted minute
	 * @param targetSec  Targeted second
	 * @return Number of seconds between the current time and the desired time.
	 */
	public static long computeNextDelay(int targetHour, int targetMin, int targetSec) {
		ZoneId currentZone = ZoneId.of("UTC");
		LocalDateTime localNow = LocalDateTime.now(currentZone);
		ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
		ZonedDateTime zonedNextTarget = zonedNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
		if (zonedNow.compareTo(zonedNextTarget) > 0)
			zonedNextTarget = zonedNextTarget.plusDays(1);

		Duration duration = Duration.between(zonedNow, zonedNextTarget);
		return duration.getSeconds();
	}

	public static long calculateMinutesUntil30() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.set(Calendar.SECOND, 0);
		if (cal.get(Calendar.MINUTE) >= 0 && cal.get(Calendar.MINUTE) < 30)
			cal.set(Calendar.MINUTE, 30);
		else {
			cal.add(Calendar.HOUR, 1);
			cal.set(Calendar.MINUTE, 0);
		}
		return (cal.getTime().getTime() - new Date().getTime()) / 1000;

	}

}
