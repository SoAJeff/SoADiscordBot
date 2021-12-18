package com.soa.rs.discordbot.v3.usertrack;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.object.entity.Member;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import reactor.core.publisher.Mono;

public class GuildCreateMemberReviewer {

	private GuildUserUtility userUtility;
	private NicknameUtility nicknameUtility;
	private RecentActionUtility recentActionUtility;
	private List<Long> guildUsers;
	private List<GuildUser> allUsers;
	private List<GuildUser> leftUsers;
	private List<GuildUser> usersToSubmit = new ArrayList<>();
	private long guildId;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public Mono<Void> reviewMember(Member member) {
		SoaLogging.getLogger(this)
				.debug("Processing member [" + getMemberName(member) + ", " + member.getId().asLong() + ", " + member
						.getGuildId().asLong() + "]");
		List<GuildUser> listUser = this.allUsers.stream()
				.filter(guildUser -> guildUser.getSnowflake() == member.getId().asLong()).collect(Collectors.toList());
		if (listUser.size() > 0) {
			//Existing user
			guildUsers.remove(member.getId().asLong());
			GuildUser user = updateExistingMember(member, listUser.get(0));
			return checkLastOnlineAndSubmit(member, user);
		} else {
			List<GuildUser> leftUser = this.leftUsers.stream()
					.filter(guildUser -> guildUser.getSnowflake() == member.getId().asLong())
					.collect(Collectors.toList());
			GuildUser user;
			if (leftUser.size() > 0) {
				SoaLogging.getLogger(this)
						.debug("Member [" + getMemberName(member) + ", " + member.getGuildId().asLong()
								+ "] previously left server and has returned.");
				user = updateExistingMember(member, leftUser.get(0));
				this.recentActionUtility
						.addRecentAction(member.getGuildId().asLong(), member.getId().asLong(), "Rejoined the server");
				//Rejoined user
			} else {
				//New user
				user = addNewUser(member);
			}
			return checkLastOnlineAndSubmit(member, user);
		}
	}

	GuildUser updateExistingMember(Member member, GuildUser user) {
		SoaLogging.getLogger(this)
				.debug("Member [" + getMemberName(member) + ", " + member.getId().asLong() + ", " + member.getGuildId()
						.asLong() + "] is existing user, updating user entry");
		GuildUser newUser = new GuildUser();
		newUser.setSnowflake(member.getId().asLong());
		newUser.setGuildSnowflake(member.getGuildId().asLong());
		String memberName = getMemberName(member);
		newUser.setUsername(memberName);
		if (!user.getUsername().equals(memberName)) {
			recentActionUtility
					.addRecentAction(member.getGuildId().asLong(), member.getId().asLong(), "Changed their user handle",
							user.getUsername(), memberName);
		}
		if (user.getKnownName() != null)
			newUser.setKnownName(user.getKnownName());
		newUser.setDisplayName(member.getDisplayName());
		if (!user.getDisplayName().equals(member.getDisplayName())) {
			recentActionUtility.addRecentAction(member.getGuildId().asLong(), member.getId().asLong(),
					"Changed their display name", user.getDisplayName(), member.getDisplayName());
		}
		if(member.getJoinTime().isPresent()) {
			newUser.setJoinedServer(Date.from(member.getJoinTime().get()));
		}
		if (!sdf.format(user.getJoinedServer()).equals(sdf.format(newUser.getJoinedServer()))) {
			SoaLogging.getLogger(this).debug("Joined server times did not match, assuming rejoined server.");
			this.recentActionUtility
					.addRecentAction(member.getGuildId().asLong(), member.getId().asLong(), "Joined the server");
		}
		//We will see if this needs to be updated after, make it equal for now...
		newUser.setLastSeen(user.getLastSeen());
		newUser.setLastActive(user.getLastActive());
		newUser.setLeftServer(Date.from(Instant.EPOCH));

		List<String> nickNames = this.nicknameUtility
				.getNicknamesForUser(user.getSnowflake(), user.getGuildSnowflake());

		if (!nickNames.contains(member.getDisplayName())) {
			this.nicknameUtility.addNickname(user.getSnowflake(), user.getGuildSnowflake(), member.getDisplayName());
		}

		if (!newUser.equals(user))
			return newUser;
		else
			return user;
	}

