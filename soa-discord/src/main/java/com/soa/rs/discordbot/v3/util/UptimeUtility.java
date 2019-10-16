package com.soa.rs.discordbot.v3.util;

import java.time.Duration;
import java.time.LocalDateTime;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;

public class UptimeUtility {

	public static Duration getUptime()
	{
		LocalDateTime launch = DiscordCfgFactory.getInstance().getLaunchTime();
		LocalDateTime now = LocalDateTime.now();

		return Duration.between(launch, now);
	}
}
