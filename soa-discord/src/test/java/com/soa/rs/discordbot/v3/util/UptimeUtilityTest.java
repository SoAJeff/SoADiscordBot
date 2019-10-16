package com.soa.rs.discordbot.v3.util;

import java.time.Duration;
import java.time.LocalDateTime;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;

import org.junit.Assert;
import org.junit.Test;

public class UptimeUtilityTest {

	@Test
	public void testUtility() {
		LocalDateTime ldt = LocalDateTime.now().minusMinutes(5);
		DiscordCfgFactory.getInstance().setLaunchTime(ldt);

		UptimeUtility utility = new UptimeUtility();
		Duration duration = utility.getUptime();

		Assert.assertEquals(5, duration.toMinutes());
	}
}
