package com.soa.rs.discordbot.v3.jdbi;

import java.util.List;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildServerUser;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;

public class GuildNicknameUtility {

	public List<GuildUser> getGuildUser(String name) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select distinct u.* from users u left join nicknames n on u.snowflake = n.usersnowflake and u.guildsnowflake = n.guildsnowflake where lower(u.username) like '%"
						+ name.toLowerCase() + "%' or lower(u.knownname) like '%" + name.toLowerCase()
						+ "%' or lower(u.displayname) like '%" + name.toLowerCase()
						+ "%' or lower(n.displayname) like '%" + name.toLowerCase() + "%'").mapToBean(GuildUser.class)
				.list());
	}

	public List<GuildUser> getGuildUserWithNameInGuild(String name, long guildSnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select distinct u.* from users u left join nicknames n on u.snowflake = n.usersnowflake and u.guildsnowflake = n.guildsnowflake where (lower(u.username) like '%"
						+ name.toLowerCase() + "%' or lower(u.knownname) like '%" + name.toLowerCase()
						+ "%' or lower(u.displayname) like '%" + name.toLowerCase()
						+ "%' or lower(n.displayname) like '%" + name.toLowerCase()
						+ "%') and u.guildsnowflake = :guildSnowflake").bind("guildSnowflake", guildSnowflake)
				.mapToBean(GuildUser.class).list());
	}

	public List<GuildServerUser> getGuildUserWithServerName(String name) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select distinct u.*, g.guildName from users u "
						+ "left join nicknames n on u.snowflake = n.usersnowflake and u.guildsnowflake = n.guildsnowflake "
						+ "left join guilds g on u.guildsnowflake = g.snowflake" + " where lower(u.username) like '%"
						+ name.toLowerCase() + "%' or lower(u.knownname) like '%" + name.toLowerCase()
						+ "%' or lower(u.displayname) like '%" + name.toLowerCase()
						+ "%' or lower(n.displayname) like '%" + name.toLowerCase() + "%'")
				.mapToBean(GuildServerUser.class).list());
	}

	public List<GuildServerUser> getGuildUserWithNameInGuildWithServerName(String name, long guildSnowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select distinct u.*, g.guildName from users u "
						+ "left join nicknames n on u.snowflake = n.usersnowflake and u.guildsnowflake = n.guildsnowflake "
						+ "left join guilds g on u.guildsnowflake = g.snowflake" + " where (lower(u.username) like '%"
						+ name.toLowerCase() + "%' or lower(u.knownname) like '%" + name.toLowerCase()
						+ "%' or lower(u.displayname) like '%" + name.toLowerCase()
						+ "%' or lower(n.displayname) like '%" + name.toLowerCase()
						+ "%') and u.guildsnowflake = :guildSnowflake").bind("guildSnowflake", guildSnowflake)
				.mapToBean(GuildServerUser.class).list());
	}
}
