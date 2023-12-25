package com.soa.rs.discordbot.v3.jdbi;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

public class GuildUserUtility {

	public void createUsersTable() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute(
				"create table users(snowflake bigint, guildsnowflake bigint, username varchar(255) not null, knownname varchar(255), displayname varchar(255) not null, lastseen timestamp, joinedserver timestamp, leftserver datetime, lastactive timestamp, constraint unique_user primary key(snowflake, guildsnowflake))"));
	}

	public void addNewUser(GuildUser user) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"insert into users (snowflake, guildsnowflake, username, knownname, displayname, lastseen, joinedserver, leftserver, lastactive) values (:snowflake, :guildSnowflake, :username, :knownName, :displayName, :lastSeen, :joinedServer, :leftServer, :lastActive)")
				.bindBean(user).execute());
	}

	public void updateExistingUser(GuildUser user) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set username = :username, knownname = :knownname, displayname = :displayname, lastseen = :lastseen, joinedserver = :joinedserver, leftserver = :leftserver, lastactive = :lastactive where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("username", user.getUsername()).bind("knownname", user.getKnownName())
				.bind("displayname", user.getDisplayName()).bind("lastseen", user.getLastSeen())
				.bind("joinedserver", user.getJoinedServer()).bind("leftserver", user.getLeftServerAsDate())
				.bind("lastactive", user.getLastActive()).bind("snowflake", user.getSnowflake())
				.bind("guildsnowflake", user.getGuildSnowflake()).execute());
	}

	public void updateExistingUsers(List<GuildUser> users) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> {
			for (GuildUser user : users) {
				handle.createUpdate(
						"update users set username = :username, knownname = :knownname, displayname = :displayname, lastseen = :lastseen, joinedserver = :joinedserver, leftserver = :leftserver, lastactive = :lastactive where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
						.bind("username", user.getUsername()).bind("knownname", user.getKnownName())
						.bind("displayname", user.getDisplayName()).bind("lastseen", user.getLastSeen())
						.bind("joinedserver", user.getJoinedServer()).bind("leftserver", user.getLeftServerAsDate())
						.bind("lastactive", user.getLastActive()).bind("snowflake", user.getSnowflake())
						.bind("guildsnowflake", user.getGuildSnowflake()).execute();
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

	public void updateLastActiveForUser(GuildUser user) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set lastactive = :lastActive where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("lastActive", user.getLastActive()).bind("snowflake", user.getSnowflake())
				.bind("guildsnowflake", user.getGuildSnowflake()).execute());
	}

	public void updateLastActiveForUser(Date lastActive, long snowflake, long guildSnowflake) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"update users set lastactive = :lastActive where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("lastActive", lastActive).bind("snowflake", snowflake).bind("guildsnowflake", guildSnowflake)
				.execute());
	}

	public void updateLastActiveForUser(long guildSnowflake, Map<Long, Date> userMap) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> {
			for (long userSnowflake : userMap.keySet()) {
				handle.createUpdate(
						"update users set lastactive = :lastActive where snowflake = :snowflake and guildsnowflake = :guildsnowflake")
						.bind("lastActive", userMap.get(userSnowflake)).bind("snowflake", userSnowflake)
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

	public List<GuildUser> getLeftUsersInGuildForGuildId(long guildSnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle
				.createQuery("select * from users where guildsnowflake = :guildSnowflake and leftserver <> :date")
				.bind("guildSnowflake", guildSnowflake).bind("date", Date.from(Instant.EPOCH))
				.mapToBean(GuildUser.class).list());
	}

	public List<GuildUser> getUsersForUserId(long snowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from users where snowflake = :snowflake")
						.bind("snowflake", snowflake).mapToBean(GuildUser.class).list());
	}

	public List<String> getUserActivityDatesForUsername(List<String> usernames, long guildSnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> {
			List<String> activityEntries = new ArrayList<>();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			StringBuilder sb = new StringBuilder();
			for (String name : usernames) {
				List<GuildUser> userEntries = handle.createQuery(
						"select * from users where (lower(username) like '%" + name.toLowerCase()
								+ "%' or lower(knownname) like '%" + name.toLowerCase()
								+ "%' or lower(displayname) like '%" + name.toLowerCase()
								+ "%') and guildsnowflake = :guildSnowflake").bind("guildSnowflake", guildSnowflake)
						.mapToBean(GuildUser.class).list();
				if (userEntries.size() == 0) {
					sb.append("**");
					sb.append(name);
					sb.append("**");
					sb.append(": No activity data found.");
					activityEntries.add(sb.toString());
					sb.setLength(0);
				} else {
					sb.append("**");
					sb.append(name);
					sb.append("**");
					sb.append(": ");
					for (int i = 0; i < userEntries.size(); i++) {
						GuildUser entry = userEntries.get(i);
						sb.append(entry.getUsername());
						sb.append(": ");
						sb.append(sdf.format(entry.getLastActive()));
						//Check if there's another entry
						if (i + 1 < userEntries.size())
							sb.append(", ");
					}
					activityEntries.add(sb.toString());
					sb.setLength(0);
				}
			}
			return activityEntries;
		});
	}
}
