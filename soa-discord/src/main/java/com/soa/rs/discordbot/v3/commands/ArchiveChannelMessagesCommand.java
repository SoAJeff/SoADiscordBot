package com.soa.rs.discordbot.v3.commands;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.util.FileDownloader;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.apache.commons.io.IOUtils;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Command(triggers={".archive"})
public class ArchiveChannelMessagesCommand extends AbstractCommand {

	private FileOutputStream fileOutputStream;
	long index = 0;
	@Override
	public void initialize() {
		//This should be false by default, and should only be compiled in as enabled when it needs to be used.
		setEnabled(false);
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		if (event.getMessage().getAuthor().isPresent()) {
			//Only Jeff can run this command, for safety.
			if (event.getMessage().getAuthor().get().getId().asLong() != 134426385019043840L)
				return Mono.empty();
		}
		else
			return Mono.empty();
		return event.getMessage().getChannel()
				.flatMap(messageChannel -> {
					try {
						fileOutputStream = new FileOutputStream(new File("Channel_ID_" + messageChannel.getId().asString() + "_Archive.txt"));
						return Mono.just(messageChannel);
					}
					catch(Exception e)
					{
						return Mono.error(e);
					}
				})
				.flatMapMany(messageChannel -> messageChannel.getMessagesBefore(event.getMessage().getId()))
				.flatMapSequential(message -> messageToString(message))
				.flatMapSequential(string -> {
					try {
						fileOutputStream.write(string.getBytes());
					} catch (Exception e) {
						SoaLogging.getLogger(this).error("Error when writing data", e);
					}
					return Flux.empty();
				})
				.doOnComplete(()-> {
					try {
						fileOutputStream.close();
					} catch (Exception e) {
						SoaLogging.getLogger(this).error("Error closing file", e);
					}
				})
				.then(event.getMessage().getChannel()
						.flatMap(messageChannel -> messageChannel.createMessage("Completed Archive, " + index + " messages archived")))
				.then(Mono.fromRunnable(()->index=0));

	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> execute(ButtonInteractionEvent event) {
		return Mono.empty();
	}

	//Message.timestamp needs to be formatted...
	public Mono<String> messageToString(Message message) {
		index++;
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.US)
				.withZone(ZoneId.systemDefault());
		Set<String> attaches = new HashSet<>();

		if (message.getAttachments().size() > 0) {
			File path = new File("attachments");
			path.mkdirs();

			for (Attachment attachment : message.getAttachments()) {
				File name;
				if(attachment.getFilename().startsWith("unknown"))
					name = new File("attachments", index + attachment.getFilename());
				else
					name = new File("attachments", attachment.getFilename());
				try (InputStream is = FileDownloader.downloadFile(attachment.getUrl());
						ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(is));
						FileOutputStream fos = new FileOutputStream(name)) {
					SoaLogging.getLogger(this).debug("Downloading: " + name.getName());
					IOUtils.copy(bais, fos);
					attaches.add(name.getName());
				} catch (Exception e) {
					SoaLogging.getLogger(this).error("error reading from stream", e);
				}
			}
		}


		StringBuilder sb = new StringBuilder();
		sb.append("[").append(formatter.format(message.getTimestamp())).append("] @")
				.append(message.getAuthor().get().getUsername()).append("#")
				.append(message.getAuthor().get().getDiscriminator()).append(": ");
		Optional.of(message.getContent()).ifPresent(sb::append);
		sb.append(System.lineSeparator());
		if(attaches.size() > 0) {
			sb.append("Included attachments: ");
			for(String attachment : attaches)
			{
				sb.append(attachment);
				sb.append(", ");
			}
			attaches.clear();
			sb.append(System.lineSeparator());
		}

		if((index % 100) == 0)
			SoaLogging.getLogger(this).debug(index + " messages saved so far.");

		return Mono
				.just(sb.toString());
	}
}
