package com.soa.rs.discordbot.bot.events;

import com.soa.rs.discordbot.util.SoaClientHelper;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

public class OldWikiEvent extends AbstractSoaMsgRcvEvent {

	private static final String FANDOM = "runescape.fandom.com";
	private static final String WIKIA = "runescape.wikia.com";
	private static final String CURRENT_URL = "https://runescape.wiki/";

	/**
	 * Constructor
	 *
	 * @param event MessageReceivedEvent
	 */
	public OldWikiEvent(MessageReceivedEvent event) {
		super(event);
	}

	@Override
	public void executeEvent() {
		String message = this.getEvent().getMessage().getContent();

		if (message.contains(FANDOM) || message.contains(WIKIA)) {
			IUser user = getEvent().getAuthor();
			StringBuilder sb = new StringBuilder();
			sb.append(user.mention());
			sb.append(", your link referenced the Old RuneScape Wiki! That site is no longer maintained.");
			sb.append("\n");
			sb.append("Instead, use the new RuneScape Wiki: ");
			sb.append(CURRENT_URL);
			SoaClientHelper.sendMsgToChannel(getEvent().getChannel(), sb.toString());
		}
	}
}
