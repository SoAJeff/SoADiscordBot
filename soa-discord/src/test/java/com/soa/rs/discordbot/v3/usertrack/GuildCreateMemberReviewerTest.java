package com.soa.rs.discordbot.v3.usertrack;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import reactor.core.publisher.Mono;

public class GuildCreateMemberReviewerTest {

	@Test
	public void addNewMemberTest() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		GuildUser user = reviewer.addNewUser(member);

		Mockito.verify(userUtility, Mockito.times(1)).addNewUser(Mockito.any(GuildUser.class));
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Joined the server");

		Assert.assertTrue(user.equals(newUser));
	}

	@Test
	public void updateExistingMemberWhereUserIsSameTest() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setKnownName("User1");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		GuildUser user = reviewer.updateExistingMember(member, newUser);

		Mockito.verify(nicknameUtility, Mockito.never())
				.addNickname(Mockito.eq(1234), Mockito.eq(6789), Mockito.anyString());

		Assert.assertEquals(newUser, user);
	}

	@Test
	public void updateExistingMemberWhereUserHasDifferentNickname() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User2");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setKnownName("User1");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		GuildUser user = reviewer.updateExistingMember(member, newUser);

		Mockito.verify(nicknameUtility, Mockito.times(1)).addNickname(1234, 6789, "User2");
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Changed their display name", "User1", "User2");

		Assert.assertFalse(newUser.equals(user));
	}

	@Test
	public void updateExistingMemberWhereUserHasNoKnownName() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		GuildUser user = reviewer.updateExistingMember(member, newUser);

		Mockito.verify(nicknameUtility, Mockito.never())
				.addNickname(Mockito.eq(1234), Mockito.eq(6789), Mockito.anyString());

		Assert.assertEquals(newUser, user);
	}

	@Test
	public void updateExistingMemberWhereUserDoesNotMatch() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User2");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setKnownName("User1");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		GuildUser user = reviewer.updateExistingMember(member, newUser);

		Assert.assertEquals("@User2#5678", user.getUsername());
		Mockito.verify(recentActionUtility)
				.addRecentAction(6789, 1234, "Changed their user handle", "@User1#5678", "@User2#5678");
		Assert.assertFalse(newUser.equals(user));
	}

	@Test
	public void updateExistingMemberWhereUserHadLeftAndRejoined() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getUsername()).thenReturn("User1");
		Mockito.when(member.getDiscriminator()).thenReturn("5678");
		Mockito.when(member.getDisplayName()).thenReturn("User1");
		Mockito.when(member.getJoinTime()).thenReturn(Optional.of(now));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setKnownName("User1");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(Instant.parse("2007-12-03T10:15:30.00Z")));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(now));
		newUser.setLastActive(Date.from(now));

		GuildUser user = reviewer.updateExistingMember(member, newUser);

		Assert.assertEquals(Date.from(Instant.EPOCH), user.getLeftServerAsDate());
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Joined the server");
		Assert.assertFalse(newUser.equals(user));
	}

	@Test
	public void testIsOffline() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		Presence presence = Mockito.mock(Presence.class);
		Mockito.when(presence.getStatus()).thenReturn(Status.OFFLINE);

		Mono<Boolean> mono = reviewer.isOnline(presence);

		Assert.assertFalse(mono.block());
	}

	@Test
	public void testIsOnline() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		Presence presence = Mockito.mock(Presence.class);
		Mockito.when(presence.getStatus()).thenReturn(Status.ONLINE);

		Mono<Boolean> mono = reviewer.isOnline(presence);

		Assert.assertTrue(mono.block());
	}

	@Test
	public void testIsIdle() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		Presence presence = Mockito.mock(Presence.class);
		Mockito.when(presence.getStatus()).thenReturn(Status.IDLE);

		Mono<Boolean> mono = reviewer.isOnline(presence);

		Assert.assertTrue(mono.block());
	}

	@Test
	public void testIsDnD() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		Presence presence = Mockito.mock(Presence.class);
		Mockito.when(presence.getStatus()).thenReturn(Status.DO_NOT_DISTURB);

		Mono<Boolean> mono = reviewer.isOnline(presence);

		Assert.assertTrue(mono.block());
	}

	@Test
	public void testCheckLastOnlineAndSubmitWhenMemberIsOnline() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Presence presence = Mockito.mock(Presence.class);
		Mockito.when(presence.getStatus()).thenReturn(Status.ONLINE);
		Mockito.when(member.getPresence()).thenReturn(Mono.just(presence));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setKnownName("User1");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(Instant.EPOCH));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		reviewer.checkLastOnlineAndSubmit(member, newUser);

		Assert.assertEquals(1, reviewer.getUsersToSubmit().size());

		Duration.between(now, newUser.getLastSeen().toInstant());

		// Time difference should be less than 5 seconds, to account for speed when running the tests.
		Assert.assertTrue(Duration.between(now, newUser.getLastSeen().toInstant()).getSeconds() < 5);
	}

	@Test
	public void testCheckLastOnlineAndSubmitWhenMemberIsOffline() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		Instant now = Instant.now();

		Member member = Mockito.mock(Member.class);
		Presence presence = Mockito.mock(Presence.class);
		Mockito.when(presence.getStatus()).thenReturn(Status.OFFLINE);
		Mockito.when(member.getPresence()).thenReturn(Mono.just(presence));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setKnownName("User1");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		reviewer.checkLastOnlineAndSubmit(member, newUser);

		userUtility.updateExistingUser(Mockito.any(GuildUser.class));

		Assert.assertEquals(Date.from(now), newUser.getLastSeen());
	}

	@Test
	public void testRemoveUser() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		Instant now = Instant.now();

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setKnownName("User1");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		List<GuildUser> users = new ArrayList<>();
		users.add(newUser);
		Mockito.when(userUtility.getGuildUser(1234L, 6789)).thenReturn(users);

		List<Long> ids = new ArrayList<>();
		ids.add(1234L);

		reviewer.setGuildUsers(ids);
		reviewer.setGuildId(6789);
		reviewer.setUserUtility(userUtility);
		reviewer.setRecentActionUtility(recentActionUtility);

		reviewer.removeRemainingUsers();

		Mockito.verify(userUtility, Mockito.times(1)).updateExistingUser(Mockito.any(GuildUser.class));
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Left the server");

		//Maybe...
		Assert.assertNotNull(newUser.getLeftServerAsDate());
	}

	@Test
	public void testRemoveUserThatIsntInList() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);

		List<GuildUser> users = new ArrayList<>();
		Mockito.when(userUtility.getGuildUser(1234L, 6789)).thenReturn(users);

		List<Long> ids = new ArrayList<>();
		ids.add(1234L);

		reviewer.setGuildUsers(ids);
		reviewer.setGuildId(6789);
		reviewer.setUserUtility(userUtility);
		reviewer.setRecentActionUtility(recentActionUtility);

		reviewer.removeRemainingUsers();

		Mockito.verify(userUtility, Mockito.never()).updateExistingUser(Mockito.any(GuildUser.class));
	}

	@Test
	public void reviewMemberAddNew() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		Instant now = Instant.now();

		Member member1 = Mockito.mock(Member.class);
		Mockito.when(member1.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member1.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member1.getUsername()).thenReturn("User1");
		Mockito.when(member1.getDiscriminator()).thenReturn("5678");
		Mockito.when(member1.getDisplayName()).thenReturn("User1");
		Mockito.when(member1.getJoinTime()).thenReturn(Optional.of(now));
		Presence presence = Mockito.mock(Presence.class);
		Mockito.when(presence.getStatus()).thenReturn(Status.OFFLINE);
		Mockito.when(member1.getPresence()).thenReturn(Mono.just(presence));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		List<GuildUser> users = new ArrayList<>();
		GuildUser newUser2 = new GuildUser();
		newUser2.setSnowflake(5678);
		users.add(newUser2);
		reviewer.setAllUsers(users);

		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		reviewer.setLeftUsers(new ArrayList<>());

		reviewer.reviewMember(member1);

		Mockito.verify(userUtility, Mockito.times(1)).addNewUser(Mockito.eq(newUser));
		Assert.assertEquals(1, reviewer.getUsersToSubmit().size());
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Joined the server");

	}

	@Test
	public void reviewMemberUpdateExisting() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		Instant now = Instant.now();

		Member member1 = Mockito.mock(Member.class);
		Mockito.when(member1.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member1.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member1.getUsername()).thenReturn("User1");
		Mockito.when(member1.getDiscriminator()).thenReturn("5678");
		Mockito.when(member1.getDisplayName()).thenReturn("User1");
		Mockito.when(member1.getJoinTime()).thenReturn(Optional.of(now));
		Presence presence = Mockito.mock(Presence.class);
		Mockito.when(presence.getStatus()).thenReturn(Status.OFFLINE);
		Mockito.when(member1.getPresence()).thenReturn(Mono.just(presence));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setKnownName("User1");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		List<GuildUser> users = new ArrayList<>();
		users.add(newUser);
		reviewer.setAllUsers(users);

		List<String> nickNames = new ArrayList<>();
		nickNames.add("User1");
		Mockito.when(nicknameUtility.getNicknamesForUser(1234, 6789)).thenReturn(nickNames);

		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);

		List<Long> guildUsers = new ArrayList<>();
		guildUsers.add(1234L);
		reviewer.setGuildUsers(guildUsers);

		reviewer.reviewMember(member1);
		reviewer.submitUsers();

		Assert.assertEquals(1, reviewer.getUsersToSubmit().size());
		Mockito.verify(userUtility).updateExistingUsers(Mockito.anyList());

		Assert.assertEquals(0, reviewer.getGuildUsers().size());

	}

	@Test
	public void testUserLeftAndRejoined() {
		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		GuildUserUtility userUtility = Mockito.mock(GuildUserUtility.class);
		NicknameUtility nicknameUtility = Mockito.mock(NicknameUtility.class);
		RecentActionUtility recentActionUtility = Mockito.mock(RecentActionUtility.class);
		Instant now = Instant.now();

		Member member1 = Mockito.mock(Member.class);
		Mockito.when(member1.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(member1.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member1.getUsername()).thenReturn("User1");
		Mockito.when(member1.getDiscriminator()).thenReturn("5678");
		Mockito.when(member1.getDisplayName()).thenReturn("User1");
		Mockito.when(member1.getJoinTime()).thenReturn(Optional.of(now));
		Presence presence = Mockito.mock(Presence.class);
		Mockito.when(presence.getStatus()).thenReturn(Status.OFFLINE);
		Mockito.when(member1.getPresence()).thenReturn(Mono.just(presence));

		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(1234);
		newUser.setGuildSnowflake(6789);
		newUser.setUsername("@User1#5678");
		newUser.setDisplayName("User1");
		newUser.setJoinedServer(Date.from(now));
		newUser.setLastSeen(Date.from(now));
		newUser.setLeftServer(Date.from(Instant.EPOCH));
		newUser.setLastActive(Date.from(now));

		List<GuildUser> users = new ArrayList<>();
		GuildUser newUser2 = new GuildUser();
		newUser2.setSnowflake(5678);
		users.add(newUser2);
		reviewer.setAllUsers(users);

		reviewer.setUserUtility(userUtility);
		reviewer.setNicknameUtility(nicknameUtility);
		reviewer.setRecentActionUtility(recentActionUtility);
		List<GuildUser> leftUsers = new ArrayList<>();
		leftUsers.add(newUser);
		reviewer.setLeftUsers(leftUsers);

		reviewer.reviewMember(member1);
		reviewer.submitUsers();

		Assert.assertEquals(1, reviewer.getUsersToSubmit().size());
		Mockito.verify(userUtility).updateExistingUsers(Mockito.anyList());
		Mockito.verify(recentActionUtility).addRecentAction(6789, 1234, "Rejoined the server");
	}

	@Test
	public void testGetMemberName() {
		Member member1 = Mockito.mock(Member.class);
		Mockito.when(member1.getUsername()).thenReturn("User1");
		Mockito.when(member1.getDiscriminator()).thenReturn("5678");
		Mockito.when(member1.getDisplayName()).thenReturn("User1");

		GuildCreateMemberReviewer reviewer = new GuildCreateMemberReviewer();
		Assert.assertEquals("@User1#5678", reviewer.getMemberName(member1));
	}

}
