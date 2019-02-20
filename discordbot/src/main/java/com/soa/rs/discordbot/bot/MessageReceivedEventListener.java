package com.soa.rs.discordbot.bot;

import com.soa.rs.discordbot.bot.events.BenIsNoobEvent;
import com.soa.rs.discordbot.bot.events.OldWikiEvent;
import com.soa.rs.discordbot.bot.events.SoaAdminEvent;
import com.soa.rs.discordbot.bot.events.SoaBotInfoEvent;
import com.soa.rs.discordbot.bot.events.SoaDjPlsEvent;
import com.soa.rs.discordbot.bot.events.SoaEventListerTask;
import com.soa.rs.discordbot.bot.events.SoaHelpEvent;
import com.soa.rs.discordbot.bot.events.SoaMusicPlayer;
import com.soa.rs.discordbot.bot.events.UserBanEvent;
import com.soa.rs.discordbot.bot.events.UserTrackingQuery;
import com.soa.rs.discordbot.bot.events.trivia.TriviaManager;
import com.soa.rs.discordbot.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.util.SoaDiscordBotConstants;
import com.soa.rs.discordbot.util.SoaLogging;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

/**
 * The MessageReceivedEventListener handles any commands that may come in via
 * messages typed within a Discord channel
 */
public class MessageReceivedEventListener implements IListener<MessageReceivedEvent> {

	/*
	 * Recommend storing any classes for events as fields, as once they are created
	 * they hopefully won't need to be recreated
	 */
	/**
	 * Music player instance for the bot.
	 */
	private SoaMusicPlayer player = null;
	/**
	 * Event Lister for the MessageReceivedEvent
	 */
	private SoaEventListerTask eventListerTask = null;

	private TriviaManager triviaManager = null;

	private OldWikiEvent wikiEvent;

	/**
	 * Handles the MessageReceivedEvent
	 * 
	 * @param event
	 *            The Message Received Event
	 */
	public void handle(MessageReceivedEvent event) {
		IMessage msg = event.getMessage();

		if (msg.getContent().startsWith(SoaDiscordBotConstants.BOT_PREFIX)) {
			String command = msg.getContent().replaceFirst(SoaDiscordBotConstants.BOT_PREFIX, "");
			String[] args = command.split(" ");

			/*
			 * Music Player Command
			 */
			if (args[0].equalsIgnoreCase("music")) {
				if (DiscordCfgFactory.getConfig().getMusicPlayer() != null
						&& DiscordCfgFactory.getConfig().getMusicPlayer().isEnabled()) {
					if (player == null) {
						player = new SoaMusicPlayer(msg);
					}
					try {
						player.setMsg(msg);
						player.handleMusicArgs(event, args);
					} catch (Exception e) {
						SoaLogging.getLogger().error("Exception thrown in MusicPlayer", e);
					}
				}
			}

			/*
			 * Event Lister command
			 */
			else if (args[0].equalsIgnoreCase("events")) {
				if (DiscordCfgFactory.getConfig().getEventListingEvent() != null
						&& DiscordCfgFactory.getConfig().getEventListingEvent().isEnabled()) {
					if (eventListerTask == null) {
						eventListerTask = new SoaEventListerTask(
								DiscordCfgFactory.getConfig().getEventListingEvent().getUrl(), event.getClient(),
								msg.getChannel());
						eventListerTask.run();
					} else {
						eventListerTask.setChannel(msg.getChannel());
						eventListerTask.run();
					}
				}
			}

			else if (args[0].equalsIgnoreCase("trivia")) {
				if (DiscordCfgFactory.getConfig().getTrivia() != null
						&& DiscordCfgFactory.getConfig().getTrivia().isEnabled()) {
					if (triviaManager == null) {
						triviaManager = new TriviaManager();
					}
					triviaManager.setMsg(msg);
					triviaManager.executeCmd(args);
				}
			}

			else if (args[0].equalsIgnoreCase("info")) {
				SoaBotInfoEvent infoEvent = new SoaBotInfoEvent(event);
				infoEvent.executeEvent();
			}

			else if (args[0].equalsIgnoreCase("admin")) {
				if (DiscordCfgFactory.getConfig().getAdminEvent() != null
						&& DiscordCfgFactory.getConfig().getAdminEvent().isEnabled()) {
					SoaAdminEvent adminEvent = new SoaAdminEvent(event);
					adminEvent.setMustHavePermission(
							DiscordCfgFactory.getConfig().getAdminEvent().getAllowedRoles().getRole());
					adminEvent.setArgs(args);
					adminEvent.executeEvent();
				}
			}

			else if (args[0].equalsIgnoreCase("user")) {
				if (DiscordCfgFactory.getConfig().getUserTrackingEvent() != null
						&& DiscordCfgFactory.getConfig().getUserTrackingEvent().isEnabled()) {
					UserTrackingQuery queryEvent = new UserTrackingQuery(event);
					queryEvent.setArgs(args);
					queryEvent.executeEvent();
				}

			}

			//Ban will use admin permissions
			else if (args[0].equalsIgnoreCase("ban")) {
				if (DiscordCfgFactory.getConfig().getAdminEvent() != null && DiscordCfgFactory.getConfig()
						.getAdminEvent().isEnabled()) {
					UserBanEvent banEvent = new UserBanEvent(event);
					banEvent.setMustHavePermission(
							DiscordCfgFactory.getConfig().getAdminEvent().getAllowedRoles().getRole());
					banEvent.executeEvent();
				}
			}

			else if (args[0].equalsIgnoreCase("help")) {
				SoaHelpEvent helpEvent = new SoaHelpEvent(event);
				helpEvent.executeEvent();
			}
		} else if (msg.getContent().toLowerCase().contains("dj pls")
				|| msg.getContent().toLowerCase().contains("dj is a noob")
				|| msg.getContent().toLowerCase().contains("dj is a nublet")) {
			if (DiscordCfgFactory.getConfig().getDjPlsEvent() != null
					&& DiscordCfgFactory.getConfig().getDjPlsEvent().isEnabled()) {
				SoaDjPlsEvent djPlsEvent = new SoaDjPlsEvent(event);
				djPlsEvent.executeEvent();
			}
		} //259152327800389632L = Ben
		else if (msg.getMentions().contains(event.getClient().getUserByID(259152327800389632L)) && msg.getContent().toLowerCase().contains("noob"))
		{
			BenIsNoobEvent benEvent = new BenIsNoobEvent(event);
			benEvent.executeEvent();
		}

		if(wikiEvent == null)
		{
			wikiEvent= new OldWikiEvent(event);
		}
		else {
			wikiEvent.setEvent(event);
		}
		wikiEvent.executeEvent();

	}
}
