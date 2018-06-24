package com.soa.rs.discordbot.bot.events;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.soa.rs.discordbot.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.util.DateAnalyzer;
import com.soa.rs.discordbot.util.SoaLogging;

/**
 * The SoaEventListParser retrieves the RSS feed provided by the SoA forums
 * calendar and parses through the entries to present the current day's upcoming
 * events. It will also present the currently running weekly skill competition
 * if one is noted in the feed and any currently ongoing events.
 */
public class SoaEventListParser extends SoaDefaultRssParser {

	/**
	 * Constructor which also sets the feed URL
	 * 
	 * @param string
	 *            the RSS feed URL
	 */
	public SoaEventListParser(String string) {
		super(string);
	}

	/**
	 * Parse the event feed and create a listing of the events to be displayed.
	 * <p>
	 * The following logic will be followed:
	 * <ul>
	 * <li>If the event date is listed in the feed for a previous date and the title
	 * contains the word "comp", then the event is assumed to be a weekly skill
	 * competition and is listed as such. Weekly skill competitions will stop being
	 * listed 7 days after they are introduced to the feed. If a competition is
	 * listed and it runs for more than 1 week, the competition will only be listed
	 * on the current events listing for 1 week.</li>
	 * <li>If the event date is listed in the feed for a previous date and the title
	 * contains the word "ongoing", then the event is assumed to be an ongoing event
	 * (such as a forum event) and is listed as such. Ongoing events will continue
	 * to be listed until they are no longer in the forum event feed. If a
	 * competition is listed and also contains the word ongoing, it will follow the
	 * earlier mentioned competition logic</li>
	 * <li>If an event is listed on the feed for today's date, the event is listed
	 * as happening today</li>
	 * <li>If an event is listed on the feed for a future date, or is listed on the
	 * feed for a previous date but is not noted as ongoing or a competition, the
	 * the event is ignored and not listed at all.</li>
	 * </ul>
	 * 
	 * @return String containing the event listings for that day.
	 */
	@Override
	public String parse() {
		try {
			SyndFeed feed = getFeed();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date today = new Date();
			Calendar cal1 = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();
			cal1.setTimeZone(TimeZone.getTimeZone("UTC"));
			cal2.setTimeZone(TimeZone.getTimeZone("UTC"));
			cal2.setTime(today);

			int i = 0;

			Iterator<SyndEntry> entryIter = feed.getEntries().iterator();
			StringBuilder sb = new StringBuilder();
			sb.append("**Today's ");
			if (DiscordCfgFactory.getConfig().getGuildAbbreviation() != null) {
				sb.append(DiscordCfgFactory.getConfig().getGuildAbbreviation());
			}
			sb.append(" Events**\n");
			while (entryIter.hasNext()) {
				SyndEntry entry = (SyndEntry) entryIter.next();
				cal1.setTime(entry.getPublishedDate());

				// Ongoing weekly competitions
				if (DateAnalyzer.isBeforeDay(cal1, cal2) && entry.getTitle().toLowerCase().contains("comp")
						&& DateAnalyzer.daysBetween(cal1.getTime(), cal2.getTime()) < 7) {
					sb.append("The following competition is ongoing!\n");
					sb.append(entry.getTitle());
					sb.append("\nFor details, visit: <" + entry.getLink() + ">");
					sb.append("\n\n");
					i++;
				}
				// Ongoing events
				if (DateAnalyzer.isBeforeDay(cal1, cal2) && entry.getTitle().toLowerCase().contains("ongoing")) {
					sb.append("The following event is ongoing!\n");
					sb.append(entry.getTitle());
					sb.append("\nFor details, visit: <" + entry.getLink() + ">");
					sb.append("\n\n");
					i++;
				}
				// Today's events
				else if (DateAnalyzer.isSameDay(cal1, cal2)) {
					sb.append("Event Title: " + entry.getTitle());
					sb.append("\nEvent Date: " + DateAnalyzer.showMultipleTimezonesForEvent(entry.getPublishedDate()));
					sb.append("\nFor details, visit: <" + entry.getLink() + ">");
					sb.append("\n\n");
					i++;
				}
			}
			// If no events are scheduled for today, say that.
			if (i == 0) {
				sb.append("No events to show for today.\n\n");
			}
			if (DiscordCfgFactory.getConfig().getEventListingEvent().getEventEndline() != null
					&& !DiscordCfgFactory.getConfig().getEventListingEvent().getEventEndline().isEmpty()) {
				Iterator<String> lineIter = DiscordCfgFactory.getConfig().getEventListingEvent().getEventEndline()
						.iterator();
				while (lineIter.hasNext()) {
					sb.append(lineIter.next());
					if (lineIter.hasNext())
						sb.append("\n");
				}
			}
			return sb.toString();
		} catch (IllegalArgumentException | FeedException | IOException e) {
			SoaLogging.getLogger().error("Error generating event list", e);
		}

		return null;
	}

}
