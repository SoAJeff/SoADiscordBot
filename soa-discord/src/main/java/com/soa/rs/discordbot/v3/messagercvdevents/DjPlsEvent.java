package com.soa.rs.discordbot.v3.messagercvdevents;

import java.util.Random;

import com.soa.rs.discordbot.v3.api.annotation.MessageRcv;
import com.soa.rs.discordbot.v3.api.command.MsgRcvd;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@MessageRcv(triggers = { "dj pls", "dj is a noob", "dj is a nublet" })
public class DjPlsEvent extends MsgRcvd {

	@Override
	public void initialize() {
		if (DiscordCfgFactory.getConfig().getDjPlsEvent() != null && DiscordCfgFactory.getConfig().getDjPlsEvent()
				.isEnabled()) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		Random rndm = new Random();
		int msg = rndm.nextInt(4) + 1;
		return event.getMessage().getChannel().flatMap(channel -> DiscordUtils.sendMessage(getMsg(msg), channel))
				.then();
	}

	private String getMsg(int msg) {
		switch (msg) {
		case 1:
			return "DJ pls";
		case 2:
			return "DJ is a noob";
		case 3:
			return "#blameDJ";
		case 4:
			return "Thank you for refreshing your twitter page";
		default:
			return "";
		}
	}
}
