package com.soa.rs.discordbot.v3.commands;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.ipb.member.ForumRank;
import com.soa.rs.discordbot.v3.ipb.member.Member;
import com.soa.rs.discordbot.v3.ipb.member.MemberFetcher;
import com.soa.rs.discordbot.v3.ipb.member.MemberResults;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import reactor.core.publisher.Mono;

@Interaction(trigger = "birthdays")
public class BirthdayInteraction extends AbstractCommand {

	@Override
	public void initialize() {
		setEnabled(true);
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return null;
	}

	@Override
	public Mono<Void> execute(ChatInputInteractionEvent event) {
		String month = event.getOption("month").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asString).get();

		return event.deferReply().withEphemeral(true).then(Mono.fromCallable(() -> performForumLookup(month))
				.flatMap(s -> event.createFollowup(s).withEphemeral(true))).onErrorResume(throwable -> {
			SoaLogging.getLogger(this).error("Error when getting birthdays", throwable);
			return event.createFollowup("Error when looking up birthdays, you may have spelled the month wrong.")
					.withEphemeral(true).then(Mono.empty());
		}).then();
	}

	private String performForumLookup(String month) {
		int[] groups = { ForumRank.ELDAR.getId(), ForumRank.LIAN.getId(), ForumRank.ADMINISTRATOR.getId(),
				ForumRank.ARQUENDI.getId(), ForumRank.ADELE.getId(), ForumRank.VORONWE.getId(),
				ForumRank.ELENDUR.getId(), ForumRank.SADRON.getId(), ForumRank.ATHEM.getId(), ForumRank.MYRTH.getId(),
				ForumRank.TYLAR.getId() };

		List<MemberBirthdate> members = new ArrayList<>();
		MemberFetcher fetcher = new MemberFetcher();
		int monthInt = Month.valueOf(month.toUpperCase()).getValue();

		MemberResults results = fetcher.fetchMembers(1, groups);
		int pages = results.getTotalPages();
		reviewResults(members, monthInt, results);
		for (int i = 2; i <= pages; i++) {
			results = fetcher.fetchMembers(i, groups);
			reviewResults(members, monthInt, results);
		}

		SoaLogging.getLogger(this).debug("Total number of members with birthdays this month: " + members.size());

		Collections.sort(members);

		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
		SimpleDateFormat finaldate = new SimpleDateFormat("MMMM dd");
		sb.append("The following forum members have birthdays during the month of " + month);
		sb.append("\n");
		for (MemberBirthdate d : members) {
			try {
				sb.append(d.getName() + ": " + finaldate.format(sdf.parse(d.getDate())) + "\n");
			} catch (ParseException ignored) {
			}
		}

		return sb.toString().trim();
	}

	private void reviewResults(List<MemberBirthdate> members, int monthInt, MemberResults results) {
		for (Member member : results.getResults()) {
			if (member.getBirthday() != null && !member.getBirthday().isEmpty()) {
				SoaLogging.getLogger(this)
						.trace("Member: " + member.getName() + " And Birthday " + member.getBirthday());
				String[] d = member.getBirthday().split("/");
				if (Integer.parseInt(d[0]) == monthInt) {
					MemberBirthdate memb = new MemberBirthdate(member.getName(), member.getBirthday());
					members.add(memb);
				}
			}
		}
	}

	@Override
	public Mono<Void> execute(ModalSubmitInteractionEvent event) {
		return null;
	}

	private static class MemberBirthdate implements Comparable<MemberBirthdate> {
		private final String name;
		private String date;

		public MemberBirthdate(String name, String date) {
			this.name = name;
			this.date = date;
		}

		public String getName() {
			return name;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public Integer getMonthNumb(MemberBirthdate memberBirthdate) {
			String[] d = memberBirthdate.getDate().split("/");
			if (d.length == 3) {
				memberBirthdate.setDate(d[0] + "/" + d[1]);
			}
			return Integer.parseInt(d[1]);
		}

		@Override
		public int compareTo(MemberBirthdate o) {
			return getMonthNumb(this).compareTo(getMonthNumb(o));
		}
	}
}
