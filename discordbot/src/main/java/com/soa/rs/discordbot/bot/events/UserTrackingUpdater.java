package com.soa.rs.discordbot.bot.events;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.xml.sax.SAXException;

import com.soa.rs.discordbot.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.jaxb.Actions;
import com.soa.rs.discordbot.jaxb.CurrentUser;
import com.soa.rs.discordbot.jaxb.DisplayNames;
import com.soa.rs.discordbot.jaxb.Guild;
import com.soa.rs.discordbot.jaxb.GuildUsers;
import com.soa.rs.discordbot.jaxb.LeftUser;
import com.soa.rs.discordbot.jaxb.LeftUsers;
import com.soa.rs.discordbot.jaxb.RecentAction;
import com.soa.rs.discordbot.jaxb.RecentActions;
import com.soa.rs.discordbot.jaxb.TrackedInformation;
import com.soa.rs.discordbot.jaxb.User;
import com.soa.rs.discordbot.util.DateAnalyzer;
import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.util.SoaLogging;
import com.soa.rs.discordbot.util.XmlReader;
import com.soa.rs.discordbot.util.XmlWriter;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;

/**
 * The UserTrackingUpdater handles all interactions with the tracking database.
 * This includes loading it, populating it on a regular basis, making any
 * modifications needed to it, and retrieving requested information from it.
 */
public class UserTrackingUpdater {

	/**
	 * Singleton class instance
	 */
	private static final UserTrackingUpdater INSTANCE = new UserTrackingUpdater();

	/**
	 * Discord client in use
	 */
	private IDiscordClient client;

	/**
	 * Xml Writer for user tracking file
	 */
	private XmlWriter writer = new XmlWriter();

	/**
	 * Xml Reader for initial load of user tracking file
	 */
	private XmlReader reader = new XmlReader();

	/**
	 * Object to hold all tracked information
	 */
	private TrackedInformation information = null;

	/**
	 * Empty constructor for use to enforce singleton instance.
	 */
	private UserTrackingUpdater() {

	}

	/**
	 * Returns the singleton user tracking instance
	 * 
	 * @return user tracking instance
	 */
	public static UserTrackingUpdater getInstance() {
		return INSTANCE;
	}

	/**
	 * Sets the discord client
	 * 
	 * @param client
	 *            The discord client in use.
	 */
	public void setClient(IDiscordClient client) {
		this.client = client;
	}

	/**
	 * Sets the currently in use tracked information. In normal operation, this
	 * method is not used.
	 * 
	 * @param information
	 *            User tracking information to be used
	 */
	void setInformation(TrackedInformation information) {
		this.information = information;
	}

	/**
	 * Load the tracking information from the configured file. This call will only
	 * work if the information is set to null; after the first load, there is no
	 * need to call this method again, and it will not re-load the file.
	 */
	public synchronized void loadInformation() {
		if (information == null) {
			try {
				this.information = reader.loadTrackedConfiguration(
						DiscordCfgFactory.getConfig().getUserTrackingEvent().getTrackingFile());
			} catch (JAXBException | SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				this.information = new TrackedInformation();
			}
		}
	}

	/**
	 * Populates the user tracking information object. This will iterate through all
	 * connected guilds and update information for all users. This method is
	 * intended to be scheduled on a regular basis to keep the information up to
	 * date. This method will write out information to the tracking file once
	 * completed, if a file is configured.
	 */
	public synchronized void populateInformation() {
		SoaLogging.getLogger().info("Running User Tracking Updater task.");
		List<IGuild> guilds = this.client.getGuilds();
		for (IGuild guild : guilds) {
			SoaLogging.getLogger().debug("Updating user information for guild: " + guild.getName());
			long guildId = guild.getLongID();
			boolean guildExists = false;
			for (Guild savedGuild : this.information.getGuild()) {
				if (savedGuild.getGuildId() == guildId) {
					guildExists = true;
					updateGuildListing(savedGuild, guild);
					updateLeftListing(savedGuild, guild);
				}
			}
			if (!guildExists) {
				updateGuildListing(guild);
			}
		}
		try {
			if (DiscordCfgFactory.getConfig().getUserTrackingEvent().getTrackingFile() != null) {
				writer.writeTrackedConfiguration(information,
						DiscordCfgFactory.getConfig().getUserTrackingEvent().getTrackingFile());
			}
		} catch (JAXBException | SAXException | IOException e) {
			SoaLogging.getLogger().error("Error writing out user information file: " + e.getMessage(), e);
		}
	}

