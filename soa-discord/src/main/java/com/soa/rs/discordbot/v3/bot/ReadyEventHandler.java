package com.soa.rs.discordbot.v3.bot;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.schedule.EventListTask;
import com.soa.rs.discordbot.v3.schedule.ForumNewsTask;
import com.soa.rs.discordbot.v3.schedule.RsNewsTask;
import com.soa.rs.discordbot.v3.util.DateAnalyzer;
import com.soa.rs.discordbot.v3.util.FileDownloader;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.apache.commons.io.IOUtils;

import discord4j.core.DiscordClient;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Image;
import reactor.core.publisher.Flux;

public class ReadyEventHandler {

	private DiscordClient client;
	private boolean initialized = false;

	public ReadyEventHandler(DiscordClient client) {
		this.client = client;
	}

	public void handle() {
		//Only run once, In a case of a major disconnection D4J will re-run this upon reconnection
		if (!initialized) {
			DiscordCfgFactory.getInstance().setLaunchTime(LocalDateTime.now());
			setDiscordUserSettings();
			setupSchedulers();
			initialized = true;
		}

	}

	private void setupSchedulers() {
		EventListTask eventTask = new EventListTask();
		eventTask.setClient(client);
		if (eventTask.initialize()) {
			SoaLogging.getLogger(this).info("Scheduling Event List Task");
			Flux.interval(Duration.ofSeconds(DateAnalyzer.computeNextDelay(0, 1, 0)), Duration.ofSeconds(60 * 60 * 24))
					.doOnNext(ignored -> eventTask.run()).onErrorContinue(
					(err, obj) -> SoaLogging.getLogger(this).error("Error when running task:" + err.getMessage(), err))
					.subscribe(null, err -> SoaLogging.getLogger(this)
							.error("Error when running task:" + err.getMessage(), err));
		}

		ForumNewsTask forumNewsTask = new ForumNewsTask();
		forumNewsTask.setClient(client);
		if (forumNewsTask.initialize()) {
			SoaLogging.getLogger(this).info("Scheduling Forum News Task");
			Flux.interval(Duration.ofSeconds(DateAnalyzer.calculateMinutesUntil30()), Duration.ofMinutes(30))
					.doOnNext(ignored -> forumNewsTask.run()).onErrorContinue(
					(err, obj) -> SoaLogging.getLogger(this).error("Error when running task:" + err.getMessage(), err))
					.subscribe(null, err -> SoaLogging.getLogger(this)
							.error("Error when running task:" + err.getMessage(), err));
		}

		RsNewsTask rsNewsTask = new RsNewsTask();
		rsNewsTask.setClient(client);
		if (rsNewsTask.initialize()) {
			SoaLogging.getLogger(this).info("Scheduling RS News Task");
			Flux.interval(Duration.ofSeconds(DateAnalyzer.calculateMinutesUntil10()), Duration.ofMinutes(10))
					.doOnNext(ignored -> rsNewsTask.run()).onErrorContinue(
					(err, obj) -> SoaLogging.getLogger(this).error("Error when running task:" + err.getMessage(), err))
					.subscribe(null, err -> SoaLogging.getLogger(this)
							.error("Error when running task:" + err.getMessage(), err));
		}

	}

	/**
	 * Sets up the bot's user settings
	 */
	private void setDiscordUserSettings() {

		if (DiscordCfgFactory.getInstance().getAvatarUrl() != null) {
			SoaLogging.getLogger(this).info("Setting bot avatar");

			try (InputStream is = FileDownloader.downloadFile(DiscordCfgFactory.getInstance().getAvatarUrl())) {
				client.edit(userEditSpec -> {
					try {
						Image.Format format = FileDownloader
								.getFormatForProvidedURL(DiscordCfgFactory.getInstance().getAvatarUrl());
						userEditSpec.setAvatar(Image.ofRaw(IOUtils.toByteArray(is), format));
					} catch (IOException e) {
						SoaLogging.getLogger(this).error("Error setting image as avatar", e);

					} catch (Exception e) {
						SoaLogging.getLogger(this).error("Error determining format of image", e);
					}
				}).subscribe();
			} catch (Exception e) {
				SoaLogging.getLogger(this).error("Error downloading the external image", e);
			}

		}
		if (DiscordCfgFactory.getInstance().getBotname() != null) {

			SoaLogging.getLogger(this)
					.info("Setting bot username to '" + DiscordCfgFactory.getInstance().getBotname() + "'");
			client.edit(userEditSpec -> userEditSpec.setUsername(DiscordCfgFactory.getInstance().getBotname()))
					.subscribe();
		}

	}

}
