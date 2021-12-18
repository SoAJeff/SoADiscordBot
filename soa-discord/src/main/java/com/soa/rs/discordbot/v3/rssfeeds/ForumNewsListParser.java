package com.soa.rs.discordbot.v3.rssfeeds;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.spec.EmbedCreateSpec;

/**
 * The SoaNewsListParser grabs information of new topics from the News and
 * announcements forum along with from the Promotions and Welcomes forum
 */
public class ForumNewsListParser extends AbstractRssParser {

	/**
	 * Constructor which also sets the feed URL
	 */
	public ForumNewsListParser() {
		super();
	}

	/**
	 * Collects the information from the RSS feed, determines if it is newer than
	 * the last check, and if so, adds it to be printed to Discord.
	 * 
	 * @return content to be printed to Discord
	 */
	@Override
	public String parse() {
		try {
			SyndFeed feed = getFeed();
			Date now = new Date();

			if (DiscordCfgFactory.getInstance().getNewsLastPost() == null) {
				DiscordCfgFactory.getInstance().setNewsLastPost(now);
				SoaLogging.getLogger(this).info("LastNews is null, setting NewsLastPost to " + now.toString());
				return null;
			}

			Iterator<SyndEntry> entryIter = feed.getEntries().iterator();
			StringBuilder sb = new StringBuilder();
			while (entryIter.hasNext()) {
				SyndEntry entry = entryIter.next();
				if (entry.getPublishedDate().compareTo(DiscordCfgFactory.getInstance().getNewsLastPost()) > 0) {
					sb.append("**News: **");
					sb.append(entry.getTitle());
					sb.append(": " + entry.getLink() + "\n");
				}
			}
			DiscordCfgFactory.getInstance().setNewsLastPost(now);
			SoaLogging.getLogger(this).info("Setting NewsLastPost to " + now.toString());
			return sb.toString();
		} catch (IllegalArgumentException | FeedException | IOException e) {
			SoaLogging.getLogger(this).error("Error generating news list", e);
		}
		return null;
	}

	@Override
	public List<EmbedCreateSpec> parseAsEmbed() {
		//Not used in this case
		return null;
	}

}
