package com.soa.rs.discordbot.v3.bot;

/**
 * The RunBot class serves as the main class for the Discord bot.
 *
 */
public class BotLauncher {

	public static void main(String[] args) {
		ConfigureBot launcher = new ConfigureBot(args);
		launcher.launch();
	}

}
