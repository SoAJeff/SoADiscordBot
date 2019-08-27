package com.soa.rs.discordbot.v3.jdbi;

import java.util.List;

import com.soa.rs.discordbot.v3.jdbi.entities.Nickname;

public class NicknameUtility {

	public void createNicknamesTable() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute(
				"create table nicknames (usersnowflake bigint not null, guildsnowflake bigint not null, displayName varchar(255) not null)"));
	}

	public void addNickname(long usersnowflake, long guildsnowflake, String nickname) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle
				.execute("insert into nicknames (usersnowflake, guildsnowflake, displayName) values (?, ?, ?)",
						usersnowflake, guildsnowflake, nickname));
	}

	public void addNickname(Nickname nickname) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"insert into nicknames (usersnowflake, guildsnowflake, displayName) values (:userSnowflake, :guildSnowflake, :displayName)")
				.bindBean(nickname).execute());
	}

	public List<String> getNicknamesForUser(long usersnowflake, long guildsnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select displayName from nicknames where usersnowflake = :snowflake and guildsnowflake = :guildsnowflake")
				.bind("snowflake", usersnowflake).bind("guildsnowflake", guildsnowflake).mapTo(String.class).list());
	}

	public List<Nickname> getNicknameForUserWithName(String displayName) {
		/*return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from nicknames ").mapToBean(Nickname.class).list().stream()
						.filter(nickname -> nickname.getDisplayName().toLowerCase().contains(displayName.toLowerCase()))
						.collect(Collectors.toList()));*/
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select * from nicknames where lower(displayName) like '%" + displayName.toLowerCase() + "%'")
				.mapToBean(Nickname.class).list());
	}

	public List<Nickname> getNicknameForUserWithName(String displayName, long guildsnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select * from nicknames where lower(displayName) like '%" + displayName.toLowerCase()
						+ "%' and guildsnowflake = :guildsnowflake").bind("guildsnowflake", guildsnowflake)
				.mapToBean(Nickname.class).list());
	}

}
