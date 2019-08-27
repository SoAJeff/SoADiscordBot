package com.soa.rs.discordbot.v3.jdbi;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

public class GuildUserUtility {

	public void createUsersTable() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute(
				"create table users(snowflake bigint, guildsnowflake bigint, username varchar(255) not null, knownname varchar(255), displayname varchar(255) not null, lastseen timestamp, joinedserver timestamp, leftserver datetime, constraint unique_user primary key(snowflake, guildsnowflake))"));
	}

	public void addNewUser(GuildUser user) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"insert into users (snowflake, guildsnowflake, username, knownname, displayname, lastseen, joinedserver, leftserver) values (:snowflake, :guildSnowflake, :username, :knownName, :displayName, :lastSeen, :joinedServer, :leftServer)")
				.bindBean(user).execute());
	}

	public void updateExistingUser(GuildUser user) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set username = :username, knownname = :knownname, displayname = :displayname, lastseen = :lastseen, joinedserver = :joinedserver, leftserver = :leftserver where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("username", user.getUsername()).bind("knownname", user.getKnownName())
				.bind("displayname", user.getDisplayName()).bind("lastseen", user.getLastSeen())
				.bind("joinedserver", user.getJoinedServer()).bind("leftserver", user.getLeftServer())
				.bind("snowflake", user.getSnowflake()).bind("guildsnowflake", user.getGuildSnowflake()).execute());
	}

	public void updateExistingUsers(List<GuildUser> users) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> {
			for (GuildUser user : users) {
				handle.createUpdate(
						"update users set username = :username, knownname = :knownname, displayname = :displayname, lastseen = :lastseen, joinedserver = :joinedserver, leftserver = :leftserver where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
						.bind("username", user.getUsername()).bind("knownname", user.getKnownName())
						.bind("displayname", user.getDisplayName()).bind("lastseen", user.getLastSeen())
						.bind("joinedserver", user.getJoinedServer()).bind("leftserver", user.getLeftServer())
						.bind("snowflake", user.getSnowflake()).bind("guildsnowflake", user.getGuildSnowflake())
						.execute();
			}
		});
	}

	public void updateKnownNameForUser(String name, long snowflake, long guildSnowflake) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set knownname = :knownname where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("knownname", name).bind("snowflake", snowflake).bind("guildsnowflake", guildSnowflake).execute());
	}

	public void updateDisplayNameForUser(String displayName, long snowflake, long guildSnowflake) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set displayname = :displayname where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("displayname", displayName).bind("snowflake", snowflake).bind("guildsnowflake", guildSnowflake)
				.execute());
	}

	public void updateUserNameForUser(String name, long snowflake, long guildSnowflake) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set username = :username where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("username", name).bind("snowflake", snowflake).bind("guildsnowflake", guildSnowflake).execute());
	}

	public void updateLastSeenForUser(Date lastSeen, long snowflake, long guildSnowflake) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set lastseen = :lastSeen where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("lastSeen", lastSeen).bind("snowflake", snowflake).bind("guildsnowflake", guildSnowflake)
				.execute());
	}

	public void updateLastSeenForUser(long guildSnowflake, Map<Long, Date> userMap) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> {
			for (long userSnowflake : userMap.keySet()) {
				handle.createUpdate(
						"update users set lastseen = :lastSeen where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
						.bind("lastSeen", userMap.get(userSnowflake)).bind("snowflake", userSnowflake)
						.bind("guildsnowflake", guildSnowflake).execute();
			}
		});
	}

	public void updateLastSeenForUser(GuildUser user) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set lastseen = :lastSeen where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("lastSeen", user.getLastSeen()).bind("snowflake", user.getSnowflake())
				.bind("guildsnowflake", user.getGuildSnowflake()).execute());
	}

	public void setJoinDateForUser(Date joined, long snowflake, long guildSnowflake) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set joinedserver = :joinedserver where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("joinedserver", joined).bind("snowflake", snowflake).bind("guildsnowflake", guildSnowflake)
				.execute());
	}

	public void setLeftDateForUser(Date left, long snowflake, long guildSnowflake) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set leftserver = :leftserver where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("leftserver", left).bind("snowflake", snowflake).bind("guildsnowflake", guildSnowflake)
				.execute());
	}

	public List<GuildUser> getGuildUser(long snowflake, long guildSnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle
				.createQuery("select * from users where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("snowflake", snowflake).bind("guildsnowflake", guildSnowflake).mapToBean(GuildUser.class).list());
	}

	public List<GuildUser> getGuildUser(String name) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select * from users where lower(username) like '%" + name.toLowerCase()
						+ "%' or lower(knownname) like '%" + name.toLowerCase() + "%' or lower(displayname) like '%"
						+ name.toLowerCase() + "%'").mapToBean(GuildUser.class).list());
	}

	public List<GuildUser> getGuildUserWithNameInGuild(String name, long guildSnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select * from users where (lower(username) like '%" + name.toLowerCase()
						+ "%' or lower(knownname) like '%" + name.toLowerCase() + "%' or lower(displayname) like '%"
						+ name.toLowerCase() + "%') and guildsnowflake = :guildSnowflake")
				.bind("guildSnowflake", guildSnowflake).mapToBean(GuildUser.class).list());
	}

	public List<GuildUser> getUsersForGuildId(long guildSnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from users where guildsnowflake = :guildSnowflake")
						.bind("guildSnowflake", guildSnowflake).mapToBean(GuildUser.class).list());
	}

	public List<GuildUser> getUsersCurrentlyInGuildForGuildId(long guildSnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle
				.createQuery("select * from users where guildsnowflake = :guildSnowflake and leftserver = :date")
				.bind("guildSnowflake", guildSnowflake).bind("date", Date.from(Instant.EPOCH))
				.mapToBean(GuildUser.class).list());
	}

	public List<GuildUser> getUsersForUserId(long snowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from users where snowflake = :snowflake")
						.bind("snowflake", snowflake).mapToBean(GuildUser.class).list());
	}
}