	String getMemberName(Member member) {
		return "@" + member.getUsername() + "#" + member.getDiscriminator();
	}

	GuildUser addNewUser(Member member) {
		SoaLogging.getLogger(this)
				.debug("Member [" + getMemberName(member) + ", " + member.getId().asLong() + ", " + member.getGuildId()
						.asLong() + "] is new user, creating new user entry");
		GuildUser newUser = NewUserUtility.createNewUser(member);

		userUtility.addNewUser(newUser);
		//Set initial display name as nickname
		this.nicknameUtility
				.addNickname(member.getId().asLong(), member.getGuildId().asLong(), member.getDisplayName());
		this.recentActionUtility
				.addRecentAction(member.getGuildId().asLong(), member.getId().asLong(), "Joined the server");
		return newUser;
	}

	Mono<Void> removeRemainingUsers() {
		SoaLogging.getLogger(this)
				.debug("GuildUsers has " + guildUsers.size() + " items remaining after all processed, marking as left");
		for (long id : guildUsers) {
			List<GuildUser> listUser = this.userUtility.getGuildUser(id, guildId);
			if (listUser.size() > 0) {
				GuildUser user = listUser.get(0);
				SoaLogging.getLogger(this)
						.debug("Marking " + user.getUsername() + " [" + user.getSnowflake() + "] as left server.");
				user.setLeftServer(new Date());
				userUtility.updateExistingUser(user);
				this.recentActionUtility
						.addRecentAction(user.getGuildSnowflake(), user.getSnowflake(), "Left the server");
			}
		}
		return Mono.empty();
	}

	Mono<Void> checkLastOnlineAndSubmit(Member member, GuildUser user) {
		/*
		 * Add all users first - since D4J 3.2, getPresence only seems to be returning online users.
		 * If online, we will update their date prior to submission
		 */
		this.usersToSubmit.add(user);
		return member.getPresence().flatMap(this::isOnline).flatMap(aBoolean -> Mono.fromRunnable(() -> {
			SoaLogging.getLogger(this).trace("Presence (is user online) is " + aBoolean);
			if (aBoolean) {
				user.setLastSeen(new Date());
			}
		})).then();
	}

	void submitUsers() {
		SoaLogging.getLogger(this).debug("Submitting " + usersToSubmit.size() + " users for Guild ID " + this.guildId);
		this.userUtility.updateExistingUsers(usersToSubmit);
	}

	Mono<Boolean> isOnline(Presence presence) {
		if (presence.getStatus().equals(Status.OFFLINE))
			return Mono.just(false);
		else
			return Mono.just(true);
	}

	public void setUserUtility(GuildUserUtility userUtility) {
		this.userUtility = userUtility;
	}

	public void setNicknameUtility(NicknameUtility nicknameUtility) {
		this.nicknameUtility = nicknameUtility;
	}

	public void setRecentActionUtility(RecentActionUtility recentActionUtility) {
		this.recentActionUtility = recentActionUtility;
	}

	public void setGuildUsers(List<Long> guildUsers) {
		this.guildUsers = guildUsers;
	}

	List<Long> getGuildUsers() {
		return guildUsers;
	}

	public void setAllUsers(List<GuildUser> allUsers) {
		this.allUsers = allUsers;
	}

	public List<GuildUser> getUsersToSubmit() {
		return usersToSubmit;
	}

	public void setGuildId(long guildId) {
		this.guildId = guildId;
	}

	public void setLeftUsers(List<GuildUser> leftUsers) {
		this.leftUsers = leftUsers;
	}
}
