package com.soa.rs.discordbot.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * This utility class provides simple access to functions which interact with
 * Discord's API that may be rate limited. RequestBuffers are used to ensure
 * actions fired by various events are completed successfully, as if a RateLimit
 * is encountered the request will be tried again when the RateLimit has
 * expired.
 *
 */
public class SoaClientHelper {

	/**
	 * Sends a message to a channel
	 *
	 * @param channel
	 *            The channel to receive the message
	 * @param msg
	 *            The message to send to the channel
	 */
	public static void sendMsgToChannel(IChannel channel, String msg) {
		RequestBuffer.request(() -> {
			try {
				channel.sendMessage(msg);
			} catch (MissingPermissionsException | DiscordException e) {
				SoaLogging.getLogger().error("Error sending message: " + e.getMessage(), e);
			}
		});
	}

	/**
	 * Sends a message to a user
	 *
	 * @param userId
	 *            The user's Discord ID as a Long
	 * @param client
	 *            The client object which represents the bot
	 * @param msg
	 *            The message to send to the channel
	 */
	public static void sendMessageToUser(Long userId, IDiscordClient client, String msg) {
		RequestBuffer.request(() -> {
			try {
				IUser user = client.getUserByID(userId);
				user.getOrCreatePMChannel().sendMessage(msg);
			} catch (MissingPermissionsException | DiscordException e) {
				SoaLogging.getLogger().error("Error sending message: " + e.getMessage(), e);
			}
		});
	}

	/**
	 * Sends a message and a file to a channel
	 *
	 * @param channel
	 *            The channel to receive the message
	 * @param msg
	 *            The message to send to the channel
	 * @param stream
	 *            An InputStream containing the file data
	 * @param fileName
	 *            The name the file should have when uploaded to Discord
	 */
	public static void sendMsgWithFileToChannel(IChannel channel, String msg, InputStream stream, String fileName) {
		RequestBuffer.request(() -> {
			try {
				channel.sendFile(msg, stream, fileName);
			} catch (MissingPermissionsException | DiscordException e) {
				SoaLogging.getLogger().error("Error sending message: " + e.getMessage(), e);
			}
		});
	}

	/**
	 * Sends a message and a file to a channel.  This method expects a URL which it will download and then upload.
	 *
	 * @param channel  The channel to receive the message
	 * @param msg      The message to send to the channel
	 * @param url      A URL to the file data
	 * @param fileName The name the file should have when uploaded to Discord
	 */
	public static void sendMsgWithFileToChannel(IChannel channel, String msg, String url, String fileName) {
		RequestBuffer.request(() -> {
			try (InputStream stream = FileDownloader.downloadFile(url)) {
				channel.sendFile(msg, stream, fileName);
			} catch (MissingPermissionsException | DiscordException e) {
				SoaLogging.getLogger().error("Error sending message: " + e.getMessage(), e);
			} catch (Exception e) {
				//Something wrong with the URL, just send the message
				sendMsgToChannel(channel, msg);
			}
		});
	}

	/**
	 * Sends a message and a file to a user via private message
	 *
	 * @param userId
	 *            The user's Discord ID as a Long
	 * @param client
	 *            The client object which represents the bot
	 * @param msg
	 *            The message to send to the channel
	 * @param stream
	 *            An InputStream containing the file data
	 * @param fileName
	 *            The name the file should have when uploaded to Discord
	 */
	public static void sendMsgWithFileToUser(Long userId, IDiscordClient client, String msg, InputStream stream,
			String fileName) {
		RequestBuffer.request(() -> {
			try {
				IUser user = client.getUserByID(userId);
				user.getOrCreatePMChannel().sendFile(msg, stream, fileName);
			} catch (MissingPermissionsException | DiscordException e) {
				SoaLogging.getLogger().error("Error sending message: " + e.getMessage(), e);
			}
		});
	}

	/**
	 * Sends an embed to a channel
	 *
	 * @param channel
	 *            The channel to send the message to
	 * @param msg
	 *            The embed to be sent to the channel
	 */
	public static void sendEmbedToChannel(IChannel channel, EmbedBuilder msg) {
		RequestBuffer.request(() -> {
			try {
				channel.sendMessage(msg.build());
			} catch (MissingPermissionsException | DiscordException e) {
				SoaLogging.getLogger().error("Error sending message: " + e.getMessage(), e);
			}
		});
	}

	/**
	 * Formats a user's Discord name in the format of @user#name, which is the
	 * format Discord uses for names.
	 *
	 * @param user
	 *            The user for whom to get the name for
	 * @return The formatted name
	 */
	public static String getDiscordUserNameForUser(IUser user) {
		StringBuilder sb = new StringBuilder();
		sb.append("@");
		sb.append(user.getName());
		sb.append("#");
		sb.append(user.getDiscriminator());

		return sb.toString();
	}

