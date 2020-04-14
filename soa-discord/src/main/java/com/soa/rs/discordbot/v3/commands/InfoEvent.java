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
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.DiscordUtils;
import com.soa.rs.discordbot.v3.util.SoaDiscordBotConstants;
import com.soa.rs.discordbot.v3.util.UptimeUtility;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Command(triggers = { ".info", ".debug" })
public class InfoEvent extends AbstractCommand {

	private String version = this.getClass().getPackage().getImplementationVersion();
	private static final int MB_UNIT = 1024 * 1024;

	@Override
	public void initialize() {
		addHelpMsg(".info", "Provides information regarding the bot.");
		setEnabled(true);
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		Duration uptime = UptimeUtility.getUptime();

		final Runtime runtime = Runtime.getRuntime();
		final long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / MB_UNIT;
		final long maxMemory = runtime.maxMemory() / MB_UNIT;

		StringBuilder sb = new StringBuilder();
		sb.append("Hi there!  I'm the " + DiscordCfgFactory.getConfig().getGuildAbbreviation() + " bot!\n");
		sb.append("My Version: " + version);
		sb.append("\n");
		sb.append("My Uptime: " + uptime.toDays() + " days, " + (uptime.toHours() % 24) + " hours, " + (uptime.toMinutes()
				% 60) + " minutes");
		sb.append("\n");
		if (event.getMessage().getContent().startsWith(".debug")) {
			sb.append("\nJVM Statistics:");
			sb.append(String.format("%nMemory: %s/%s MB", formatNumber(usedMemory), formatNumber(maxMemory)));
			sb.append(String.format("%nCPU Usage: %.1f%%", getProcessCpuLoad()));
			sb.append(String.format("%nThreads: %s%n%n", formatNumber(Thread.activeCount())));

		}
		sb.append("Info on me can be found on the forums: <" + SoaDiscordBotConstants.FORUMTHREAD_URL + ">");
		sb.append("\n");
		sb.append("The source for me can be found on GitHub: <" + SoaDiscordBotConstants.GITHUB_URL + ">");
		return event.getMessage().getChannel().flatMap(messageChannel -> DiscordUtils.sendMessage(sb.toString(), messageChannel)).then();
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
