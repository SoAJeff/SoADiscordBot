package com.soa.rs.discordbot.v3.usertrack;

import java.util.ArrayList;
import java.util.List;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class LastSeenCacheTest {

	@Test
	public void addUserToCache() {
		LastSeenCache cache = new LastSeenCache();
		cache.addNewGuild(6789);
		cache.updateCacheForGuildUser(6789, 1234);

		Assert.assertEquals(1, cache.getCacheForGuild(6789).size());
		Assert.assertNotNull(cache.getCacheForGuild(6789).get(1234L));
	}

	@Test
	public void addUserToNullCache() {
		LastSeenCache cache = new LastSeenCache();

		cache.updateCacheForGuildUser(6789, 1234);

		try {
			cache.getCacheForGuild(6789);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(0, cache.numOfGuilds());
		}

	}

	@Test
	public void writeToDatabase() {
		LastSeenCache cache = new LastSeenCache();
		GuildUserUtility util = Mockito.mock(GuildUserUtility.class);

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		List<GuildUser> userList = new ArrayList<>();
		userList.add(user);

		Mockito.when(util.getGuildUser(1234, 6789)).thenReturn(userList);
		Mockito.doNothing().when(util).updateExistingUser(Mockito.any(GuildUser.class));

		cache.setGuildUserUtility(util);

		cache.addNewGuild(6789);
		cache.updateCacheForGuildUser(6789, 1234);

		cache.writeCacheToDatabase();

		Mockito.verify(util, Mockito.times(1)).updateLastSeenForUser(Mockito.anyLong(), Mockito.anyMap());
	}

	@Test
	public void writeZeroUsersToDatabase() {
		LastSeenCache cache = new LastSeenCache();
		GuildUserUtility util = Mockito.mock(GuildUserUtility.class);

		cache.setGuildUserUtility(util);

		cache.addNewGuild(6789);

		cache.writeCacheToDatabase();

		Mockito.verify(util, Mockito.times(0)).updateLastSeenForUser(Mockito.anyLong(), Mockito.anyMap());
	}

	@Test
	public void writeToDatabaseException() {
		LastSeenCache cache = new LastSeenCache();
		GuildUserUtility util = Mockito.mock(GuildUserUtility.class);

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		List<GuildUser> userList = new ArrayList<>();
		userList.add(user);

		Mockito.when(util.getGuildUser(1234, 6789)).thenReturn(userList);

		cache.addNewGuild(6789);
		cache.updateCacheForGuildUser(6789, 1234);

		cache.writeCacheToDatabase();

		Mockito.verify(util, Mockito.times(0)).updateLastSeenForUser(Mockito.anyLong(), Mockito.anyMap());
	}

	@Test
	public void writeToDatabaseNullUser() {
		LastSeenCache cache = new LastSeenCache();
		GuildUserUtility util = Mockito.mock(GuildUserUtility.class);

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		List<GuildUser> userList = new ArrayList<>();
		userList.add(null);

		Mockito.when(util.getGuildUser(1234, 6789)).thenReturn(userList);
		Mockito.doNothing().when(util).updateExistingUser(Mockito.any(GuildUser.class));

		cache.setGuildUserUtility(util);
		cache.addNewGuild(6789);
		cache.updateCacheForGuildUser(6789, 1234);

		cache.writeCacheToDatabase();

		Mockito.verify(util, Mockito.times(0)).updateLastSeenForUser(Mockito.any(GuildUser.class));
	}
}
