package com.soa.rs.discordbot.bot.events;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.soa.rs.discordbot.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.jaxb.Actions;
import com.soa.rs.discordbot.jaxb.CurrentUser;
import com.soa.rs.discordbot.jaxb.LeftUser;
import com.soa.rs.discordbot.jaxb.RecentAction;
import com.soa.rs.discordbot.jaxb.RecentActions;
import com.soa.rs.discordbot.jaxb.User;
import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.util.SoaLogging;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

/**
 * The UserTrackingQuery class handles the user interaction with the tracking
 * database. This includes allowing any user to search for a user, and adding a
 * known user name to a user if the user has the appropriate rank.
 */
public class UserTrackingQuery extends AbstractSoaMsgRcvEvent {

	/**
	 * Event arguments
	 */
	private String[] args;

	/**
	 * Default constructor
	 * 
	 * @param event
	 *            The event that was received
	 */
	public UserTrackingQuery(MessageReceivedEvent event) {
		super(event);
	}

	/**
	 * Sets the event arguments
	 * 
	 * @param args
	 *            The event arguments
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}

	/**
	 * Executes the event. This event will parse the argument passed in and invoke
	 * the appropriate method to complete the event.
	 */
	@Override
	public void executeEvent() {
		if (args.length >= 2) {
			if (args[1].equalsIgnoreCase("setKnownUser") || args[1].equalsIgnoreCase("setKnownName")) {
				addKnownNameToUser();
			} else if (args[1].equalsIgnoreCase("search")) {
				searchUsers();
			} else if (args[1].equalsIgnoreCase("sendfile")) {
				sendTrackingFileToUser();
			} else if (args[1].equalsIgnoreCase("getrecentactions")) {
				postRecentActions();
			} else {
				sendHelp();
			}
		} else {
			sendHelp();
		}
	}

	/**
	 * Searches the user database for the search term specified in the argument.
	 * Returns the results within Discord Embeds.
	 */
	private void searchUsers() {
		String searchTerm = null;
		long id = DiscordCfgFactory.getConfig().getDefaultGuildId();
		Map<User, Long> resultSet = null;
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

		searchTerm = getEvent().getMessage().getContent().substring(13);
		if (searchTerm.contains("-server")) {
			id = getServerId(searchTerm);
			searchTerm = searchTerm.substring(0, searchTerm.indexOf("-server") - 1);
			SoaLogging.getLogger().debug("Searchterm updated to " + searchTerm);
		}
		if (id == 0) {
			SoaLogging.getLogger().error("User searched for invalid server");
			SoaClientHelper.sendMsgToChannel(getEvent().getChannel(),
					"We didn't recognize the server that was entered");
			return;
		}
		SoaLogging.getLogger().info("Search term: " + searchTerm);
		resultSet = UserTrackingUpdater.getInstance().searchUsers(searchTerm, false);
		resultSet.putAll(UserTrackingUpdater.getInstance().searchLeftUsers(searchTerm));

		if (resultSet.size() == 0) {
			if (!getEvent().getMessage().getMentions().isEmpty())
				searchTerm = getMentionedUser();
			if (searchTerm != null) {
				resultSet = UserTrackingUpdater.getInstance().searchUsers(searchTerm, false);
				resultSet.putAll(UserTrackingUpdater.getInstance().searchLeftUsers(searchTerm));
			}
		}

		if (resultSet.size() > 0) {
			Iterator<Entry<User, Long>> mapIter = resultSet.entrySet().iterator();
			while (mapIter.hasNext()) {
				Map.Entry<User, Long> entry = mapIter.next();
				if (entry.getValue().longValue() == id) {
					EmbedBuilder builder = new EmbedBuilder();

					User user = entry.getKey();

					if (user instanceof CurrentUser) {
						builder.withTitle(getEvent().getClient().getUserByID(user.getUserId())
								.getDisplayName(getEvent().getClient().getGuildByID(entry.getValue().longValue())));
					} else {
						builder.withTitle(user.getUserName());
					}
					builder.withDesc(user.getUserName() + " in  server: "
							+ getEvent().getClient().getGuildByID(entry.getValue().longValue()).getName());
					if (user.getKnownName() != null) {
						builder.appendField("Known Name", user.getKnownName(), false);
					}
					sb.setLength(0);
					Iterator<String> iter = user.getDisplayNames().getDisplayName().iterator();
					int nameCount = 0;
					while (iter.hasNext()) {
						String name = iter.next();
						sb.append(name);
						nameCount++;
						if (sb.length() > 975) {
							sb.append(", and " + (user.getDisplayNames().getDisplayName().size() - nameCount)
									+ " additional names.");
							break;
						}
						if (iter.hasNext()) {
							sb.append(", ");
						}
					}
					builder.appendField("All display names", sb.toString(), false);

					if (user instanceof CurrentUser) {
						builder.appendField("Joined server date",
								sdf.format(((CurrentUser) user).getJoined().toGregorianCalendar().getTime()), true);
						builder.appendField("Last seen date",
								sdf.format(((CurrentUser) user).getLastOnline().toGregorianCalendar().getTime()), true);
					} else if (user instanceof LeftUser) {
						builder.appendField("Left server date",
								sdf.format(((LeftUser) user).getLeft().toGregorianCalendar().getTime()), false);
					}
					// Send Embed

					SoaClientHelper.sendEmbedToChannel(getEvent().getChannel(), builder);
				}

			}
		} else {
			SoaClientHelper.sendMsgToChannel(getEvent().getChannel(), "Your search returned 0 results.");
		}

	}

