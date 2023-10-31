package com.soa.rs.discordbot.v3.commands;

import java.util.ArrayList;
import java.util.List;

import com.github.soajeff.clancompare.ClanCompareProcessor;
import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.SettingsUtility;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Interaction(trigger = "clancompare")
public class ClanCompareInteraction extends AbstractCommand {

	private String clanFetchUrl;
	private String clanFetchApiKey;
	private String rsClanFetchUrl;
	private String altFetchUrl;
	private SettingsUtility settingsUtility;

	@Override
	public void initialize() {
		if (DiscordCfgFactory.getConfig().getClanCompareEvent() != null && DiscordCfgFactory.getConfig().getClanCompareEvent().isEnabled()) {
			setEnabled(DiscordCfgFactory.getConfig().getClanCompareEvent().isEnabled());
			this.clanFetchUrl = DiscordCfgFactory.getConfig().getClanCompareEvent().getForumsApiUrl();
			this.clanFetchApiKey = DiscordCfgFactory.getConfig().getClanCompareEvent().getForumsApiKey();
			this.rsClanFetchUrl = DiscordCfgFactory.getConfig().getClanCompareEvent().getRsClanUrl();
			this.altFetchUrl = DiscordCfgFactory.getConfig().getClanCompareEvent().getCompetitionsUrl();
			this.settingsUtility = new SettingsUtility();
		} else {
			setEnabled(false);
		}
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return null;
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		return event.deferReply().withEphemeral(true)
				.then(Mono.fromCallable(() -> settingsUtility.getValueForKey("clancompare.altId"))
						.map(Integer::parseInt).flatMap(i -> Mono.fromCallable(() -> processClanCompare(i)))
						.onErrorResume(err -> {
							String error = "Error encountered during clan compare operation.";
							List<String> e = new ArrayList<>();
							e.add(error);
							SoaLogging.getLogger(this).error(error, err);
							return Mono.just(e);
						}).flatMapMany(Flux::fromIterable)
						.flatMapSequential(s -> event.createFollowup(s).withEphemeral(true)).then()).then();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return null;
	}

	public List<String> processClanCompare(int altId)
	{
		ClanCompareProcessor processor = new ClanCompareProcessor(clanFetchUrl, clanFetchApiKey, rsClanFetchUrl, altFetchUrl, altId);
		List<String> processResult =  processor.process();

		List<String> response = new ArrayList<>();

		if(processResult.isEmpty())
		{
			response.add("Clan Compare found no deltas between the forums and the in-game clan.");
			return response;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Clan Compare Results:");
		sb.append("\n");
		for(String s : processResult)
		{
			sb.append("* ");
			sb.append(s);
			sb.append("\n");
			if(sb.length() > 1500)
			{
				response.add(sb.toString().trim());
				sb.setLength(0);
			}
		}
		response.add(sb.toString().trim());
		return response;
	}
}
