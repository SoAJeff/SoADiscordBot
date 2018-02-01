package com.soa.rs.discordbot.bot.events;

import java.util.List;

import com.soa.rs.discordbot.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.util.NoDefinedRolesException;
import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.util.SoaLogging;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.Image;

/**
 * The SoaAdminEvent can be used for an admin (specified in the configuration)
 * to change the avatar or username of the bot while the bot is running, or to
 * make the bot speak within a channel.
 * <p>
 * <b>NOTE:</b> This event changes the username of the bot only; it does not
 * change the bot's display name in a server if the bot has a display name that
 * has been set differently than the username. It is expected the admin can
 * manually assign the display name in the server itself. Additionally, in some
 * servers, the bot may not be able to change its display name, but the bot can
 * always change its username.
 * <p>
 * <b>NOTE:</b> This event is not to be displayed within the Help menu printed
 * by <tt>SoaHelpEvent</tt>.
 */
public class SoaAdminEvent extends AbstractSoaMsgRcvEvent {

	/**
	 * The rest of the arguments entered for the message
	 */
	private String args[];

	/**
	 * Constructor
	 * 
	 * @param event
	 *            MessageReceivedEvent
	 */
	public SoaAdminEvent(MessageReceivedEvent event) {
		super(event);
	}

	/**
	 * Set the arguments
	 * 
	 * @param args
	 *            arguments
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.soa.rs.discordbot.bot.events.AbstractSoaMsgRcvEvent#executeEvent()
	 */
	@Override
	public void executeEvent() {
		try {
			if (permittedToExecuteEvent()) {
				String action = args[1];
				if (action.equalsIgnoreCase("news"))
					sendMessageToChannel();
				else if (action.equalsIgnoreCase("change-name"))
					changeBotName();
				else if (action.equalsIgnoreCase("change-avatar"))
					changeAvatar();
				else if (action.equalsIgnoreCase("change-presence-text"))
					changePresenceText();

			}
		} catch (NoDefinedRolesException e) {
			SoaLogging.getLogger().error(getEvent().getAuthor().getDisplayName(getEvent().getGuild())
					+ " attempted to execute an Admin Event but did not have the appropriate Role to do so.");
		}
	}

	/**
	 * Sends a message to the specified channel
	 */
	private void sendMessageToChannel() {
		String channelName = args[2];
		int i;
		StringBuilder sb = new StringBuilder();
		for (i = 3; i < args.length; i++) {
			sb.append(args[i] + " ");
		}

		IDiscordClient client = getEvent().getClient();
		List<IChannel> channels = client.getChannels();
		for (IChannel channel : channels) {
			if (channel.getName().equals(channelName)) {
				SoaClientHelper.sendMsgToChannel(channel, sb.toString());
			}
		}
	}

	/**
	 * Changes the bot's username. Does not affect the display name of the bot if
	 * the bot has an assigned display name in that server.
	 */
	private void changeBotName() {
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			sb.append(args[i] + " ");
		}
		SoaLogging.getLogger().info("Changing bot username to " + sb.toString());
		SoaClientHelper.setBotName(getEvent().getClient(), sb.toString());
	}

	/**
	 * Changes the bot's avatar
	 */
	private void changeAvatar() {
		String imageType = args[2].substring(args[2].lastIndexOf(".") + 1);
		SoaLogging.getLogger().info("Changing bot avatar to: " + args[2]);
		SoaClientHelper.setBotAvatar(getEvent().getClient(), Image.forUrl(imageType, args[2]));
	}

	/**
	 * Changes the user's presence text
	 */
	/*
	 * TODO Account for other presence types when they become available in 2.10
	 */
	private void changePresenceText() {
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			sb.append(args[i] + " ");
		}
		SoaLogging.getLogger().info("Setting presence text to " + sb.toString());
		SoaClientHelper.setBotPlaying(getEvent().getClient(), sb.toString());
		//Apply to the config so that it can be used in the case of a disconnection
		DiscordCfgFactory.getConfig().setDefaultStatus(sb.toString());
	}
}
