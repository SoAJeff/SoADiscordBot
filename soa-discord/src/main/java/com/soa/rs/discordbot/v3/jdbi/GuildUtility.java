package com.soa.rs.discordbot.v3.jdbi;

import java.util.List;
import java.util.Optional;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildEntry;

public class GuildUtility {

	public void createGuildsTable() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle
				.execute("create table guilds (snowflake bigint primary key, guildName varchar(255) not null)"));
	}

	public void addNewGuild(long snowflake, String guildName) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle
				.execute("insert into guilds (snowflake, guildName) values (?, ?)", snowflake, guildName));
	}

	public void addNewGuild(GuildEntry entry) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle
				.createUpdate("insert into guilds (snowflake, guildName) values (:snowflake, :guildName)")
				.bindBean(entry).execute());
	}

	public void updateGuildInfo(long snowflake, String guildName) {
		JdbiWrapper.getInstance().getJdbi().useHandle(
				handle -> handle.createUpdate("update guilds set guildName = :guildName where snowflake = :snowflake")
						.bind("guildName", guildName).bind("snowflake", snowflake).execute());
	}

	public void updateGuildInfo(GuildEntry entry) {
		JdbiWrapper.getInstance().getJdbi().useHandle(
				handle -> handle.createUpdate("update guilds set guildName = :guildName where snowflake = :snowflake")
						.bind("guildName", entry.getGuildName()).bind("snowflake", entry.getSnowflake()).execute());
	}

	public List<GuildEntry> getAllGuilds() {
		return JdbiWrapper.getInstance().getJdbi()
				.withHandle(handle -> handle.createQuery("select * from guilds").mapToBean(GuildEntry.class).list());
	}

	public Optional<String> getNameForGuild(long snowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select guildName from guilds where snowflake = :snowflake")
						.bind("snowflake", snowflake).mapTo(String.class).findOne());
	}

	public List<GuildEntry> getGuildById(long snowflake) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from guilds where snowflake = :snowflake")
						.bind("snowflake", snowflake).mapToBean(GuildEntry.class).list());
	}

	public List<GuildEntry> getGuildByName(String name) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from guilds where guildName = :guildName")
						.bind("guildName", name).mapToBean(GuildEntry.class).list());
	}
}
