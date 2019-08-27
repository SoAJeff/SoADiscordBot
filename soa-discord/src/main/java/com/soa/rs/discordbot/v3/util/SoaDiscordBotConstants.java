package com.soa.rs.discordbot.v3.util;

/**
 * Contains some default values
 */
public class SoaDiscordBotConstants {

	/**
	 * Default event calendar feed URL
	 */
	public static final String EVENT_CALENDAR_URL = "https://forums.soa-rs.com/calendar/events.xml";

	/**
	 * Default news feed URL
	 */
	public static final String NEWS_FEED_URL = "https://forums.soa-rs.com/rss/1-soa-promos-and-news.xml";

	/**
	 * Default username for the bot
	 */
	public static final String BOT_USERNAME = "SoA";

	/**
	 * Default avatar for the bot
	 */
	public static final String AVATAR_URL = "http://soa-rs.com/img/greenlogo.png";

	/**
	 * Default "playing" status for the bot
	 */
	public static final String PLAYING_STATUS = "RuneScape Clan";

	/**
	 * Link to the forum thread for the SoA Discord Bot
	 */
	public static final String FORUMTHREAD_URL = "https://forums.soa-rs.com/topic/22213-soas-new-discord-bot/";

	/**
	 * Link to the Github repository for the SoA Discord Bot
	 */
	public static final String GITHUB_URL = "https://github.com/SoAJeff/SoADiscordBot";

	/**
	 * Prefix used for any command caught by the MessageReceivedEventListener
	 */
	public static final String BOT_PREFIX = ".";

	/**
	 * The standard set of staff roles within SoA used for checking if user is a
	 * member of staff
	 */
	public static final String[] STAFF_ROLES = new String[] { "Eldar", "Lian", "Arquendi" };

	/**
	 * The RuneInfo setRSN command
	 */
	public static final String RUNEINFO_PREFIX = "!setrsn";

}
