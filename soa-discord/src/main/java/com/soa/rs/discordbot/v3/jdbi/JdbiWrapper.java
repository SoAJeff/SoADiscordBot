package com.soa.rs.discordbot.v3.jdbi;

import org.jdbi.v3.core.Jdbi;

public class JdbiWrapper {
	private static JdbiWrapper ourInstance = new JdbiWrapper();

	private Jdbi jdbi;

	public static JdbiWrapper getInstance() {
		return ourInstance;
	}

	private JdbiWrapper() {
	}

	public void setJdbi(Jdbi jdbi) {
		this.jdbi = jdbi;
	}

	public Jdbi getJdbi() {
		return jdbi;
	}


}
