package com.soa.rs.discordbot.bot.events;

import java.time.Duration;
import java.time.LocalDateTime;

import com.soa.rs.discordbot.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.util.SoaDiscordBotConstants;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * The SoaBotInfoEvent displays general information about the bot.
 */
public class SoaBotInfoEvent extends AbstractSoaMsgRcvEvent {

	/**
	 * The version of the bot - this is pulled from the Jar Manifest
	 */
	private String version = this.getClass().getPackage().getImplementationVersion();

	/**
	 * Constructor
	 * 
	 * @param event
	 *            MessageReceivedEvent
	 */
	public SoaBotInfoEvent(MessageReceivedEvent event) {
		super(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.soa.rs.discordbot.bot.events.AbstractSoaMsgRcvEvent#executeEvent()
	 */
	@Override
	public void executeEvent() {

		LocalDateTime launch = DiscordCfgFactory.getInstance().getLaunchTime();
		LocalDateTime now = LocalDateTime.now();

		Duration uptime = Duration.between(launch, now);

		StringBuilder sb = new StringBuilder();
		sb.append("Hi there!  I'm the " + DiscordCfgFactory.getConfig().getGuildAbbreviation() + " bot!\n");
		sb.append("My Version: " + version);
		sb.append("\n");
		sb.append("My Uptime: " + uptime.toDays() + "days, " + (uptime.toHours() % 24) + "hours, "
				+ (uptime.toMinutes() % 60) + "minutes");
		sb.append("\n");
		sb.append("Info on me can be found on the forums: <" + SoaDiscordBotConstants.FORUMTHREAD_URL + ">");
		sb.append("\n");
		sb.append("The source for me can be found on GitHub: <" + SoaDiscordBotConstants.GITHUB_URL + ">");

		SoaClientHelper.sendMsgToChannel(getEvent().getMessage().getChannel(), sb.toString());

	}

}