	/**
	 * Adds a new guild to the tracking file
	 * 
	 * @param guild
	 *            Discord object identifying the guild.
	 */
	private void updateGuildListing(IGuild guild) {
		Guild newGuild = new Guild();
		newGuild.setGuildId(guild.getLongID());
		newGuild.setGuildName(guild.getName());
		newGuild.setGuildUsers(new GuildUsers());
		newGuild.setLeftUsers(new LeftUsers());
		newGuild.setRecentActions(new RecentActions());
		newGuild = updateGuildListing(newGuild, guild);
		this.information.getGuild().add(newGuild);
	}

	/**
	 * Updates the information for the guild of users who are currently within the
	 * guild.
	 * 
	 * @param ourGuild
	 *            Internal tracking information for the guild being reviewed
	 * @param guild
	 *            Discord4J information for the guild being reviewed
	 * @return The updated guild object.
	 */
	private Guild updateGuildListing(Guild ourGuild, IGuild guild) {
		if (ourGuild.getRecentActions() == null) {
			ourGuild.setRecentActions(new RecentActions());
		}

		List<IUser> users = guild.getUsers();
		for (IUser user : users) {
			int position = getUserPosition(ourGuild, user);
			if (position != -1) {
				try {
					SoaLogging.getLogger().trace("User " + user.getName() + " in list, updating details.");
					CurrentUser curUser = ourGuild.getGuildUsers().getUser().get(position);
					if (!SoaClientHelper.getDiscordUserNameForUser(user).equals(curUser.getUserName())) {
						addRecentAction(ourGuild.getRecentActions(), curUser.getUserName(), Actions.CHANGED_USER_HANDLE,
								SoaClientHelper.getDiscordUserNameForUser(user));
						curUser.setUserName(SoaClientHelper.getDiscordUserNameForUser(user));

					}
					if (isNewDisplayName(curUser.getDisplayNames(), user.getDisplayName(guild))) {
						addRecentAction(ourGuild.getRecentActions(), curUser.getUserName(),
								Actions.CHANGED_DISPLAY_NAME, curUser.getDisplayNames().getDisplayName().get(0),
								user.getDisplayName(guild));
					}
					if (wasPriorDisplayName(curUser.getDisplayNames(), user.getDisplayName(guild))) {
						curUser.getDisplayNames().getDisplayName().add(0, user.getDisplayName(guild));
					}
					IPresence presence = user.getPresence();
					if (presence.getStatus() != StatusType.OFFLINE) {
						curUser.setLastOnline(getCurrentDate());
					}
				} catch (Exception e) {
					// Something went wrong
				}

			} else {
				try {
					SoaLogging.getLogger().trace("User " + user.getName() + " not in list, adding.");
					addRecentAction(ourGuild.getRecentActions(), SoaClientHelper.getDiscordUserNameForUser(user),
							Actions.JOINED_SERVER);
					CurrentUser curUser = new CurrentUser();

					curUser.setUserId(user.getLongID());
					curUser.setUserName(SoaClientHelper.getDiscordUserNameForUser(user));
					curUser.setDisplayNames(new DisplayNames());
					curUser.getDisplayNames().getDisplayName().add(user.getDisplayName(guild));
					curUser.setJoined(convertDateToCal(guild.getJoinTimeForUser(user)));
					IPresence presence = user.getPresence();
					if (presence.getStatus() != StatusType.OFFLINE) {

						curUser.setLastOnline(getCurrentDate());
					} else {
						curUser.setLastOnline(convertDateToCal(guild.getJoinTimeForUser(user)));
					}

					ourGuild.getGuildUsers().getUser().add(curUser);
				} catch (Exception e) {
					// Something went wrong
				}
			}
		}
		return ourGuild;
	}

