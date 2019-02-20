package com.soa.rs.discordbot.bot.events;

import com.soa.rs.discordbot.util.SoaClientHelper;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class BenIsNoobEvent extends AbstractSoaMsgRcvEvent {

	public BenIsNoobEvent(MessageReceivedEvent event)
	{
		super(event);
	}

	@Override
	public void executeEvent() {
		SoaClientHelper.sendMsgToChannel(getEvent().getChannel(), "Ben is a noob");
	}
}
