package com.soa.rs.discordbot.v3.commands;

import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.SettingsUtility;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.InteractionFollowupCreateMono;
import reactor.core.publisher.Mono;

@Interaction(trigger = "clancompareupdatealtcompid")
public class ClanCompareUpdateAltCompIdInteraction extends AbstractCommand {

	private SettingsUtility settingsUtility;
	private final String CLANCOMPARE_SETTING = "clancompare.altId" ;
	@Override
	public void initialize() {
		if (DiscordCfgFactory.getConfig().getClanCompareEvent() != null && DiscordCfgFactory.getConfig().getClanCompareEvent().isEnabled()) {
			setEnabled(DiscordCfgFactory.getConfig().getClanCompareEvent().isEnabled());
			this.settingsUtility = new SettingsUtility();
		}
		else {
			setEnabled(false);
		}
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return null;
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		long compid = event.getOption("compid").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asLong).get();
		String compidString = Long.toString(compid);
		return event.deferReply().withEphemeral(true).then(Mono.fromCallable(()->settingsUtility.updateValueForKey(CLANCOMPARE_SETTING, compidString)).flatMap(
				i->sendUpdateMessage(i, event))).then();
	}

	private InteractionFollowupCreateMono sendUpdateMessage(Integer i, ChatInputInteractionEvent event) {
		if(i == 1)
		{
			return event.createFollowup("Value updated.").withEphemeral(true);
		}
		else {
			return event.createFollowup("Failed to update value.").withEphemeral(true);
		}
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ButtonInteractionEvent event) {
		return Mono.empty();
	}
}
