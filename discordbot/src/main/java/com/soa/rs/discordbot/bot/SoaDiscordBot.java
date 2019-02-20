package com.soa.rs.discordbot.bot;

import java.time.LocalDateTime;

import com.soa.rs.discordbot.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.util.SoaLogging;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

/**
 * The SoaDiscordBot class contains the necessary logic to log the bot into
 * discord and register appropriate event listeners for the bot to interact with
 * Discord.
 * 
 */
public class SoaDiscordBot {

	/**
	 * Discord client object; interaction with Discord will be done through this
	 * object.
	 */
	private IDiscordClient client = null;

	/**
	 * Start the bot. This will log the bot into Discord and register appropriate
	 * event listeners
	 */
	public void start() {
		SoaLogging.getLogger().info("Logging-in bot with Token: " + DiscordCfgFactory.getConfig().getDiscordToken());
		try {
			client = loginClient();
		} catch (DiscordException e) {
			// TODO Auto-generated catch block
			SoaLogging.getLogger().error("Error with logging in", e);
		}
		if (client != null) {
			SoaLogging.getLogger().info("Logged in to Discord");
			DiscordCfgFactory.getInstance().setLaunchTime(LocalDateTime.now());
			EventDispatcher dispatcher = client.getDispatcher();
			dispatcher.registerListener(new ReadyEventListener());
			dispatcher.registerListener(new MessageReceivedEventListener());
			dispatcher.registerListener(new ReconnectedEventListener());
		}
	}

	/**
	 * Process logging the bot into Discord
	 * 
	 * @return logged-in Discord client
	 * @throws DiscordException
	 */
	private IDiscordClient loginClient() throws DiscordException {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(DiscordCfgFactory.getConfig().getDiscordToken()).setMaxReconnectAttempts(1000);
		return clientBuilder.login();
	}

	/**
	 * Log the bot out of Discord.
	 */
	public void disconnect() {
		try {
			client.logout();
		} catch (DiscordException e) {
			SoaLogging.getLogger().error("Error when disconnecting from Discord", e);
		}
	}

}
