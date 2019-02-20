package com.soa.rs.discordbot.bot.events;

import com.soa.rs.discordbot.util.NoDefinedRolesException;
import com.soa.rs.discordbot.util.SoaLogging;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

/**
 * The user ban event allows an admin to issue a ban via a command and tagging the user.
 * The user will be added to the ban list with a default reason and 7 days of history remove.
 * This event does not have its own permission set and instead will use the permissions
 * of the admin event due to their similarity in nature.
 */
public class UserBanEvent extends AbstractSoaMsgRcvEvent {

	/**
	 * Constructor
	 *
	 * @param event MessageReceivedEvent
	 */
	public UserBanEvent(MessageReceivedEvent event) {
		super(event);
	}

	@Override
	public void executeEvent() {
		try {
			if (permittedToExecuteEvent())
				if (getEvent().getMessage().getMentions().size() > 0) {
					for (IUser user : getEvent().getMessage().getMentions()) {
						SoaLogging.getLogger().info("Banning user @" + user.getName() + "#" + user.getDiscriminator()
								+ "from the server " + getEvent().getGuild().getName() + ", as requested by @"
								+ getEvent().getMessage().getAuthor().getName() + "#" + getEvent().getMessage()
								.getAuthor().getDiscriminator());
						//Ban user, delete 7 days of history
						RequestBuffer.request(
								() -> getEvent().getGuild().banUser(user, "Ban issued via admin ban request.", 7));
						//Delete our message asking for the ban
						RequestBuffer.request(() -> getEvent().getMessage().delete());
					}
				}
		} catch (NoDefinedRolesException e) {
			e.printStackTrace();
		}

	}
}
