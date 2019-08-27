package com.soa.rs.discordbot.v3.jdbi;

import java.util.Iterator;

import com.soa.rs.discordbot.v3.jaxb.DatabaseConnection;
import com.soa.rs.discordbot.v3.jaxb.JdbcProperty;

import org.jdbi.v3.core.Jdbi;

public class DatabaseInitializer {

	private Jdbi jdbi;

	public boolean initializeDatabase(DatabaseConnection cfg) {
		if (cfg != null) {
			String url = generateJdbcUrl(cfg);

			this.jdbi = Jdbi.create(url, cfg.getDbUsername(), cfg.getDbPassword());
			return true;
		}
		return false;
	}

	String generateJdbcUrl(DatabaseConnection cfg) {
		StringBuilder sb = new StringBuilder();
		sb.append(cfg.getJdbcUrl());
		sb.append("/");
		sb.append(cfg.getDbName());
		if (cfg.getJdbcProperties() != null) {
			sb.append("?");
			Iterator<JdbcProperty> iter = cfg.getJdbcProperties().getProperty().iterator();
			while (iter.hasNext()) {
				JdbcProperty property = iter.next();
				sb.append(property.getPropertyKey());
				sb.append("=");
				sb.append(property.getPropertyValue());
				if (iter.hasNext())
					sb.append("&");
			}
		}

		return sb.toString();
	}

	public Jdbi getJdbi() {
		return jdbi;
	}
}
