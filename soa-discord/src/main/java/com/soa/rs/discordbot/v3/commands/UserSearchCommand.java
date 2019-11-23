package com.soa.rs.discordbot.v3.commands;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.jdbi.GuildNicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;
import com.soa.rs.discordbot.v3.jdbi.GuildUtility;
import com.soa.rs.discordbot.v3.jdbi.NicknameUtility;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildEntry;
import com.soa.rs.discordbot.v3.jdbi.entities.GuildServerUser;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Command(triggers = { ".usersearch", ".user-search" })
public class UserSearchCommand extends AbstractCommand {

	private GuildUserUtility guildUserUtility = new GuildUserUtility();
	private GuildUtility guildUtility = new GuildUtility();
	private NicknameUtility nicknameUtility = new NicknameUtility();
	private GuildNicknameUtility guildNicknameUtility = new GuildNicknameUtility();

	@Override
	public void initialize() {
		setEnabled(DiscordCfgFactory.getInstance().isUserTrackingEnabled());
		if (DiscordCfgFactory.getInstance().isUserTrackingEnabled()) {
			addHelpMsg(".usersearch", "Search the users database for information about a specific user.");
		}
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		Search search = determineSearch(event);
		if (search.getSearchTerm().equals(""))
			return Mono.empty();

		Set<GuildServerUser> users = new HashSet<>();
		if (search.getServerName() == null) {
			if (event.getGuildId().isPresent()) {
				//Is in a guild, so search for that guild.
				users.addAll(guildNicknameUtility.getGuildUserWithNameInGuildWithServerName(search.getSearchTerm(),
						event.getGuildId().get().asLong()));
			} else {
				//Was sent in a private message, so get everything
				users.addAll(guildNicknameUtility.getGuildUserWithServerName(search.getSearchTerm()));
			}
		} else {
			long id = getServerIdForName(search.getServerName());
			if (id == 0) {
				return event.getMessage().getChannel()
						.flatMap(messageChannel -> messageChannel.createMessage("No server with that name.")).then();
			} else if (id == 1) {
				return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(
						"More than 1 server with that name, provide ID or send search from server to search.")).then();
			}
			users.addAll(guildNicknameUtility.getGuildUserWithNameInGuildWithServerName(search.getSearchTerm(), id));
		}

		SoaLogging.getLogger(this).debug("After searching, size of users is " + users.size());

		if (users.size() == 0) {
			return event.getMessage().getChannel()
					.flatMap(messageChannel -> messageChannel.createMessage("No results for search.")).then();
		}

		return Flux.fromIterable(users).flatMap(user -> event.getMessage().getChannel()
				.flatMap(messageChannel -> messageChannel.createEmbed(createEmbed(user)))).then();
	}

	public Search determineSearch(MessageCreateEvent event) {
		if (!event.getMessage().getContent().isPresent()) {
			return new Search("");
		}

		int i = 1;
		String[] content = event.getMessage().getContent().get().split(" ");
		StringBuilder sb = new StringBuilder();
		StringBuilder serverBuilder = new StringBuilder();
		long id = 0;
		boolean server = false;

		while (i < content.length) {
			if (!content[i].equalsIgnoreCase("-server") && !content[i].equalsIgnoreCase("-serverid") && !server) {
				sb.append(" ");
				sb.append(content[i]);
			} else if (content[i].equalsIgnoreCase("-serverid")) {
				i++;
				try {
					id = Long.parseLong(content[i]);
					break;
				} catch (NumberFormatException ex) {
					SoaLogging.getLogger(this).error("User provided an ID but it was not a long");
				}
			} else {
				if (!server) {
					server = true;
				} else {
					if (content[i].equalsIgnoreCase("-serverid"))
						break;
					serverBuilder.append(" ");
					serverBuilder.append(content[i]);
				}
			}
			i++;
		}
		if (!server) {
			if (id != 0)
				return new Search(sb.toString().trim(), id);
			else
				return new Search(sb.toString().trim());
		} else {
			return new Search(sb.toString().trim(), serverBuilder.toString().trim());
		}
	}

	private long getServerIdForName(String name) {
		List<GuildEntry> entries = this.guildUtility.getGuildByName(name);
		//No server
		if (entries.size() < 1)
			return 0;
			//More than 1 server
		else if (entries.size() > 1)
			return 1;
		return entries.get(0).getSnowflake();
	}

	public Consumer<EmbedCreateSpec> createEmbed(GuildServerUser user) {
		return embedCreateSpec -> {
			SoaLogging.getLogger(this).trace("Creating embed for user " + user.getUsername());
			embedCreateSpec.setTitle(user.getDisplayName());
			embedCreateSpec.setDescription(user.getUsername() + " in server: " + user.getGuildName());

			if (user.getKnownName() != null && user.getKnownName().trim().length() > 0) {
				embedCreateSpec.addField("Known as", user.getKnownName(), false);
			}
			List<String> nicknames = nicknameUtility.getNicknamesForUser(user.getSnowflake(), user.getGuildSnowflake());
			StringBuilder sb = new StringBuilder();
			Iterator<String> iter = nicknames.iterator();
			int nameCount = 0;
			while (iter.hasNext()) {
				String name = iter.next();
				sb.append(name);
				nameCount++;
				if (sb.length() > 975) {
					sb.append(", and " + (nicknames.size() - nameCount) + " additional names.");
					break;
				}
				if (iter.hasNext()) {
					sb.append(", ");
				}

			}
			embedCreateSpec.addField("All Display Names", sb.toString(), false);

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss z");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

			embedCreateSpec.addField("Joined server date", sdf.format(user.getJoinedServer()), true);

			if (sdf.format(user.getLeftServer()).equals(sdf.format(Date.from(Instant.EPOCH)))) {
				embedCreateSpec.addField("Last seen date", sdf.format(user.getLastSeen()), true);
				embedCreateSpec.addField("Last active date", sdf.format(user.getLastActive()), false);
			} else {
				embedCreateSpec.addField("Left server date", sdf.format(user.getLeftServer()), true);
			}

		};
	}

	class Search {
		private String searchTerm;
		private String serverName;
		private long serverId;

		public Search(String searchTerm) {
			this.searchTerm = searchTerm;
		}

		public Search(String searchTerm, long serverId) {
			this.searchTerm = searchTerm;
			this.serverId = serverId;
		}

		public Search(String searchTerm, String serverName) {
			this.searchTerm = searchTerm;
			this.serverName = serverName;
		}

		public String getSearchTerm() {
			return searchTerm;
		}

		public String getServerName() {
			return serverName;
		}

		public long getServerId() {
			return serverId;
		}
	}
}
