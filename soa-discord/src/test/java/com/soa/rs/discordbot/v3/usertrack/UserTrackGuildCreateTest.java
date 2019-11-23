package com.soa.rs.discordbot.v3.usertrack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.GuildUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildEntry;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserTrackGuildCreateTest {

/*	GuildUtility guildUtility = new GuildUtility();
	GuildUserUtility userUtility = new GuildUserUtility();
	NicknameUtility nicknameUtility = new NicknameUtility();

	@BeforeClass
	public static void setUp() {
		Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
		JdbiWrapper.getInstance().setJdbi(jdbi);
	}

	@Before
	public void createDb() {
		guildUtility.createGuildsTable();
	}

	@After
	public void tearDown() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute("drop table guilds"));
	}*/

	@Test
	public void testGuildMatchesNoUpdateNeeded() {
		UserTrackGuildCreate guildCreate = new UserTrackGuildCreate();
		GuildUtility util = Mockito.mock(GuildUtility.class);
		RecentCache cache = new LastSeenCache();
		guildCreate.setLastSeenCache(cache);

		List<GuildEntry> guildEntries = new ArrayList<>();
		GuildEntry entry = new GuildEntry();
		entry.setGuildName("Guild1");
		entry.setSnowflake(12345);
		guildEntries.add(entry);

		Mockito.when(util.getGuildById(12345)).thenReturn(guildEntries);

		guildCreate.setGuildUtil(util);
		guildCreate.checkAndUpdateGuildDb(12345, "Guild1");

		Mockito.verify(util, Mockito.never()).updateGuildInfo(12345, "Guild1");
	}

	@Test
	public void testGuildNameDoesntMatchAndIsUpdated() {
		UserTrackGuildCreate guildCreate = new UserTrackGuildCreate();
		GuildUtility util = Mockito.mock(GuildUtility.class);
		RecentCache cache = new LastSeenCache();
		guildCreate.setLastSeenCache(cache);

		List<GuildEntry> guildEntries = new ArrayList<>();
		GuildEntry entry = new GuildEntry();
		entry.setGuildName("Guild1");
		entry.setSnowflake(67890);
		guildEntries.add(entry);

		Mockito.when(util.getGuildById(67890)).thenReturn(guildEntries);

		guildCreate.setGuildUtil(util);
		guildCreate.checkAndUpdateGuildDb(67890, "Guild2");

		Mockito.verify(util, Mockito.times(1)).updateGuildInfo(67890, "Guild2");
	}

	@Test
	public void testGuildWasntBeingTracked() {
		UserTrackGuildCreate guildCreate = new UserTrackGuildCreate();
		GuildUtility util = Mockito.mock(GuildUtility.class);
		RecentCache cache = new LastSeenCache();
		guildCreate.setLastSeenCache(cache);

		List<GuildEntry> guildEntries = new ArrayList<>();
		Mockito.when(util.getGuildById(67890)).thenReturn(guildEntries);

		guildCreate.setGuildUtil(util);
		guildCreate.checkAndUpdateGuildDb(12345, "Guild1");

		Mockito.verify(util, Mockito.times(1)).addNewGuild(12345, "Guild1");
	}

	@Test
	public void testHandleJoinGuild() {
		UserTrackGuildCreate guildCreate = Mockito.spy(UserTrackGuildCreate.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		RecentCache cache = new LastSeenCache();
		guildCreate.setLastSeenCache(cache);
		RecentCache lastActiveCache = new LastActiveCache();
		guildCreate.setLastActiveCache(lastActiveCache);
		guildCreate.setRecentActionUtility(recentActionUtility);
		GuildCreateMemberReviewer reviewer = Mockito.mock(GuildCreateMemberReviewer.class);
		Mockito.when(reviewer.reviewMember(Mockito.any(Member.class))).thenReturn(Mono.empty());
		Mockito.when(reviewer.removeRemainingUsers()).thenReturn(Mono.empty());
		Mockito.when(guildCreate.createReviewer()).thenReturn(reviewer);
		Mockito.doNothing().when(guildCreate).checkAndUpdateGuildDb(6789, "SoA");

		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = new NicknameUtility();

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);

		List<GuildUser> list = new ArrayList<>();
		list.add(user);

		Mockito.when(userUtility.getUsersForGuildId(6789)).thenReturn(list);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.isBot()).thenReturn(false);

		Guild guild = Mockito.mock(Guild.class);
		Mockito.when(guild.getMembers()).thenReturn(Flux.just(member));
		Mockito.when(guild.getId()).thenReturn(Snowflake.of(6789));
		Mockito.when(guild.getName()).thenReturn("SoA");

		guildCreate.setUserUtility(userUtility);
		guildCreate.setNicknameUtility(nicknameUtility);

		GuildCreateEvent event = Mockito.mock(GuildCreateEvent.class);
		Mockito.when(event.getGuild()).thenReturn(guild);

		guildCreate.handleJoinedGuild(event);

		Mockito.verify(reviewer, Mockito.times(1)).reviewMember(Mockito.any(Member.class));
		Mockito.verify(reviewer, Mockito.times(1)).removeRemainingUsers();
		//		Assert.assertEquals(1, reviewer.getGuildUsers().size());

	}

	@Test
	public void testHandleJoinGuildBotUser() {
		UserTrackGuildCreate guildCreate = Mockito.spy(UserTrackGuildCreate.class);
		GuildCreateMemberReviewer reviewer = Mockito.mock(GuildCreateMemberReviewer.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		guildCreate.setRecentActionUtility(recentActionUtility);
		RecentCache cache = new LastSeenCache();
		guildCreate.setLastSeenCache(cache);
		RecentCache lastActiveCache = new LastActiveCache();
		guildCreate.setLastActiveCache(lastActiveCache);
		Mockito.when(reviewer.reviewMember(Mockito.any(Member.class))).thenReturn(Mono.empty());
		Mockito.when(reviewer.removeRemainingUsers()).thenReturn(Mono.empty());
		Mockito.when(guildCreate.createReviewer()).thenReturn(reviewer);
		//		Mockito.when(reviewer.setGuildUsers()).thenCallRealMethod();
		Mockito.doNothing().when(guildCreate).checkAndUpdateGuildDb(6789, "SoA");

		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = new NicknameUtility();

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);

		List<GuildUser> list = new ArrayList<>();
		list.add(user);

		Mockito.when(userUtility.getUsersForGuildId(6789)).thenReturn(list);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.isBot()).thenReturn(true);

		Guild guild = Mockito.mock(Guild.class);
		Mockito.when(guild.getMembers()).thenReturn(Flux.just(member));
		Mockito.when(guild.getId()).thenReturn(Snowflake.of(6789));
		Mockito.when(guild.getName()).thenReturn("SoA");

		guildCreate.setUserUtility(userUtility);
		guildCreate.setNicknameUtility(nicknameUtility);

		GuildCreateEvent event = Mockito.mock(GuildCreateEvent.class);
		Mockito.when(event.getGuild()).thenReturn(guild);

		guildCreate.handleJoinedGuild(event);

		Mockito.verify(reviewer, Mockito.never()).reviewMember(Mockito.any(Member.class));
		Mockito.verify(reviewer, Mockito.times(1)).removeRemainingUsers();
		//		Assert.assertEquals(0, reviewer.getGuildUsers().size());
	}

	@Test
	public void testSetGuildUsersStream() {
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);

		List<GuildUser> list = new ArrayList<>();
		list.add(user);

		Mockito.when(userUtility.getUsersForGuildId(6789)).thenReturn(list);

		Guild guild = Mockito.mock(Guild.class);
		Mockito.when(guild.getId()).thenReturn(Snowflake.of(6789));
		Mockito.when(guild.getName()).thenReturn("SoA");

		List<Long> listOfIds = userUtility.getUsersForGuildId(guild.getId().asLong()).stream()
				.map(GuildUser::getSnowflake).collect(Collectors.toList());

		Assert.assertEquals(1, listOfIds.size());

		Assert.assertEquals(1234, listOfIds.get(0).longValue());
	}

}
