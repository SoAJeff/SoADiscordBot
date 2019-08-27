package com.soa.rs.discordbot.v3.jdbi;

import com.soa.rs.discordbot.v3.jaxb.DatabaseConnection;
import com.soa.rs.discordbot.v3.jaxb.JdbcProperties;
import com.soa.rs.discordbot.v3.jaxb.JdbcProperty;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DatabaseInitializerTest {

	@BeforeClass
	public static void setup() {
		SoaLogging.initializeLogging();
	}

	@Test
	public void testGenerateUrlNoProperties() {
		DatabaseConnection cfg = new DatabaseConnection();
		cfg.setJdbcUrl("jdbc:mysql://localhost:3306");
		cfg.setDbName("testbot");

		DatabaseInitializer init = new DatabaseInitializer();
		String url = init.generateJdbcUrl(cfg);

		Assert.assertEquals("jdbc:mysql://localhost:3306/testbot", url);
	}

	@Test
	public void testGenerateUrlWithSingleProperty() {
		DatabaseConnection cfg = new DatabaseConnection();
		cfg.setJdbcUrl("jdbc:mysql://localhost:3306");
		cfg.setDbName("testbot");
		JdbcProperties properties = new JdbcProperties();
		JdbcProperty property = new JdbcProperty();
		property.setPropertyKey("serverTimezone");
		property.setPropertyValue("America/New_York");
		properties.getProperty().add(property);
		cfg.setJdbcProperties(properties);

		DatabaseInitializer init = new DatabaseInitializer();
		String url = init.generateJdbcUrl(cfg);

		Assert.assertEquals("jdbc:mysql://localhost:3306/testbot?serverTimezone=America/New_York", url);
	}

	@Test
	public void testGenerateUrlWithMultipleProperties() {
		DatabaseConnection cfg = new DatabaseConnection();
		cfg.setJdbcUrl("jdbc:mysql://localhost:3306");
		cfg.setDbName("testbot");
		JdbcProperties properties = new JdbcProperties();
		JdbcProperty property = new JdbcProperty();
		property.setPropertyKey("serverTimezone");
		property.setPropertyValue("America/New_York");
		properties.getProperty().add(property);

		JdbcProperty property2 = new JdbcProperty();
		property2.setPropertyKey("someKey");
		property2.setPropertyValue("someValue");
		properties.getProperty().add(property2);
		cfg.setJdbcProperties(properties);

		DatabaseInitializer init = new DatabaseInitializer();
		String url = init.generateJdbcUrl(cfg);

		Assert.assertEquals("jdbc:mysql://localhost:3306/testbot?serverTimezone=America/New_York&someKey=someValue",
				url);
	}

	@Test
	public void testInitializeDatabaseValidUrl() {
		DatabaseConnection cfg = new DatabaseConnection();
		cfg.setJdbcUrl("jdbc:mysql://localhost:3306");
		cfg.setDbName("testbot");
		cfg.setDbUsername("user");
		cfg.setDbPassword("pass");

		DatabaseInitializer init = new DatabaseInitializer();
		Assert.assertTrue(init.initializeDatabase(cfg));
		Assert.assertNotNull(init.getJdbi());
	}

	@Test
	public void testInitializeDatabaseNullConfig() {
		DatabaseInitializer init = new DatabaseInitializer();
		init.initializeDatabase(null);
	}
}
