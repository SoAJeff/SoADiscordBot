package com.soa.rs.discordbot.v3.usertrack;

import java.util.List;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.RecentActionUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserTrackMemberUpdated {

	private GuildUserUtility guildUserUtility;
	private RecentActionUtility recentActionUtility;
	private NicknameUtility nicknameUtility;
	private RecentlySeenCache cache;

	/**
	 * A user update processes whether the user has changed their
	 * username or discriminator.  While other updates may cause a
	 * user update, they are not applicable to the system at this time.
	 * <p>
	 * This needs to check for ALL instances of the user, as the user could
	 * be in multiple guilds.
	 */
	public void handleUserUpdate(User user) {
		String memberName = "@" + user.getUsername() + "#" + user.getDiscriminator();
		List<GuildUser> users = this.guildUserUtility.getUsersForUserId(user.getId().asLong());
		for (GuildUser guildUser : users) {
			this.cache.updateCacheForGuildUser(guildUser.getGuildSnowflake(), guildUser.getSnowflake());
			if (!memberName.equals(guildUser.getUsername())) {
				SoaLogging.getLogger(this)
						.debug("Received User Update and name did not match, updating [" + memberName + ", " + user
								.getId().asLong() + ", " + guildUser.getGuildSnowflake() + "]");
				this.guildUserUtility
						.updateUserNameForUser(memberName, guildUser.getSnowflake(), guildUser.getGuildSnowflake());
				this.recentActionUtility.addRecentAction(guildUser.getGuildSnowflake(), guildUser.getSnowflake(),
						"Changed their user handle", guildUser.getUsername(), memberName);
			}
		}

	}

	/**
	 * A member update processes whether a user has changed their nickname
	 * in a guild.  Note that this should not update the cache, as Discord
	 * only dispatches the event on update of a nickname or role, and both
	 * can happen when the user is not currently logged into Discord and
	 * do not reflect activity.
	 */
	public void handleMemberUpdate(Member member) {
		GuildUser user = this.guildUserUtility.getGuildUser(member.getId().asLong(), member.getGuildId().asLong())
				.get(0);
		List<String> nicknames = this.nicknameUtility
				.getNicknamesForUser(member.getId().asLong(), member.getGuildId().asLong());
		String displayName = member.getDisplayName();

		if (!user.getDisplayName().equals(displayName)) {
			SoaLogging.getLogger(this)
					.debug("Received Member Update and display name has changed, updating [" + displayName + ", "
							+ member.getId().asLong() + ", " + member.getGuildId().asLong() + "]");
			this.guildUserUtility
					.updateDisplayNameForUser(displayName, member.getId().asLong(), member.getGuildId().asLong());
			this.recentActionUtility.addRecentAction(member.getGuildId().asLong(), member.getId().asLong(),
					"Changed their display name", user.getDisplayName(), displayName);
		}

		if (!nicknames.contains(displayName)) {
			this.nicknameUtility.addNickname(member.getId().asLong(), member.getGuildId().asLong(), displayName);
		}
	}

	public Mono<Void> handlePresenceUpdate(Member member, PresenceUpdateEvent event) {
		SoaLogging.getLogger(this)
				.trace("Presence update received for [" + member.getDisplayName() + ", " + member.getId().asLong()
						+ ", " + member.getGuildId().asLong() + "]");
		this.cache.updateCacheForGuildUser(member.getGuildId().asLong(), member.getId().asLong());
		if (event.getNewUsername().isPresent() || event.getNewDiscriminator().isPresent()) {
			/*
			 * Discord sent these, but this seems to get sent falsely when nothing changed.
			 * Below will check and see if they are the same and only update if not.
			 */
			String memberName =
					"@" + event.getNewUsername().orElse(member.getUsername()) + "#" + event.getNewDiscriminator()
							.orElse(member.getDiscriminator());

			return Flux.fromIterable(
					this.guildUserUtility.getGuildUser(member.getId().asLong(), member.getGuildId().asLong()))
					.filter(guildUser -> !guildUser.getUsername().equals(memberName))
					.flatMap(guildUser -> Mono.fromRunnable(() -> {
						performMemberNameUpdate(member, memberName, guildUser);
					})).then();
		}

		return Mono.empty();
	}

	void performMemberNameUpdate(Member member, String memberName, GuildUser guildUser) {
		SoaLogging.getLogger(this)
				.debug("Username update found, updating [" + memberName + ", " + member.getId().asLong() + ", " + member
						.getGuildId().asLong() + "]");
		this.guildUserUtility
				.updateUserNameForUser(memberName, guildUser.getSnowflake(), guildUser.getGuildSnowflake());
		this.recentActionUtility
				.addRecentAction(guildUser.getGuildSnowflake(), guildUser.getSnowflake(), "Changed their user handle",
						guildUser.getUsername(), memberName);
	}

	public void setGuildUserUtility(GuildUserUtility guildUserUtility) {
		this.guildUserUtility = guildUserUtility;
	}

	public void setRecentActionUtility(RecentActionUtility recentActionUtility) {
		this.recentActionUtility = recentActionUtility;
	}

	public void setNicknameUtility(NicknameUtility nicknameUtility) {
		this.nicknameUtility = nicknameUtility;
	}

	public void setCache(RecentlySeenCache cache) {
		this.cache = cache;
	}
}
