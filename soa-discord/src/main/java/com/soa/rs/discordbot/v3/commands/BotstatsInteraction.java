package com.soa.rs.discordbot.v3.commands;

import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Locale;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.util.UptimeUtility;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Command(triggers = {})
@Interaction(trigger="botstats")
public class BotstatsInteraction extends AbstractCommand {

	private static final int MB_UNIT = 1024 * 1024;

	@Override
	public void initialize() {
		setEnabled(true);
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		Duration uptime = UptimeUtility.getUptime();

		final Runtime runtime = Runtime.getRuntime();
		final long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / MB_UNIT;
		final long maxMemory = runtime.maxMemory() / MB_UNIT;

		StringBuilder sb = new StringBuilder();
		sb.append("\nJVM Statistics:");
		sb.append(String.format("%nMemory: %s/%s MB", formatNumber(usedMemory), formatNumber(maxMemory)));
		sb.append(String.format("%nCPU Usage: %.1f%%", getProcessCpuLoad()));
		sb.append(String.format("%nThreads: %s%n", formatNumber(Thread.activeCount())));
		sb.append("Uptime: " + uptime.toDays() + " days, " + (uptime.toHours() % 24) + " hours, " + (uptime.toMinutes()
				% 60) + " minutes");
		return event.reply().withEphemeral(true).withContent(sb.toString()).then();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ButtonInteractionEvent event) {
		return Mono.empty();
	}

	private String formatNumber(double number) {
		return NumberFormat.getNumberInstance(Locale.ENGLISH).format(number);
	}

	private double getProcessCpuLoad() {
		double cpuLoad;
		try {
			final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			final ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			final AttributeList list = mbs.getAttributes(name, new String[] { "ProcessCpuLoad" });

			if(list.isEmpty()) {
				return Double.NaN;
			}

			final Attribute att = (Attribute) list.get(0);
			final Double value = (Double) att.getValue();

			if(value == -1.0) {
				return Double.NaN;
			}

			cpuLoad = value * 100d;
		} catch (InstanceNotFoundException | ReflectionException | MalformedObjectNameException err) {
			cpuLoad = Double.NaN;
		}

		return cpuLoad;
	}
}
