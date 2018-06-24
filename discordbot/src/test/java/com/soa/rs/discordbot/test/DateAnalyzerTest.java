package com.soa.rs.discordbot.test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.soa.rs.discordbot.util.DateAnalyzer;

public class DateAnalyzerTest {

	Calendar cal1, cal2, cal3, cal4, cal5;

	@Before
	public void setUp() {
		cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal3 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal4 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal5 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		cal2.add(Calendar.DATE, 1);
		cal3.add(Calendar.DATE, 2);
		cal4.add(Calendar.DATE, 3);
	}

	@Test
	public void isBeforeDayTest() {
		Assert.assertTrue(DateAnalyzer.isBeforeDay(cal1, cal2));
		Assert.assertTrue(DateAnalyzer.isBeforeDay(cal1, cal4));
		Assert.assertFalse(DateAnalyzer.isBeforeDay(cal3, cal2));
	}

	@Test
	public void isSameDayTest() {
		Assert.assertTrue(DateAnalyzer.isSameDay(cal1, cal5));
		Assert.assertFalse(DateAnalyzer.isSameDay(cal1, cal2));
		Assert.assertFalse(DateAnalyzer.isSameDay(cal1, cal3));
		Assert.assertFalse(DateAnalyzer.isSameDay(cal1, cal4));
	}

	@Test
	public void daysBetweenTest() {
		Assert.assertEquals(DateAnalyzer.daysBetween(cal1.getTime(), cal2.getTime()), 1);
		Assert.assertEquals(DateAnalyzer.daysBetween(cal1.getTime(), cal3.getTime()), 2);
		Assert.assertEquals(DateAnalyzer.daysBetween(cal1.getTime(), cal4.getTime()), 3);
		Assert.assertEquals(DateAnalyzer.daysBetween(cal1.getTime(), cal5.getTime()), 0);

	}

	@Test
	public void testMultipleTimeZones() {
		//For use during test, this sets the time to 23 June 2018, 7:43 PM EDT
		Date date = new Date(1529797404281L);

		Assert.assertEquals("23-Jun 19:43 EDT | 23-Jun 23:43 UTC (Game time) | 24-Jun 09:43 AEST",
				DateAnalyzer.showMultipleTimezonesForEvent(date));
	}

}
