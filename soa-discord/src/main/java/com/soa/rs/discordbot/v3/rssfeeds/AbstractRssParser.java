package com.soa.rs.discordbot.v3.rssfeeds;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.spec.EmbedCreateSpec;

/**
 * This class serves as a base for any RSS parsing that will be done from the
 * forums.
 */
public abstract class AbstractRssParser {

	private URL url;
	private SyndFeedInput input;

	/**
	 * Constructor which also sets the feed URL
	 */
	public AbstractRssParser() {
		input = new SyndFeedInput();
	}

	/**
	 * Sets the RSS feed URL
	 *
	 * @param string the RSS feed URL
	 */
	public void setUrl(String string) throws MalformedURLException {
		try {
			url = new URL(string);
		} catch (MalformedURLException e) {
			SoaLogging.getLogger(this).error("Error setting URL", e);
			throw e;
		}
	}

	/**
	 * Get the RSS feed from the forums
	 *
	 * @return the RSS feed object
	 * @throws IllegalArgumentException
	 * @throws FeedException
	 * @throws IOException
	 */
	protected SyndFeed getFeed() throws IllegalArgumentException, FeedException, IOException {
		return input.build(new XmlReader(url));

	}

	/**
	 * Parse the feed and return the output to be printed into Discord
	 *
	 * @return content to be printed to Discord
	 */
	public abstract String parse();

	public abstract List<Consumer<EmbedCreateSpec>> parseAsEmbed();
}
