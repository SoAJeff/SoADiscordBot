package com.soa.rs.discordbot.v3.usertrack;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

import org.junit.Test;
import org.mockito.Mockito;

import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Snowflake;

public class UserTrackMemberUpdatedTest {

	@Test
	public void handleMemberUpdateDisplayNameDidNotChange() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());

		List<String> nicknames = new ArrayList<>();
		nicknames.add("User");
		Mockito.when(nicknameUtility.getNicknamesForUser(1234, 6789)).thenReturn(nicknames);

		List<GuildUser> users = new ArrayList<>();
		users.add(user);
		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getDisplayName()).thenReturn("User");

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setNicknameUtility(nicknameUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		utmu.handleMemberUpdate(member);

		Mockito.verify(guildUserUtility, Mockito.never())
				.updateDisplayNameForUser(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility, Mockito.never())
				.addRecentAction(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString());
		Mockito.verify(nicknameUtility, Mockito.never())
				.addNickname(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());
		Mockito.verify(cache, Mockito.never()).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void handleMemberUpdateDisplayNameChangedExistingName() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());

		List<String> nicknames = new ArrayList<>();
		nicknames.add("User");
		nicknames.add("User2");
		Mockito.when(nicknameUtility.getNicknamesForUser(1234, 6789)).thenReturn(nicknames);

		List<GuildUser> users = new ArrayList<>();
		users.add(user);
		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getDisplayName()).thenReturn("User2");

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setNicknameUtility(nicknameUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		utmu.handleMemberUpdate(member);

		Mockito.verify(guildUserUtility, Mockito.times(1))
				.updateDisplayNameForUser(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility, Mockito.times(1))
				.addRecentAction(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString());
		Mockito.verify(nicknameUtility, Mockito.never())
				.addNickname(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());
		Mockito.verify(cache, Mockito.never()).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void handleMemberUpdateDisplayNameChangedNotExistingName() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		GuildUser user = new GuildUser();
		user.setSnowflake(1234);
		user.setGuildSnowflake(6789);
		user.setUsername("@User#1234");
		user.setKnownName("KnownNameTest");
		user.setDisplayName("User");
		user.setJoinedServer(new Date());
		user.setLastSeen(new Date());

		List<String> nicknames = new ArrayList<>();
		nicknames.add("User");
		Mockito.when(nicknameUtility.getNicknamesForUser(1234, 6789)).thenReturn(nicknames);

		List<GuildUser> users = new ArrayList<>();
		users.add(user);
		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getDisplayName()).thenReturn("User2");

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setNicknameUtility(nicknameUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		utmu.handleMemberUpdate(member);

		Mockito.verify(guildUserUtility, Mockito.times(1))
				.updateDisplayNameForUser(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility, Mockito.times(1))
				.addRecentAction(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString());
		Mockito.verify(nicknameUtility, Mockito.times(1))
				.addNickname(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());
		Mockito.verify(cache, Mockito.never()).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void handleUserUpdateUsernameDidNotChange() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		GuildUser guildUser = new GuildUser();
		guildUser.setSnowflake(1234);
		guildUser.setGuildSnowflake(6789);
		guildUser.setUsername("@User#1234");
		guildUser.setKnownName("KnownNameTest");
		guildUser.setDisplayName("User");
		guildUser.setJoinedServer(new Date());
		guildUser.setLastSeen(new Date());

		List<GuildUser> users = new ArrayList<>();
		users.add(guildUser);
		Mockito.when(guildUserUtility.getUsersForUserId(1234)).thenReturn(users);

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(user.getUsername()).thenReturn("User");
		Mockito.when(user.getDiscriminator()).thenReturn("1234");

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setNicknameUtility(nicknameUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		utmu.handleUserUpdate(user);

		Mockito.verify(guildUserUtility, Mockito.never())
				.updateUserNameForUser(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility, Mockito.never())
				.addRecentAction(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString());
		Mockito.verify(cache).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void handleUserUpdateUsernameChanged() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		GuildUser guildUser = new GuildUser();
		guildUser.setSnowflake(1234);
		guildUser.setGuildSnowflake(6789);
		guildUser.setUsername("@User#1234");
		guildUser.setKnownName("KnownNameTest");
		guildUser.setDisplayName("User");
		guildUser.setJoinedServer(new Date());
		guildUser.setLastSeen(new Date());

		List<GuildUser> users = new ArrayList<>();
		users.add(guildUser);
		Mockito.when(guildUserUtility.getUsersForUserId(1234)).thenReturn(users);

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(user.getUsername()).thenReturn("User2");
		Mockito.when(user.getDiscriminator()).thenReturn("1234");

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setNicknameUtility(nicknameUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		utmu.handleUserUpdate(user);

		Mockito.verify(guildUserUtility, Mockito.times(1))
				.updateUserNameForUser(Mockito.eq("@User2#1234"), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility, Mockito.times(1))
				.addRecentAction(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString());
		Mockito.verify(cache).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void handleUserUpdateUsernameChangedTwoAccounts() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		GuildUser guildUser = new GuildUser();
		guildUser.setSnowflake(1234);
		guildUser.setGuildSnowflake(6789);
		guildUser.setUsername("@User#1234");
		guildUser.setKnownName("KnownNameTest");
		guildUser.setDisplayName("User");
		guildUser.setJoinedServer(new Date());
		guildUser.setLastSeen(new Date());

		GuildUser guildUser2 = new GuildUser();
		guildUser2.setSnowflake(1234);
		guildUser2.setGuildSnowflake(5678);
		guildUser2.setUsername("@User#1234");
		guildUser2.setKnownName("KnownNameTest");
		guildUser2.setDisplayName("User");
		guildUser2.setJoinedServer(new Date());
		guildUser2.setLastSeen(new Date());

		List<GuildUser> users = new ArrayList<>();
		users.add(guildUser);
		users.add(guildUser2);

		Mockito.when(guildUserUtility.getUsersForUserId(1234)).thenReturn(users);

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(user.getUsername()).thenReturn("User2");
		Mockito.when(user.getDiscriminator()).thenReturn("1234");

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setNicknameUtility(nicknameUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		utmu.handleUserUpdate(user);

		Mockito.verify(guildUserUtility, Mockito.times(2))
				.updateUserNameForUser(Mockito.eq("@User2#1234"), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility, Mockito.times(2))
				.addRecentAction(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString());
		Mockito.verify(cache, Mockito.times(2)).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void testPresenceUpdateUserNotChanged() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("1234");

		PresenceUpdateEvent event = Mockito.mock(PresenceUpdateEvent.class);
		Mockito.when(event.getNewUsername()).thenReturn(Optional.empty());
		Mockito.when(event.getNewDiscriminator()).thenReturn(Optional.empty());

		utmu.handlePresenceUpdate(member, event).block();

		Mockito.verify(guildUserUtility, Mockito.never())
				.updateUserNameForUser(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility, Mockito.never())
				.addRecentAction(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString());
		Mockito.verify(cache).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void testPresenceUpdateUsernameChangedNotDiscriminator() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User2");
		Mockito.when(member.getDiscriminator()).thenReturn("1234");

		PresenceUpdateEvent event = Mockito.mock(PresenceUpdateEvent.class);
		Mockito.when(event.getNewUsername()).thenReturn(Optional.of("User2"));
		Mockito.when(event.getNewDiscriminator()).thenReturn(Optional.empty());

		GuildUser guildUser = new GuildUser();
		guildUser.setSnowflake(1234);
		guildUser.setGuildSnowflake(6789);
		guildUser.setUsername("@User#1234");
		guildUser.setKnownName("KnownNameTest");
		guildUser.setDisplayName("User");
		guildUser.setJoinedServer(new Date());
		guildUser.setLastSeen(new Date());

		List<GuildUser> users = new ArrayList<>();
		users.add(guildUser);

		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		utmu.handlePresenceUpdate(member, event).block();

		Mockito.verify(guildUserUtility)
				.updateUserNameForUser(Mockito.eq("@User2#1234"), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility)
				.addRecentAction(Mockito.eq(6789L), Mockito.eq(1234L), Mockito.anyString(), Mockito.anyString(),
						Mockito.eq("@User2#1234"));
		Mockito.verify(cache).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void testPresenceUpdateDiscriminatorChangedNotUsername() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");

		PresenceUpdateEvent event = Mockito.mock(PresenceUpdateEvent.class);
		Mockito.when(event.getNewUsername()).thenReturn(Optional.empty());
		Mockito.when(event.getNewDiscriminator()).thenReturn(Optional.of("5678"));

		GuildUser guildUser = new GuildUser();
		guildUser.setSnowflake(1234);
		guildUser.setGuildSnowflake(6789);
		guildUser.setUsername("@User#1234");
		guildUser.setKnownName("KnownNameTest");
		guildUser.setDisplayName("User");
		guildUser.setJoinedServer(new Date());
		guildUser.setLastSeen(new Date());

		List<GuildUser> users = new ArrayList<>();
		users.add(guildUser);

		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		utmu.handlePresenceUpdate(member, event).block();

		Mockito.verify(guildUserUtility)
				.updateUserNameForUser(Mockito.eq("@User1#5678"), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility)
				.addRecentAction(Mockito.eq(6789L), Mockito.eq(1234L), Mockito.anyString(), Mockito.anyString(),
						Mockito.eq("@User1#5678"));
		Mockito.verify(cache).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void testPresenceUpdateUsernameAndDiscriminator() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User2");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");

		PresenceUpdateEvent event = Mockito.mock(PresenceUpdateEvent.class);
		Mockito.when(event.getNewUsername()).thenReturn(Optional.of("User2"));
		Mockito.when(event.getNewDiscriminator()).thenReturn(Optional.of("5678"));

		GuildUser guildUser = new GuildUser();
		guildUser.setSnowflake(1234);
		guildUser.setGuildSnowflake(6789);
		guildUser.setUsername("@User#1234");
		guildUser.setKnownName("KnownNameTest");
		guildUser.setDisplayName("User");
		guildUser.setJoinedServer(new Date());
		guildUser.setLastSeen(new Date());

		List<GuildUser> users = new ArrayList<>();
		users.add(guildUser);

		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		utmu.handlePresenceUpdate(member, event).block();

		Mockito.verify(guildUserUtility)
				.updateUserNameForUser(Mockito.eq("@User2#5678"), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility)
				.addRecentAction(Mockito.eq(6789L), Mockito.eq(1234L), Mockito.anyString(), Mockito.anyString(),
						Mockito.eq("@User2#5678"));
		Mockito.verify(cache).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void testPresenceUpdateUsernameAndDiscriminatorButMatchesUsernameOnFile() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User2");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");

		PresenceUpdateEvent event = Mockito.mock(PresenceUpdateEvent.class);
		Mockito.when(event.getNewUsername()).thenReturn(Optional.of("User2"));
		Mockito.when(event.getNewDiscriminator()).thenReturn(Optional.of("5678"));

		GuildUser guildUser = new GuildUser();
		guildUser.setSnowflake(1234);
		guildUser.setGuildSnowflake(6789);
		guildUser.setUsername("@User2#5678");
		guildUser.setKnownName("KnownNameTest");
		guildUser.setDisplayName("User");
		guildUser.setJoinedServer(new Date());
		guildUser.setLastSeen(new Date());

		List<GuildUser> users = new ArrayList<>();
		users.add(guildUser);

		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		utmu.handlePresenceUpdate(member, event).block();

		Mockito.verify(guildUserUtility, Mockito.never())
				.updateUserNameForUser(Mockito.eq("@User2#5678"), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility, Mockito.never())
				.addRecentAction(Mockito.eq(6789L), Mockito.eq(1234L), Mockito.anyString(), Mockito.anyString(),
						Mockito.eq("@User2#5678"));
		Mockito.verify(cache).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}

	@Test
	public void testPresenceUpdateNoUserOnFile() {
		GuildUserUtility guildUserUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		RecentCache cache = Mockito.mock(RecentCache.class);

		UserTrackMemberUpdated utmu = new UserTrackMemberUpdated();
		utmu.setGuildUserUtility(guildUserUtility);
		utmu.setRecentActionUtility(recentActionUtility);
		utmu.setCache(cache);

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User2");
		Mockito.when(member.getDiscriminator()).thenReturn("1234");

		PresenceUpdateEvent event = Mockito.mock(PresenceUpdateEvent.class);
		Mockito.when(event.getNewUsername()).thenReturn(Optional.of("User2"));
		Mockito.when(event.getNewDiscriminator()).thenReturn(Optional.empty());

		List<GuildUser> users = new ArrayList<>();

		Mockito.when(guildUserUtility.getGuildUser(1234, 6789)).thenReturn(users);

		utmu.handlePresenceUpdate(member, event).block();

		Mockito.verify(guildUserUtility, Mockito.never())
				.updateUserNameForUser(Mockito.eq("@User2#1234"), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(recentActionUtility, Mockito.never())
				.addRecentAction(Mockito.eq(6789L), Mockito.eq(1234L), Mockito.anyString(), Mockito.anyString(),
						Mockito.eq("@User2#1234"));
		Mockito.verify(cache).updateCacheForGuildUser(Mockito.anyLong(), Mockito.anyLong());
	}
}
