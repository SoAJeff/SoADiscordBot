package com.soa.rs.discordbot.v3.rssfeeds;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.function.Consumer;

import com.rometools.rome.feed.synd.SyndEntry;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

public class RsNewsParser extends AbstractRssParser {

	private Map<String, Date> lastEventsPosted;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm z");
	private boolean initialized = false;

	/**
	 * Constructor which also sets the feed URL
	 */
	public RsNewsParser() {
		super();
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		lastEventsPosted = new HashMap<>();
	}

	public void initialize() {
		if (lastEventsPosted.isEmpty() && !initialized) {
			SoaLogging.getLogger(this).info("Initial RS News, fetching all present entries");
			try {
				for (SyndEntry entry : getFeed().getEntries()) {
					SoaLogging.getLogger(this)
							.trace("Adding already present title [" + entry.getTitle() + "] with date [" + entry
									.getPublishedDate() + "]");
					lastEventsPosted.put(entry.getTitle(), entry.getPublishedDate());
				}
				SoaLogging.getLogger(this).debug("LastEventsPosted contains " + lastEventsPosted.size() + " entries");
				initialized = true;
			} catch (Exception e) {
				SoaLogging.getLogger(this).error("An error was encountered loading the RSS feed", e);
			}
		} else {
			SoaLogging.getLogger(this).info("Running full parse");
		}
	}

	@Override
	public String parse() {
		//Not used in this case
		return null;
	}

	@Override
	public List<Consumer<EmbedCreateSpec>> parseAsEmbed() {
		List<SyndEntry> entries;
		Queue<SyndEntry> entriesToPost = new ArrayDeque<>();
		List<Consumer<EmbedCreateSpec>> specs = new ArrayList<>();
		if (!initialized) {
			initialize();
			return specs;
		}
		try {
			entries = getFeed().getEntries();
		} catch (Exception e) {
			SoaLogging.getLogger(this).error("An error was encountered loading the RSS feed", e);
			return specs;
		}

		SoaLogging.getLogger(this).info("Checking RS News Articles");

		for (SyndEntry entry : entries) {
			//If this returns null, then we have never seen the entry.  We will post it
			if (lastEventsPosted.get(entry.getTitle()) == null) {
				SoaLogging.getLogger(this).info("New article found with title: " + entry.getTitle());
				entriesToPost.add(entry);
				lastEventsPosted.put(entry.getTitle(), entry.getPublishedDate());
			}

			/*
			Commented out 23 June 2019.  Jagex's RSS Feed appears to do odd things at times, including changing the timestamp of
			entries on their feed.  This appears to be an error on their part, but it causes the reader to be tripped up.
			Possible solution would be to check if the published date is in the last 24h.  Even then, it seems unlikely
			a news post would have the same name, especially within the last 15 entries posted, and the URL would
			have to be different - perhaps we should log the URL too?

			Example:
			08:29:58.406 [parallel-1] INFO - Checking RS News Articles
08:29:58.407 [parallel-1] INFO - New article with same title/older date found with title: Player Support Blog - Upgrading Systems, prior date:Tue May 21 20:00:00 EDT 2019, new date: Mon Jun 10 20:00:00 EDT 2019
08:29:58.407 [parallel-1] INFO - New article with same title/older date found with title: Digsite Update & Wiki Integration, prior date:Sun May 19 20:00:00 EDT 2019, new date: Wed Jun 05 20:00:00 EDT 2019
			 */
			//If Jagex posts the same news entry title on the same date, then they are stupid.
/*			else if (lastEventsPosted.get(entry.getTitle()).before(entry.getPublishedDate())) {
				SoaLogging.getLogger()
						.info("New article with same title/older date found with title: " + entry.getTitle()
								+ ", prior date: " + lastEventsPosted.get(entry.getTitle()) + ", new date: " + entry
								.getPublishedDate());
				entriesToPost.add(entry);
				lastEventsPosted.put(entry.getTitle(), entry.getPublishedDate());
			}*/

		}

		if (entriesToPost.size() > 0) {
			while (!entriesToPost.isEmpty()) {
				SoaLogging.getLogger(this)
						.debug("Generating Embed for article with title: " + entriesToPost.peek().getTitle());
				specs.add(createEmbed(entriesToPost.poll()));
			}
			SoaLogging.getLogger(this).debug("LastEventsPosted contains " + lastEventsPosted.size() + " entries");
		} else {
			SoaLogging.getLogger(this).info("None to display.");
		}
		return specs;
	}

	private Consumer<EmbedCreateSpec> createEmbed(SyndEntry entry) {
		return embedCreateSpec -> {
			embedCreateSpec
					.setAuthor("RuneScape News", DiscordCfgFactory.getConfig().getRsNewsTask().getRsNewsArchiveLink(),
							DiscordCfgFactory.getConfig().getRsNewsTask().getRsNewsArchiveImage());
			embedCreateSpec.setTitle(entry.getTitle()).setUrl(entry.getUri());
			embedCreateSpec.setColor(new Color(64, 64, 64));
			if (entry.getEnclosures().size() > 0 && (entry.getEnclosures().get(0).getType().equals("image/png") || entry
					.getEnclosures().get(0).getType().equals("image/jpeg")))
				embedCreateSpec.setImage(entry.getEnclosures().get(0).getUrl());
			embedCreateSpec.setDescription(entry.getDescription().getValue());
			embedCreateSpec.setFooter("Posted at: " + sdf.format(new Date()), "https://i.imgur.com/BcdoFfR.png");
		};
	}

	Map<String, Date> getLastEventsPosted() {
		return lastEventsPosted;
	}

}
