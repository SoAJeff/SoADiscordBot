package com.soa.rs.discordbot.bot.events;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.rometools.rome.feed.synd.SyndEntry;
import com.soa.rs.discordbot.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.util.SoaLogging;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

public class RsNewsTask extends SoaDefaultRssParser {

	private String lastEventPostedTitle;
	private IDiscordClient client;
	private Date lastEventDate;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm z");

	/**
	 * Constructor which also sets the feed URL
	 *
	 * @param string the RSS feed URL
	 */
	public RsNewsTask(String string, IDiscordClient client) {
		super(string);
		this.client = client;
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public void runTask() {
		if (lastEventPostedTitle == null) {
			SoaLogging.getLogger().info("Initial run, setting the initial title");
			try {
				lastEventPostedTitle = getFeed().getEntries().get(0).getTitle();
				lastEventDate = getFeed().getEntries().get(0).getPublishedDate();
				SoaLogging.getLogger().debug("Initial title is " + lastEventPostedTitle);
			} catch (Exception e) {
				SoaLogging.getLogger().error("An error was encountered loading the RSS feed", e);
			} finally {
				return;
			}
		} else {
			SoaLogging.getLogger().info("Running full parse");
			parse();
		}
	}

	@Override
	public String parse() {

		List<SyndEntry> entries;
		try {
			entries = getFeed().getEntries();
		} catch (Exception e) {
			SoaLogging.getLogger().error("An error was encountered loading the RSS feed", e);
			return null;
		}

		SoaLogging.getLogger().info("Checking RS News Articles");

		Iterator<SyndEntry> iterator = entries.iterator();
		String tempTitle = null;
		while (iterator.hasNext()) {
			SyndEntry entry = iterator.next();
			if (!entry.getTitle().equals(lastEventPostedTitle) && (entry.getPublishedDate().equals(lastEventDate)
					|| entry.getPublishedDate().after(lastEventDate))) {
				tempTitle = entry.getTitle();

				SoaLogging.getLogger().info("New article found with title: " + tempTitle);
				SoaLogging.getLogger().debug("Generating Embed");

				EmbedBuilder builder = new EmbedBuilder();
				builder.withAuthorName("RuneScape News")
						.withAuthorUrl(DiscordCfgFactory.getConfig().getRsNewsTask().getRsNewsArchiveLink())
						.withAuthorIcon(DiscordCfgFactory.getConfig().getRsNewsTask().getRsNewsArchiveImage());
				builder.withTitle(entry.getTitle()).withUrl(entry.getUri());
				builder.withColor(Color.darkGray);
				if (entry.getEnclosures().size() > 0 && (entry.getEnclosures().get(0).getType().equals("image/png")
						|| entry.getEnclosures().get(0).getType().equals("image/jpeg")))
					builder.withImage(entry.getEnclosures().get(0).getUrl());
				builder.appendDesc(entry.getDescription().getValue());
				builder.withFooterText("Posted at: " + sdf.format(new Date()))
						.withFooterIcon("https://i.imgur.com/BcdoFfR.png");

				for (IChannel channel : this.client.getChannels()) {
					if (channel.getName().equals(DiscordCfgFactory.getConfig().getRsNewsTask().getChannel())) {
						SoaLogging.getLogger().debug("Sending embed to channel: " + channel.getName());
						SoaClientHelper.sendEmbedToChannel(channel, builder);
					}
				}
			} else {
				if (tempTitle != null) {
					SoaLogging.getLogger().debug("Updating new last title to be " + tempTitle);
					lastEventPostedTitle = tempTitle;
				} else
					SoaLogging.getLogger().info("None to display.");
				return null;
			}
		}
		return null;
	}
}
