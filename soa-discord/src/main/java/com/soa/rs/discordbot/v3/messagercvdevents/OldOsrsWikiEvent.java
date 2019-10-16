package com.soa.rs.discordbot.v3.messagercvdevents;

import com.soa.rs.discordbot.v3.api.annotation.MessageRcv;
import com.soa.rs.discordbot.v3.api.command.MsgRcvd;
import com.soa.rs.discordbot.v3.util.DiscordUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@MessageRcv(triggers = { "https://oldschoolrunescape.fandom.com", "https://oldschoolrunescape.wikia.com" })
public class OldOsrsWikiEvent extends MsgRcvd {

	private static final String CURRENT_URL = "https://oldschool.runescape.wiki/";

	@Override
	public void initialize() {
		setEnabled(true);
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		StringBuilder sb = new StringBuilder();
		event.getMessage().getAuthor().ifPresent(user -> sb.append(user.getMention()));
		sb.append(", your link referenced the old Old School RuneScape Wiki! That site is no longer maintained.");
		sb.append("\n");
		sb.append("Instead, use the new Old School RuneScape Wiki: ");
		sb.append(CURRENT_URL);
		return event.getMessage().getChannel().flatMap(channel -> DiscordUtils.sendMessage(sb.toString(), channel))
				.then();
	}
}