	/**
	 * Sends the tracking file to the requesting user if they have the correct rank.
	 */
	private void sendTrackingFileToUser() {
		if (SoaClientHelper.isRank(getEvent().getMessage(),
				getEvent().getClient().getGuildByID(DiscordCfgFactory.getConfig().getDefaultGuildId()),
				DiscordCfgFactory.getConfig().getUserTrackingEvent().getCanUpdateQuery().getRole())) {
			try {
				InputStream stream = UserTrackingUpdater.getInstance().writeInfoToStream();
				if (stream != null) {
					SoaClientHelper.sendMsgWithFileToUser(getEvent().getAuthor().getLongID(), getEvent().getClient(),
							"File containing all user tracked information attached!", stream, "userInfo.xml");
				}
			} catch (Exception e) {
				SoaLogging.getLogger().error("There was an error sending the data to the user.", e);
			}
		}

	}

	/**
	 * Adds a known name to a user if the requesting user has the appropriate rank.
	 */
	private void addKnownNameToUser() {
		if (SoaClientHelper.isRank(getEvent().getMessage(),
				getEvent().getClient().getGuildByID(DiscordCfgFactory.getConfig().getDefaultGuildId()),
				DiscordCfgFactory.getConfig().getUserTrackingEvent().getCanUpdateQuery().getRole())) {
			try {
				String search = null;
				String name = null;
				int i = 2;

				StringBuilder sb = new StringBuilder();

				while (i < args.length) {
					if (args[i].equalsIgnoreCase("-search")) {
						i++;
						while (args.length > i && !args[i].equalsIgnoreCase("-name")) {
							sb.append(args[i]);
							sb.append(" ");
							i++;
						}
						search = sb.toString().trim();
						sb.setLength(0);
					} else if (args[i].equalsIgnoreCase("-name")) {
						i++;
						while (args.length > i && !args[i].equalsIgnoreCase("-search")) {
							sb.append(args[i]);
							sb.append(" ");
							i++;
						}
						name = sb.toString().trim();
						sb.setLength(0);

					}
				}

				if (search == null || name == null) {
					SoaClientHelper.sendMsgToChannel(getEvent().getChannel(), "Arguments provided were invalid.");
					return;
				} else if (search.startsWith("<@") && search.endsWith(">")) {
					search = getMentionedUser();
					if (search == null) {
						SoaClientHelper.sendMsgToChannel(getEvent().getChannel(), "Arguments provided were invalid.");
					}
				}

				if (search != null) {
					boolean updated = UserTrackingUpdater.getInstance().addKnownNameToUser(search, name);
					if (updated) {
						SoaClientHelper.sendMsgToChannel(getEvent().getChannel(), "User's known name updated");
					} else {
						SoaClientHelper.sendMsgToChannel(getEvent().getChannel(), "Was not able to find user");
					}
				}
			} catch (Exception e) {
				SoaClientHelper.sendMsgToChannel(getEvent().getChannel(), "An error occurred: " + e.getMessage());
				SoaLogging.getLogger().error("Error occurred in KnownUser", e);
			}
		} else {
			SoaClientHelper.sendMsgToChannel(getEvent().getChannel(),
					"You do not have the role to perform this action");
		}
	}

