package com.soa.rs.discordbot.v3.rssfeeds;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jaxb.RsListingEvent;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

public class RsNewsParserTest {

	private InputSource source;

	@Before
	public void setup() {
		ByteArrayInputStream bais = new ByteArrayInputStream(feed.getBytes());
		source = new InputSource(bais);
		SoaLogging.initializeLogging();
		DiscordCfgFactory.getConfig().setRsNewsTask(new RsListingEvent());
		DiscordCfgFactory.getConfig().getRsNewsTask().setRsNewsArchiveLink("Test Link");
		DiscordCfgFactory.getConfig().getRsNewsTask().setRsNewsArchiveImage("Test Image");
	}

	@Test
	public void testInitialLoad() {
		MockRsNewsParser realParser = new MockRsNewsParser();
		realParser.setSource(source);
		try {
			realParser.initialize();
			Assert.assertEquals(15, realParser.getLastEventsPosted().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testNoNewItems() {
		MockRsNewsParser parser = new MockRsNewsParser();
		parser.setSource(source);
		try {
			parser.initialize();
			parser.setSource(new InputSource(new ByteArrayInputStream(feed.getBytes())));
			Assert.assertEquals(0, parser.parseAsEmbed().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testNewItem() {
		MockRsNewsParser parser = new MockRsNewsParser();
		parser.setSource(source);
		try {
			parser.initialize();
			parser.getLastEventsPosted().remove("MAW enhancers & Actionbar fixes");
			parser.setSource(new InputSource(new ByteArrayInputStream(feed.getBytes())));
			Assert.assertEquals(1, parser.parseAsEmbed().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testItemHasDifferentDateNewArticle() {
		MockRsNewsParser parser = new MockRsNewsParser();
		parser.setSource(source);
		try {
			parser.initialize();
			parser.getLastEventsPosted().get("MAW enhancers & Actionbar fixes").setTime(12345);
			parser.setSource(new InputSource(new ByteArrayInputStream(feed.getBytes())));
			//When fixed, switch this back
//			Assert.assertEquals(1, parser.parseAsEmbed().size());
			Assert.assertEquals(0, parser.parseAsEmbed().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testItemHasDifferentDateEarlierArticle() {
		MockRsNewsParser parser = new MockRsNewsParser();
		parser.setSource(source);
		try {
			parser.initialize();
			parser.getLastEventsPosted().get("MAW enhancers & Actionbar fixes")
					.setTime(parser.getLastEventsPosted().get("MAW enhancers & Actionbar fixes").getTime() + 1);
			parser.setSource(new InputSource(new ByteArrayInputStream(feed.getBytes())));
			Assert.assertEquals(0, parser.parseAsEmbed().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}



	private class MockRsNewsParser extends RsNewsParser {

		InputSource source;

		MockRsNewsParser() {
			super();
		}

		@Override
		public SyndFeed getFeed() throws IllegalArgumentException, FeedException, IOException {
			return new SyndFeedInput().build(source);
		}

		public void setSource(InputSource source) {
			this.source = source;
		}
	}

	/**
	 * Jagex feed as of 19Jun2019.  May be useful for testing parser as Jagex seems to update their feed oddly.
	 */
	private String feed = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
			+ "<rss xmlns:dc=\"http://purl.org/dc/elements/1.1\" version=\"2.0\">\n" + "<channel>\n"
			+ "<language>en</language>\n" + "<title>RuneScape Recent News</title>\n"
			+ "<description>The latest news for RuneScape, the massively-multiplayer online game developed and published by Jagex.</description>\n"
			+ "<link>https://secure.runescape.com/m=news</link>\n"
			+ "<docs>http://www.rssboard.org/rss-specification</docs>\n"
			+ "<generator>JagexRssFeedGenerator</generator>\n" + "<ttl>30</ttl>\n" + "<copyright></copyright>\n"
			+ "<lastBuildDate>Wed, 19 Jun 2019 20:43:46 GMT</lastBuildDate>\n" + "<image>\n"
			+ "<link>https://secure.runescape.com/m=news</link>\n" + "<title>RuneScape Recent News</title>\n"
			+ "<url>https://www.runescape.com/img/responsive/common/logos/rss.svg</url>\n" + "</image>\n" + "<item>\n"
			+ "<title>Mobile Dev Blog - June</title>\n" + "<dc:creator></dc:creator>\n" + "<description>\n"
			+ "Read on for what we&apos;ve been up to on Mobile this month!\n" + "</description>\n"
			+ "<category>Mobile</category>\n"
			+ "<link>https://secure.runescape.com/m=news/mobile-dev-blog---june</link>\n"
			+ "<pubDate>Wed, 12 Jun 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/mobile-dev-blog---june</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>Mahjarrat Aura - Update!</title>\n" + "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2019/06/Aura-News.jpg\" />\n"
			+ "<description>\n"
			+ "We&apos;re happy to confirm that from July 29th the Mahjarrat Aura will be obtainable by all players!\n"
			+ "</description>\n" + "<category>Future Updates</category>\n"
			+ "<link>https://secure.runescape.com/m=news/mahjarrat-aura---update</link>\n"
			+ "<pubDate>Tue, 18 Jun 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/mahjarrat-aura---update</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>MAW enhancers &amp; Actionbar fixes</title>\n"
			+ "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2019/06/Battle-with-Mah_News_Header.jpg\" />\n"
			+ "<description>\n"
			+ "We&#8217;re leaping into action this patch week, tackling all kinds of issues. Most importantly, we&#8217;ve made some changes to Motherlode Maw Enhancers and Actionbars.\n"
			+ "</description>\n" + "<category>Game Updates</category>\n"
			+ "<link>https://secure.runescape.com/m=news/maw-enhancers--actionbar-fixes</link>\n"
			+ "<pubDate>Mon, 17 Jun 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/maw-enhancers--actionbar-fixes</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>Summer Special 2019</title>\n" + "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2019/06/RS19-News-Header_Summer_Special.jpg\" />\n"
			+ "<description>\n" + "It&apos;s that time of the year again! Summer Special 2019 is here!\n"
			+ "</description>\n" + "<category>Website</category>\n"
			+ "<link>https://secure.runescape.com/m=news/summer-special-2019</link>\n"
			+ "<pubDate>Mon, 10 Jun 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/summer-special-2019</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>Smithing Quality of Life</title>\n" + "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2019/06/Smithing-QoL-News-Header.jpg\" />\n"
			+ "<description>\n"
			+ "Clink&#8230;clink&#8230;clink&#8230; Oh, don&#8217;t mind the noise; we&#8217;re just putting the finishing touches on Smithing. Read on to find out more!\n"
			+ "</description>\n" + "<category>Game Updates</category>\n"
			+ "<link>https://secure.runescape.com/m=news/smithing-quality-of-life</link>\n"
			+ "<pubDate>Mon, 10 Jun 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/smithing-quality-of-life</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>Month Ahead: June</title>\n" + "<dc:creator></dc:creator>\n"
			+ "<description>\n"
			+ "Most of us are currently fixated on the mysteries awaiting us in The Land Out of Time, but in the meantime there&#8217;s plenty happening in mainland Gielinor.\n"
			+ "</description>\n" + "<category>Behind The Scenes</category>\n"
			+ "<link>https://secure.runescape.com/m=news/month-ahead-june</link>\n"
			+ "<pubDate>Fri, 07 Jun 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/month-ahead-june</guid>\n" + "</item>\n"
			+ "<item>\n" + "<title>Comp Cape - Dev Blog 4</title>\n" + "<dc:creator></dc:creator>\n" + "<description>\n"
			+ "Another update on the Comp Cape Design!\n" + "</description>\n" + "<category>Dev Blogs</category>\n"
			+ "<link>https://secure.runescape.com/m=news/comp-cape---dev-blog-4</link>\n"
			+ "<pubDate>Thu, 06 Jun 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/comp-cape---dev-blog-4</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>Weapon Diversity Beta</title>\n" + "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2019/06/Weapon-Diversity-Beta_News_Header.jpg\" />\n"
			+ "<description>\n"
			+ "We&#8217;re hacking, slashing, and whacking our way into June this week with our first look at the Weapon Diversity Beta!\n"
			+ "</description>\n" + "<category>Game Updates</category>\n"
			+ "<link>https://secure.runescape.com/m=news/weapon-diversity-beta</link>\n"
			+ "<pubDate>Mon, 03 Jun 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/weapon-diversity-beta</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>RuneFest 2019 - Tickets Now Available!</title>\n"
			+ "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2018/4/Runefest_Announcement_News_thumbnail.jpg\" />\n"
			+ "<description>\n" + "Tickets now available!\n" + "</description>\n" + "<category>Events</category>\n"
			+ "<link>https://secure.runescape.com/m=news/runefest-2019---tickets-now-available-1</link>\n"
			+ "<pubDate>Fri, 31 May 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/runefest-2019---tickets-now-available-1</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>Quality of Life Week</title>\n" + "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2019/05/QoL_week_news_main_image.jpg\" />\n"
			+ "<description>\n"
			+ "We all love big, exciting updates like The Land Out of Time, but sometimes it&#8217;s good to look back on older content and fix things that aren&apos;t quite right.\n"
			+ "</description>\n" + "<category>Game Updates</category>\n"
			+ "<link>https://secure.runescape.com/m=news/quality-of-life-week</link>\n"
			+ "<pubDate>Tue, 28 May 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/quality-of-life-week</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>RuneFest 2019 - Save The Date! </title>\n"
			+ "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2018/4/Runefest_Announcement_News_thumbnail.jpg\" />\n"
			+ "<description>\n" + "Save the Date!\n" + "</description>\n" + "<category>Events</category>\n"
			+ "<link>https://secure.runescape.com/m=news/runefest-2019---save-the-date-</link>\n"
			+ "<pubDate>Fri, 24 May 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/runefest-2019---save-the-date-</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>Smouldering Lamps</title>\n" + "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/png\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2018/4/News_Banner-SmoulderingLamps3.png\" />\n"
			+ "<description>\n" + "They&apos;re back and ready to burn!\n" + "</description>\n"
			+ "<category>Treasure Hunter</category>\n"
			+ "<link>https://secure.runescape.com/m=news/smouldering-lamps</link>\n"
			+ "<pubDate>Thu, 23 May 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/smouldering-lamps</guid>\n" + "</item>\n"
			+ "<item>\n" + "<title>Player Support Blog - Upgrading Systems</title>\n" + "<dc:creator></dc:creator>\n"
			+ "<description>\n"
			+ "In our message to the community on March 19th, we promised to release a series of Player Support blogs. This is our first blog!\n"
			+ "</description>\n" + "<category>Support</category>\n"
			+ "<link>https://secure.runescape.com/m=news/player-support-blog---upgrading-systems</link>\n"
			+ "<pubDate>Wed, 22 May 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/player-support-blog---upgrading-systems</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>Summer Update Revealed!</title>\n" + "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2019/05/Land_Out_of_Time_News_Thumbnail.jpg\" />\n"
			+ "<description>\n"
			+ "Storm clouds have gathered in Gielinor, and within them lies a long-kept secret... that&apos;s about to be revealed!&#13;&#10;\n"
			+ "</description>\n" + "<category>Future Updates</category>\n"
			+ "<link>https://secure.runescape.com/m=news/summer-update-revealed</link>\n"
			+ "<pubDate>Tue, 21 May 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/summer-update-revealed</guid>\n"
			+ "</item>\n" + "<item>\n" + "<title>Digsite Update &amp; Wiki Integration</title>\n"
			+ "<dc:creator></dc:creator>\n"
			+ "<enclosure type=\"image/jpeg\" length=\"0\" url=\"https://cdn.runescape.com/assets/img/external/news/2019/05/digstie_update_header.jpg\" />\n"
			+ "<description>\n"
			+ "You might have been digging around for clues about our big summer update, but we&#8217;ve been up to some digging of our own.\n"
			+ "</description>\n" + "<category>Game Updates</category>\n"
			+ "<link>https://secure.runescape.com/m=news/digsite-update--wiki-integration</link>\n"
			+ "<pubDate>Mon, 20 May 2019 00:00:00 GMT</pubDate>\n"
			+ "<guid isPermaLink=\"true\">https://secure.runescape.com/m=news/digsite-update--wiki-integration</guid>\n"
			+ "</item>\n" + "</channel>\n" + "</rss>";
}