	/**
	 * Deletes a message from a channel
	 *
	 * @param msg
	 *            The message to be deleted.
	 */
	public static void deleteMessageFromChannel(IMessage msg) {
		RequestBuffer.request(() -> {
			try {
				msg.delete();
			} catch (MissingPermissionsException | DiscordException e) {
				SoaLogging.getLogger().error("Error deleting message: " + e.getMessage(), e);
			}
		});
	}

	/**
	 * Sets the bot's avatar
	 *
	 * @param client
	 *            The client object which represents the bot
	 * @param img
	 *            The image to set as the bot's avatar
	 */
	public static void setBotAvatar(IDiscordClient client, Image img) {
		RequestBuffer.request(() -> {
			try {
				client.changeAvatar(img);
			} catch (DiscordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	/**
	 * Sets the bot's account name
	 *
	 * @param client
	 *            The client object which represents the bot
	 * @param name
	 *            The desired account name
	 */
	public static void setBotName(IDiscordClient client, String name) {
		RequestBuffer.request(() -> {
			try {
				client.changeUsername(name);
			} catch (DiscordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	/**
	 * Sets the bot's 'playing' status
	 *
	 * @param client
	 *            The client object which represents the bot
	 * @param playing
	 *            The string containing the name of the game to show as being played
	 */
	public static void setBotPlaying(IDiscordClient client, String playing) {
		RequestBuffer.request(() -> {
			client.changePlayingText(playing);
		});
	}

	/**
	 * Checks if the user who sent the message has the appropriate rank
	 *
	 * @param msg
	 *            The message sent by the user
	 * @param roleString
	 *            A string containing the role to check for
	 * @return True if the user has the rank, false otherwise
	 */
	public static boolean isRank(IMessage msg, String roleString) {
		return isRank(msg, msg.getGuild(), roleString);
	}

	/**
	 * Checks if the user who sent the message has the appropriate rank
	 *
	 * @param msg
	 *            The message sent by the user
	 * @param guild
	 *            The guild to check for
	 * @param roleString
	 *            A string containing the role to check for
	 * @return True if the user has the rank, false otherwise
	 */
	public static boolean isRank(IMessage msg, IGuild guild, String roleString) {
		List<String> roleStrings = new ArrayList<String>();
		roleStrings.add(roleString);
		return isRank(msg, guild, roleStrings);
	}

	/**
	 * Checks if the user who sent the message has the appropriate rank
	 *
	 * @param msg
	 *            The message sent by the user
	 * @param roleStrings
	 *            A list of roles to check if the user has
	 * @return True if the user has one of the roles, false otherwise
	 */
	public static boolean isRank(IMessage msg, List<String> roleStrings) {
		return isRank(msg, msg.getGuild(), roleStrings);

	}

	/**
	 * Checks if the user who sent the message has the appropriate rank
	 *
	 * @param msg
	 *            The message sent by the user
	 * @param guild
	 *            The guild to check for
	 * @param roleStrings
	 *            A list of roles to check if the user has
	 * @return True if the user has one of the roles, false otherwise
	 */
	public static boolean isRank(IMessage msg, IGuild guild, List<String> roleStrings) {
		List<IRole> roleListing = new LinkedList<IRole>(msg.getAuthor().getRolesForGuild(guild));
		Iterator<IRole> roleIterator = roleListing.iterator();

		while (roleIterator.hasNext()) {
			IRole role = roleIterator.next();
			for (String roleString : roleStrings) {
				if (role.getName().equalsIgnoreCase(roleString))
					return true;
			}
		}
		return false;
	}

	/**
	 * Formats a list of roles into a comma separated list in a string
	 *
	 * @param roles
	 *            The list of roles
	 * @return A string listing each role, separated by a comma.
	 */
	public static String translateRoleList(List<String> roles) {
		Iterator<String> iter = roles.iterator();
		StringBuilder sb = new StringBuilder();
		while (iter.hasNext()) {
			sb.append(iter.next());
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	/**
	 * Finds a guild with the provided name
	 *
	 * @param client Client to search in
	 * @param name Name of guild to search for
	 * @return Guild object for that guild
	 */
	public static IGuild findGuildByName(IDiscordClient client, String name)
	{
		List<IGuild> guilds = client.getGuilds();
		for(IGuild guild : guilds)
		{
			if(guild.getName().equals(name))
				return guild;
		}
		return null;
	}

}