	/**
	 * Displays Recent Actions for a server
	 */
	private void postRecentActions() {
		if (SoaClientHelper.isRank(getEvent().getMessage(),
				getEvent().getClient().getGuildByID(DiscordCfgFactory.getConfig().getDefaultGuildId()),
				DiscordCfgFactory.getConfig().getUserTrackingEvent().getCanUpdateQuery().getRole())) {
			long guildID = DiscordCfgFactory.getConfig().getDefaultGuildId();
			if (getEvent().getMessage().getContent().contains("-server")) {
				guildID = getServerId(getEvent().getMessage().getContent());
			}
			RecentActions actions = UserTrackingUpdater.getInstance().getRecentActionsForGuild(guildID);
			StringBuilder sb = new StringBuilder();
			sb.append("**Recent Actions for guild: " + getEvent().getClient().getGuildByID(guildID).getName() + "**\n");
			if (actions.getRecentAction().size() == 0) {
				sb.append("No actions recorded for this guild");
			} else {
				for (RecentAction action : actions.getRecentAction()) {
					sb.append(action.getUser() + getStringForAction(action.getAction()));
					if (action.getAction() == Actions.CHANGED_DISPLAY_NAME) {
						sb.append(action.getOriginalValue() + " to " + action.getChangedValue() + ".\n");
					} else if (action.getAction() == Actions.CHANGED_USER_HANDLE) {
						sb.append(action.getChangedValue() + ".\n");
					}
				}
			}
			SoaClientHelper.sendMsgToChannel(getEvent().getChannel(), sb.toString());
		}
	}

	/**
	 * Gets an appropriate string for the action
	 * 
	 * @param action
	 *            Action for which to return a string
	 * @return String representation of the action.
	 */
	private String getStringForAction(Actions action) {
		String value = null;
		switch (action) {
		case JOINED_SERVER:
			value = " has joined the server.\n";
			break;
		case LEFT_SERVER:
			value = " has left the server.\n";
			break;
		case CHANGED_DISPLAY_NAME:
			value = " has changed their display name from ";
			break;
		case CHANGED_USER_HANDLE:
			value = " has changed their user handle to ";
			break;
		default:
			break;
		}
		return value;
	}

	/**
	 * Displays the help menu
	 */
	private void sendHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("```Help: User Tracking\n");
		sb.append(
				".user search <searchphrase> [-server <servername>] - Searches and displays information about the searched user.  Optionally, the server name of the server to be searched for can be provided.  If not provided, the configured default server is used.\n");
		sb.append(
				".user setKnownName (or .user setKnownUser) -name <name> -search <search> - Adds a recognizable name <name> to the user identified in <search>.  Search must match a user's name exactly.\n");
		sb.append(".user sendfile - Sends a copy of the tracking XML file to the user.\n");
		sb.append(
				".user getRecentActions [-server <servername>] - Lists out the last 15 tracking events for the guild.\n");
		sb.append("\n");
		sb.append(
				"Note: sendFile, setKnownName, and getRecentActions may only be run by the following users: "
						+ SoaClientHelper.translateRoleList(
								DiscordCfgFactory.getConfig().getUserTrackingEvent().getCanUpdateQuery().getRole())
						+ "\n");
		sb.append("```");

		SoaClientHelper.sendMsgToChannel(getEvent().getMessage().getChannel(), sb.toString());
	}

	/**
	 * Retrieves the first mentioned user if one was deemed to be mentioned.
	 * 
	 * @return The name of the mentioned user in the format of @name#discriminator.
	 */
	private String getMentionedUser() {
		List<IUser> mentionedUsers = getEvent().getMessage().getMentions();
		String name = null;

		if (!mentionedUsers.isEmpty()) {
			SoaLogging.getLogger().info(
					"Checking mentioned users: " + SoaClientHelper.getDiscordUserNameForUser(mentionedUsers.get(0)));
			name = SoaClientHelper.getDiscordUserNameForUser(mentionedUsers.get(0));
		}
		return name;
	}

	/**
	 * If a server has been provided, fetch the ID
	 * 
	 * @param searchTerm
	 *            The search string
	 * @return long ID of the server, or 0 if the server is not one the bot is
	 *         connected to.
	 */
	private long getServerId(String searchTerm) {
		int startindex = searchTerm.indexOf("-server");
		String serverName = searchTerm.substring(startindex + 8);
		IGuild guild = SoaClientHelper.findGuildByName(getEvent().getClient(), serverName);
		if (guild != null)
			return guild.getLongID();
		return 0;
	}

}