	/**
	 * Updates the information for the guild of users who have left the guild.
	 * 
	 * @param ourGuild
	 *            Internal tracking information for the guild being reviewed
	 * @param guild
	 *            Discord4J information for the guild being reviewed
	 * @return The updated guild object.
	 */
	private Guild updateLeftListing(Guild ourGuild, IGuild guild) {
		List<IUser> users = guild.getUsers();

		Iterator<CurrentUser> curIter = ourGuild.getGuildUsers().getUser().iterator();

		while (curIter.hasNext()) {
			CurrentUser user = curIter.next();
			boolean hasLeft = true;
			Iterator<IUser> iter = users.iterator();
			while (iter.hasNext()) {
				IUser checkUser = iter.next();
				SoaLogging.getLogger().trace("Our User " + user.getUserId() + " DiscordUser " + checkUser.getLongID());
				if (checkUser.getLongID() == user.getUserId()) {
					SoaLogging.getLogger().trace("This person has not left.");
					hasLeft = false;
				}
			}
			if (hasLeft) {
				try {
					SoaLogging.getLogger().trace("User " + user.getUserName() + " has left, adding as left user.");
					addRecentAction(ourGuild.getRecentActions(), user.getUserName(), Actions.LEFT_SERVER);
					LeftUser leftUser = new LeftUser();
					leftUser.setUserId(user.getUserId());
					leftUser.setUserName(user.getUserName());
					leftUser.setDisplayNames(new DisplayNames());
					leftUser.getDisplayNames().getDisplayName().addAll(user.getDisplayNames().getDisplayName());

					leftUser.setLeft(getCurrentDate());

					ourGuild.getLeftUsers().getUser().add(leftUser);
					curIter.remove();
				} catch (DatatypeConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		Iterator<LeftUser> leftIter = ourGuild.getLeftUsers().getUser().iterator();

		while (leftIter.hasNext()) {
			LeftUser user = leftIter.next();
			if (DateAnalyzer.daysBetween(user.getLeft().toGregorianCalendar().getTime(), new Date()) > 30) {
				leftIter.remove();
			}
		}
		return ourGuild;
	}

	/**
	 * Gets the user's position within the guild.
	 * 
	 * @param ourGuild
	 *            Internal tracking information for the guild being reviewed
	 * @param user
	 *            Discord4J user object being searched for.
	 * @return The position within the internal tracking for the user if they exist,
	 *         -1 otherwise.
	 */
	private int getUserPosition(Guild ourGuild, IUser user) {
		int position = -1;
		Iterator<CurrentUser> iter = ourGuild.getGuildUsers().getUser().iterator();
		int i = 0;
		while (iter.hasNext()) {
			CurrentUser checkUser = iter.next();
			if (checkUser.getUserId() == user.getLongID()) {
				position = i;
				return position;
			}
			i++;
		}

		return -1;
	}

	/**
	 * Checks if the user has a new display name or not that they have never had
	 * before.
	 * 
	 * @param displayNames
	 *            Listing of all past recorded display names
	 * @param name
	 *            The current display name
	 * @return True if they do have a new display name, false otherwise.
	 */
	private boolean wasPriorDisplayName(DisplayNames displayNames, String name) {
		Iterator<String> iter = displayNames.getDisplayName().iterator();
		int i = 0;
		while (iter.hasNext()) {
			String dispName = iter.next();
			if (dispName.equals(name)) {
				if (i > 0) {
					iter.remove();
					displayNames.getDisplayName().add(0, dispName);
				}
				return false;
			}
			i++;
		}
		return true;
	}

	/**
	 * Checks if the user's display name has changed since the last analysis
	 * 
	 * @param displayNames
	 *            Listing of all past recorded display names
	 * @param name
	 *            The current display name on the account
	 * @return True if the name has changed, false otherwise.
	 */
	private boolean isNewDisplayName(DisplayNames displayNames, String name) {
		if (displayNames.getDisplayName().get(0).equals(name))
			return false;
		return true;
	}

	/**
	 * Adds a recent action to the Recent Actions List
	 * 
	 * @param actions
	 *            The Recent Actions list
	 * @param user
	 *            The user affected by the action
	 * @param action
	 *            The action to be recorded
	 */
	private void addRecentAction(RecentActions actions, String user, Actions action) {
		addRecentAction(actions, user, action, null, null);
	}

	/**
	 * Adds a recent action including the after value to the Recent Actions List
	 * 
	 * @param actions
	 *            The Recent Actions list
	 * @param user
	 *            The user affected by the action
	 * @param action
	 *            The action to be recorded
	 * @param changedValue
	 *            The new value for the property changed
	 */
	private void addRecentAction(RecentActions actions, String user, Actions action, String changedValue) {
		addRecentAction(actions, user, action, null, changedValue);
	}

	/**
	 * Adds a recent action including the before and after values specified to the
	 * Recent Actions List
	 * 
	 * @param actions
	 *            The Recent Actions list
	 * @param user
	 *            The user affected by the action
	 * @param action
	 *            The action to be recorded
	 * @param originalValue
	 *            The before value for the property changed
	 * @param changedValue
	 *            The after value for the property changed
	 */
	private void addRecentAction(RecentActions actions, String user, Actions action, String originalValue,
			String changedValue) {
		RecentAction recentAction = new RecentAction();
		recentAction.setUser(user);
		recentAction.setAction(action);
		if (originalValue != null) {
			recentAction.setOriginalValue(originalValue);
		}
		if (changedValue != null) {
			recentAction.setChangedValue(changedValue);
		}

		if (actions.getRecentAction().size() >= 15) {
			actions.getRecentAction().remove(14);
		}
		actions.getRecentAction().add(0, recentAction);
	}

	/**
	 * Converts a <tt>LocalDateTime</tt> into a <tt>XMLGregorianCalendar</tt>.
	 * 
	 * @param dateTime
	 *            Date to be converted
	 * @return The converted date
	 * @throws DatatypeConfigurationException
	 *             If the date cannot be converted for some reason.
	 */
	private XMLGregorianCalendar convertDateToCal(LocalDateTime dateTime) throws DatatypeConfigurationException {
		XMLGregorianCalendar calendar = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(dateTime.toLocalDate().toString());
		return calendar;
	}

	/**
	 * Converts the current date into a <tt>XMLGregorianCalendar</tt>
	 * 
	 * @return The converted date
	 * @throws DatatypeConfigurationException
	 *             If the date cannot be converted for some reason.
	 */
	private XMLGregorianCalendar getCurrentDate() throws DatatypeConfigurationException {
		return getDate(new Date());
	}

	/**
	 * Converts the specified date into a <tt>XMLGregorianCalendar</tt>
	 * 
	 * @param date
	 *            The date to be converted
	 * @return The converted date
	 * @throws DatatypeConfigurationException
	 *             If the date cannot be converted for some reason.
	 */
	private XMLGregorianCalendar getDate(Date date) throws DatatypeConfigurationException {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		return date2;
	}

	/**
	 * Adds a known name a specified user.
	 * 
	 * @param search
	 *            The name to search for
	 * @param name
	 *            The name to attach to the user
	 * @return True if the operation was successful, false otherwise.
	 */
	public synchronized boolean addKnownNameToUser(String search, String name) {
		boolean updated = false;

		Map<User, Long> searchedUsers = searchUsers(search, true);

		if (searchedUsers.size() > 0) {
			Iterator<Entry<User, Long>> iter = searchedUsers.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<User, Long> entry = iter.next();
				entry.getKey().setKnownName(name);
			}
			updated = true;
		}

		return updated;
	}

	/**
	 * Searches for a user within the tracked information.
	 * 
	 * @param search
	 *            The string to search for
	 * @param mustEqual
	 *            Boolean detailing whether the string should match the entry
	 *            exactly to be considered a result, or just be contained within to
	 *            be a result.
	 * @return A map containing entries with the user object and the guild ID that
	 *         they were found in.
	 */
	public Map<User, Long> searchUsers(String search, boolean mustEqual) {
		Map<User, Long> searchedUsers = new HashMap<User, Long>();

		for (Guild savedGuild : this.information.getGuild()) {

			List<CurrentUser> guildUsers = savedGuild.getGuildUsers().getUser();
			Iterator<CurrentUser> iter = guildUsers.iterator();
			while (iter.hasNext()) {
				CurrentUser user = iter.next();
				if (mustEqual) {
					// For settingKnownName
					if (user.getUserName().toLowerCase().equals(search.toLowerCase())) {
						searchedUsers.put(user, savedGuild.getGuildId());
					} else {
						Iterator<String> displayNamesIter = user.getDisplayNames().getDisplayName().iterator();
						while (displayNamesIter.hasNext()) {
							String dispName = displayNamesIter.next();
							if (dispName.toLowerCase().equals(search.toLowerCase())) {
								searchedUsers.put(user, savedGuild.getGuildId());
							}
						}
					}
				} else {
					// For search
					if (user.getUserName().toLowerCase().contains(search.toLowerCase())
							|| (user.getKnownName() != null && user.getKnownName().contains(search))) {
						searchedUsers.put(user, savedGuild.getGuildId());
					} else {
						Iterator<String> displayNamesIter = user.getDisplayNames().getDisplayName().iterator();
						while (displayNamesIter.hasNext()) {
							String dispName = displayNamesIter.next();
							if (dispName.toLowerCase().contains(search.toLowerCase())) {
								searchedUsers.put(user, savedGuild.getGuildId());
							}
						}
					}
				}
			}

		}

		return searchedUsers;
	}

	/**
	 * Searches for a user who has left the guild within tracked information.
	 * 
	 * @param search
	 *            The string to search for
	 * 
	 * @return A map containing entries with the user object and the guild ID that
	 *         they were found in.
	 */
	public Map<LeftUser, Long> searchLeftUsers(String search) {
		Map<LeftUser, Long> searchedUsers = new HashMap<LeftUser, Long>();

		for (Guild savedGuild : this.information.getGuild()) {

			List<LeftUser> leftUsers = savedGuild.getLeftUsers().getUser();
			Iterator<LeftUser> iter = leftUsers.iterator();
			while (iter.hasNext()) {
				LeftUser user = iter.next();
				if (user.getUserName().toLowerCase().contains(search.toLowerCase())) {
					searchedUsers.put(user, savedGuild.getGuildId());
				} else {
					Iterator<String> displayNamesIter = user.getDisplayNames().getDisplayName().iterator();
					while (displayNamesIter.hasNext()) {
						String dispName = displayNamesIter.next();
						if (dispName.toLowerCase().contains(search.toLowerCase())) {
							searchedUsers.put(user, savedGuild.getGuildId());
						}
					}
				}
			}
		}

		return searchedUsers;
	}

	/**
	 * Gets the recent actions for the specified guild
	 * 
	 * @param guildID
	 *            Long ID identifying the guild
	 * @return Recent Actions for the guild
	 */
	public RecentActions getRecentActionsForGuild(long guildID) {
		for (Guild guild : this.information.getGuild()) {
			if (guild.getGuildId() == guildID) {
				return guild.getRecentActions();
			}
		}
		// Guild ID isn't for a tracked guild, return null
		return null;
	}

	/**
	 * Writes the tracked information to a stream and returns the stream
	 * 
	 * @return A stream containing the tracked information.
	 * @throws JAXBException
	 *             If an error occurs marshalling out the data
	 * @throws IOException
	 *             If an error occurs when writing the data to the stream.
	 * @throws SAXException
	 *             If an error occurs marshalling out the data
	 */
	public InputStream writeInfoToStream() throws JAXBException, IOException, SAXException {
		return this.writer.writeTrackedConfigurationToStream(information);
	}

}
