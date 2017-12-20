package com.soa.rs.discordbot.bot.events;

import com.soa.rs.discordbot.util.SoaClientHelper;
import com.soa.rs.discordbot.cfg.DiscordCfgFactory;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * The SoaHelpEvent displays a help message of the bot's commands
 */
public class SoaHelpEvent extends AbstractSoaMsgRcvEvent {

	/**
	 * Constructor
	 * 
	 * @param event
	 *            MessageReceivedEvent
	 */
	public SoaHelpEvent(MessageReceivedEvent event) {
		super(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.soa.rs.discordbot.bot.events.AbstractSoaMsgRcvEvent#executeEvent()
	 */
	@Override
	public void executeEvent() {
		StringBuilder sb = new StringBuilder();
		sb.append("```Help: ");
		sb.append(DiscordCfgFactory.getConfig().getGuildAbbreviation());
		sb.append(" Commands\n");
		sb.append(".events - Displays an up-to-date listing of today's events.\n");
		sb.append(".info - Displays bot info.\n");
		sb.append(".music - Use .music help for music commands.\n");
		sb.append(".trivia - Use .trivia help for the trivia commands.\n");
		sb.append(".user - Use .user help for the user tracking commands.\n");
		sb.append("```");

		SoaClientHelper.sendMsgToChannel(getEvent().getMessage().getChannel(), sb.toString());
	}
}
