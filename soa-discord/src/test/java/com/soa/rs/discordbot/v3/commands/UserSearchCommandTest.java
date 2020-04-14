package com.soa.rs.discordbot.v3.commands;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

public class UserSearchCommandTest {

	@Test
	public void testJustSearchTerm()
	{
		UserSearchCommand command = new UserSearchCommand();
		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(".usersearch Apple SoA");
		Mockito.when(event.getMessage()).thenReturn(message);

		UserSearchCommand.Search search = command.determineSearch(event);

		Assert.assertEquals("Apple SoA", search.getSearchTerm());
		Assert.assertNull(search.getServerName());
	}

	@Test
	public void testSearchTermAndServer()
	{
		UserSearchCommand command = new UserSearchCommand();
		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(".usersearch Apple SoA -server Spirits of Arianwyn");
		Mockito.when(event.getMessage()).thenReturn(message);

		UserSearchCommand.Search search = command.determineSearch(event);

		Assert.assertEquals("Apple SoA", search.getSearchTerm());
		Assert.assertEquals("Spirits of Arianwyn", search.getServerName());
	}

	@Test
	public void testEmptySearch()
	{
		UserSearchCommand command = new UserSearchCommand();
		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(".usersearch");
		Mockito.when(event.getMessage()).thenReturn(message);

		UserSearchCommand.Search search = command.determineSearch(event);

		Assert.assertEquals("", search.getSearchTerm());
	}

	@Test
	public void testEmptyMessage()
	{
		UserSearchCommand command = new UserSearchCommand();
		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn("");
		Mockito.when(event.getMessage()).thenReturn(message);

		UserSearchCommand.Search search = command.determineSearch(event);

		Assert.assertEquals("", search.getSearchTerm());
	}

	@Test
	public void testSearchTermAndId()
	{
		UserSearchCommand command = new UserSearchCommand();
		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(".usersearch Apple SoA -serverid 1234");
		Mockito.when(event.getMessage()).thenReturn(message);

		UserSearchCommand.Search search = command.determineSearch(event);

		Assert.assertEquals("Apple SoA", search.getSearchTerm());
		Assert.assertEquals(1234, search.getServerId());
	}

	@Test
	public void testSearchTermAndJunkId()
	{
		UserSearchCommand command = new UserSearchCommand();
		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(".usersearch Apple SoA -serverid JUNK");
		Mockito.when(event.getMessage()).thenReturn(message);

		UserSearchCommand.Search search = command.determineSearch(event);

		Assert.assertEquals("Apple SoA", search.getSearchTerm());
		Assert.assertNotEquals(1234, search.getServerId());
	}

	@Test
	public void testSearchTermAndIdThenServerName()
	{
		UserSearchCommand command = new UserSearchCommand();
		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(".usersearch Apple SoA -serverid 1234 -server This should never be seen");
		Mockito.when(event.getMessage()).thenReturn(message);

		UserSearchCommand.Search search = command.determineSearch(event);

		Assert.assertEquals("Apple SoA", search.getSearchTerm());
		Assert.assertEquals(1234, search.getServerId());
		Assert.assertNull(search.getServerName());
	}

	@Test
	public void testSearchTermAndServerNameThenId()
	{
		UserSearchCommand command = new UserSearchCommand();
		MessageCreateEvent event = Mockito.mock(MessageCreateEvent.class);
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getContent()).thenReturn(".usersearch Apple SoA -server Spirits of Arianwyn -serverid 1234");
		Mockito.when(event.getMessage()).thenReturn(message);

		UserSearchCommand.Search search = command.determineSearch(event);

		Assert.assertEquals("Apple SoA", search.getSearchTerm());
		Assert.assertEquals("Spirits of Arianwyn", search.getServerName());
		Assert.assertNotEquals(1234, search.getServerId());
	}
}
