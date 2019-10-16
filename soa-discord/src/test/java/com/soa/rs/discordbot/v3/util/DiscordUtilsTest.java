package com.soa.rs.discordbot.v3.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DiscordUtilsTest {

	//Verify old behavior matches new behavior using String.join
	@Test
	public void testJoinListOfRoles() {
		List<String> roles = Arrays.asList(SoaDiscordBotConstants.STAFF_ROLES);
		Iterator<String> iter = roles.iterator();
		StringBuilder sb = new StringBuilder();
		while (iter.hasNext()) {
			sb.append(iter.next());
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}

		Assert.assertEquals(sb.toString(), DiscordUtils.translateRoleList(roles));
	}
}
