package com.soa.rs.discordbot.v3.commands;

import java.util.Optional;

import com.soa.rs.discordbot.v3.jdbi.GuildUserUtility;

import org.junit.Test;
import org.mockito.Mockito;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

public class SetRsnCommandTest {

	private final String thumbsUp = "👍";
	private final String caution = "⚠";

	@Test
	public void testSetRsn() {
		GuildUserUtility utility = Mockito.mock(GuildUserUtility.class);

		SetRsnCommand command = new SetRsnCommand();
		command.setUserUtility(utility);

		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Member member = Mockito.mock(Member.class);

		Mockito.when(member.getGuildId()).thenReturn(Snowflake.of(6789));
		Mockito.when(member.getId()).thenReturn(Snowflake.of(1234));
		Mockito.when(event.getMember()).thenReturn(Optional.of(member));

		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(Optional.of("!setrsn noob"));
		Mockito.when(message.addReaction(ReactionEmoji.unicode(thumbsUp))).thenReturn(Mono.empty());
		Mockito.when(event.getMessage()).thenReturn(message);

		command.execute(event);

		Mockito.verify(utility, Mockito.times(1)).updateKnownNameForUser("noob", 1234, 6789);
		Mockito.verify(message, Mockito.times(1)).addReaction(ReactionEmoji.unicode(thumbsUp));
	}

	@Test
	public void testSetRsnNoMember() {
		GuildUserUtility utility = Mockito.mock(GuildUserUtility.class);

		SetRsnCommand command = new SetRsnCommand();
		command.setUserUtility(utility);

		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Mockito.when(event.getMember()).thenReturn(Optional.empty());

		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(Optional.of("!setrsn noob"));
		Mockito.when(message.addReaction(ReactionEmoji.unicode(caution))).thenReturn(Mono.empty());
		Mockito.when(event.getMessage()).thenReturn(message);

		User user = Mockito.mock(User.class);
		Mockito.when(message.getAuthor()).thenReturn(Optional.of(user));

		MessageChannel channel = Mockito.spy(MessageChannel.class);
		Mockito.when(message.getChannel()).thenReturn(Mono.just(channel));

		command.execute(event);

		//If this got called, it means that we "sent the message", as it happens afterwards
		Mockito.verify(message, Mockito.times(1)).addReaction(ReactionEmoji.unicode(caution));

	}

	@Test
	public void testSetRsnNoMemberNoUser()
	{
		GuildUserUtility utility = Mockito.mock(GuildUserUtility.class);

		SetRsnCommand command = new SetRsnCommand();
		command.setUserUtility(utility);

		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Mockito.when(event.getMember()).thenReturn(Optional.empty());

		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(Optional.of("!setrsn noob"));
		Mockito.when(message.addReaction(ReactionEmoji.unicode(caution))).thenReturn(Mono.empty());
		Mockito.when(event.getMessage()).thenReturn(message);

		Mockito.when(message.getAuthor()).thenReturn(Optional.empty());

		MessageChannel channel = Mockito.spy(MessageChannel.class);
		Mockito.when(message.getChannel()).thenReturn(Mono.just(channel));

		command.execute(event);

		//Verify NEITHER reaction was sent
		Mockito.verify(message, Mockito.times(0)).addReaction(ReactionEmoji.unicode(thumbsUp));
		Mockito.verify(message, Mockito.times(0)).addReaction(ReactionEmoji.unicode(caution));

	}
}
