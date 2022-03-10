package com.soa.rs.discordbot.v3.commands;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

public class UserActivityCommandTest {

	@Test
	public void testParseNameUserActivity() {
		UserActivityCommand command = new UserActivityCommand();
		List<String> parsedNames = command
				.parseNames(".useractivity Applejuiceaj ~ 100xp" + System.lineSeparator() + "Bennybear ~ 200xp");

		Assert.assertEquals(2, parsedNames.size());
		for (String name : parsedNames) {
			Assert.assertTrue(name.length() <= 12);
		}

		Assert.assertEquals("Applejuiceaj", parsedNames.get(0));
		Assert.assertEquals("Bennybear", parsedNames.get(1));
	}

	@Test
	public void testParseNameUser_Activity() {
		UserActivityCommand command = new UserActivityCommand();
		List<String> parsedNames = command
				.parseNames(".user-activity Applejuiceaj ~ 100xp" + System.lineSeparator() + "Bennybear ~ 200xp");

		Assert.assertEquals(2, parsedNames.size());
		for (String name : parsedNames) {
			Assert.assertTrue(name.length() <= 12);
		}

		Assert.assertEquals("Applejuiceaj", parsedNames.get(0));
		Assert.assertEquals("Bennybear", parsedNames.get(1));
	}

	@Test
	public void testParseNameUserActivityBlankFirstLine() {
		UserActivityCommand command = new UserActivityCommand();
		List<String> parsedNames = command.parseNames(
				".useractivity" + "\n" + "Applejuiceaj ~ 100xp" + "\n"
						+ "Bennybear ~ 200xp");

		Assert.assertEquals(2, parsedNames.size());
		for (String name : parsedNames) {
			Assert.assertTrue(name.length() <= 12);
		}

		Assert.assertEquals("Applejuiceaj", parsedNames.get(0));
		Assert.assertEquals("Bennybear", parsedNames.get(1));
	}

	@Test
	public void testParseNameUserActivityNameLongerThan12Chars() {
		UserActivityCommand command = new UserActivityCommand();
		List<String> parsedNames = command.parseNames(
				".useractivity" + "\n" + "Applejuiceaj ~ 100xp" + "\n"
						+ "BennybearIsAGiantNoob ~ 200xp");

		Assert.assertEquals(1, parsedNames.size());
		for (String name : parsedNames) {
			Assert.assertTrue(name.length() <= 12);
		}

		Assert.assertEquals("Applejuiceaj", parsedNames.get(0));
	}

	@Test
	public void testParseNameUserActivityOneHasSymbolOneDoesnt() {
		UserActivityCommand command = new UserActivityCommand();
		List<String> parsedNames = command
				.parseNames(".useractivity Applejuiceaj" + "\n" + "Bennybear ~ 200xp");

		Assert.assertEquals(2, parsedNames.size());
		for (String name : parsedNames) {
			Assert.assertTrue(name.length() <= 12);
		}

		Assert.assertEquals("Applejuiceaj", parsedNames.get(0));
		Assert.assertEquals("Bennybear", parsedNames.get(1));
	}

	@Test
	public void testParseNameUserWithNoNames() {
		UserActivityCommand command = new UserActivityCommand();
		List<String> parsedNames = command.parseNames(".useractivity");

		Assert.assertEquals(0, parsedNames.size());
	}

	@Test
	public void testCreateOutputString() {
		UserActivityCommand command = new UserActivityCommand();
		List<String> databaseOutput = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		databaseOutput.add("**KnownNameTest**: @User#1234: " + sdf.format(Date.from(Instant.EPOCH)));
		databaseOutput.add("**SearchName**: No activity data found.");

		StringBuilder sb = new StringBuilder();
		sb.append("**Activity data for provided users:**");
		sb.append(System.lineSeparator());
		sb.append("-> **KnownNameTest**: @User#1234: " + sdf.format(Date.from(Instant.EPOCH)));
		sb.append(System.lineSeparator());
		sb.append("-> **SearchName**: No activity data found.");

		Assert.assertEquals(sb.toString(), command.createParsedOutput(databaseOutput).get(0));

	}

	@Test
	public void testCreateOutputStringMakes2Messages() {
		UserActivityCommand command = new UserActivityCommand();
		List<String> databaseOutput = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		for (int i = 0; i < 20; i++) {
			databaseOutput.add("**KnownNameTest**: @User#1234: " + sdf.format(Date.from(Instant.EPOCH)));
			databaseOutput.add("**SearchName**: No activity data found.");
		}

		Assert.assertEquals(2, command.createParsedOutput(databaseOutput).size());

	}
}
