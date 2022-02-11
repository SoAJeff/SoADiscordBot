package com.soa.rs.discordbot.v3.cfg;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import com.soa.rs.discordbot.v3.jaxb.DiscordConfiguration;
import com.soa.rs.discordbot.v3.util.XmlReader;

import org.xml.sax.SAXException;

/**
 * The <tt>DiscordCfg</tt> class is used for storing any configuration which
 * will be accessed by other parts of the bot. Configuration can be stored
 * within this class for easy access from other classes as needed.
 * <p>
 * The DiscordCfg should be created and accessed via
 * {@link DiscordCfgFactory#getInstance()}, and not via directly calling this
 * class.
 */

public class DiscordCfg {

	/**
	 * The date the last time that news was posted.
	 */
	private Date newsLastPost = null;

	/**
	 * The Uptime of the bot
	 */
	private LocalDateTime launchTime = null;

	private DiscordConfiguration config = null;

	private String botname = null;

	private String avatarUrl = null;

	private final TreeMap<String, String> helpMap = new TreeMap<>();

	private final List<String> commandFileNames = new ArrayList<>();

	/**
	 * Constructor for creating a DiscordCfg. This should never be called within the
	 * application outside of {@link DiscordCfgFactory}, and instead
	 * {@link DiscordCfgFactory#getInstance()} should be used.
	 */
	DiscordCfg() {
	}

	public DiscordConfiguration getConfig() {
		if (this.config == null) {
			this.config = new DiscordConfiguration();
		}
		return this.config;
	}

	/**
	 * Load an initial configuration from the configuration file specified on the
	 * command line.
	 *
	 * @param filename the path to the configuration file
	 * @throws JAXBException
	 * @throws SAXException
	 */
	public void loadFromFile(String filename) throws JAXBException, SAXException {
		XmlReader reader = new XmlReader();
		this.config = reader.loadAppConfig(filename);
	}

	/**
	 * Get the last time news was posted
	 *
	 * @return Date of last time news was posted.
	 */
	public Date getNewsLastPost() {
		return newsLastPost;
	}

	/**
	 * Set the last time news was posted
	 *
	 * @param newsFeedLastPost the last time news was posted.
	 */
	public void setNewsLastPost(Date newsFeedLastPost) {
		newsLastPost = newsFeedLastPost;
	}

	/**
	 * Get the launch time of the bot
	 *
	 * @return the launch time for the bot
	 */
	public LocalDateTime getLaunchTime() {
		return launchTime;
	}

	/**
	 * Set the launch time of the bot
	 *
	 * @param launchTime the launch time of the bot
	 */
	public void setLaunchTime(LocalDateTime launchTime) {
		this.launchTime = launchTime;
	}

	public String getBotname() {
		return botname;
	}

	public void setBotname(String botname) {
		this.botname = botname;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public void addHelpMessage(String command, String helpMsg) {
		synchronized (this) {
			helpMap.put(command, helpMsg);
		}
	}

	public TreeMap<String, String> getHelpMap() {
		return this.helpMap;
	}

	public List<String> getCommandFileNames() {
		return commandFileNames;
	}

	public void addCommandFilename(String name)
	{
		this.commandFileNames.add(name);
	}

	public boolean isUserTrackingEnabled() {
		return DiscordCfgFactory.getConfig().getUserTrackingEvent() != null && DiscordCfgFactory.getConfig()
				.getUserTrackingEvent().isEnabled();
	}

}
