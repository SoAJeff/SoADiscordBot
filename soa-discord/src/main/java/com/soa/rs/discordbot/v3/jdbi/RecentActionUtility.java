package com.soa.rs.discordbot.v3.jdbi;

import java.util.Date;
import java.util.List;

import com.soa.rs.discordbot.v3.jdbi.entities.RecentAction;
import com.soa.rs.discordbot.v3.jdbi.entities.UsernameRecentAction;

public class RecentActionUtility {

	public void createRecentActionsTable() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute(
				"create table recentactions (actionId int not null auto_increment primary key, date timestamp not null, guildsnowflake bigint not null, usersnowflake bigint not null, action varchar(255) not null, originalvalue varchar(255), newvalue varchar(255))"));
	}

	public void addRecentAction(long guildSnowflake, long userSnowflake, String action) {
		addRecentAction(guildSnowflake, userSnowflake, action, null, null);
	}

	public void addRecentAction(long guildSnowflake, long userSnowflake, String action, String newValue) {
		addRecentAction(guildSnowflake, userSnowflake, action, null, newValue);
	}

	public void addRecentAction(long guildSnowflake, long userSnowflake, String action, String originalValue,
			String newValue) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute(
				"insert into recentactions (date, guildsnowflake, usersnowflake, action, originalvalue, newvalue) values"
						+ "(?, ?, ?, ?, ?, ?)", new Date(), guildSnowflake, userSnowflake, action, originalValue,
				newValue));

	}

	public void addRecentAction(RecentAction action) {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.createUpdate(
				"insert into recentactions (date, guildsnowflake, usersnowflake, action, originalvalue, newvalue) values (:date, :guildSnowflake, :userSnowflake, :action, :originalValue, :newValue)")
				.bind("date", new Date()).bindBean(action).execute());
	}

	public List<RecentAction> getRecentActions() {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from recentactions").mapToBean(RecentAction.class).list());
	}

	public List<RecentAction> getRecentActionsLimitByN(int limit) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select * from recentactions order by actionId desc limit " + limit)
						.mapToBean(RecentAction.class).list());
	}

	public List<UsernameRecentAction> getRecentActionsLimitByNWithUsername(int limit) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select r.*, u.username from recentactions r left join users u on u.snowflake = r.usersnowflake and u.guildsnowflake = r.guildsnowflake "
						+ "order by actionId desc limit " + limit).mapToBean(UsernameRecentAction.class).list());
	}

	public List<UsernameRecentAction> getRecentActionsLimitByNWithUsername(int limit, long id) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery(
				"select r.*, u.username from recentactions r left join users u on u.snowflake = r.usersnowflake and u.guildsnowflake = r.guildsnowflake "
						+ "where r.guildsnowflake = :id order by actionId desc limit " + limit).bind("id", id)
				.mapToBean(UsernameRecentAction.class).list());
	}
}
