package com.soa.rs.discordbot.v3.jdbi;

public class SettingsUtility {

	public void createSettingsTable() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute(
				"create table settings (settingkey varchar(255) not null, settingvalue varchar(255), primary key(settingkey))"));
	}

	public String getValueForKey(String key) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createQuery("select settingvalue from settings where settingkey = :settingkey").bind("settingkey", key)
						.mapTo(String.class).first());
	}

	public int updateValueForKey(String key, String value) {
		return JdbiWrapper.getInstance().getJdbi().withHandle(
				handle -> handle.createUpdate("update settings set settingvalue = :settingvalue where settingkey = :settingkey")
						.bind("settingvalue", value).bind("settingkey", key).execute());
	}
}
