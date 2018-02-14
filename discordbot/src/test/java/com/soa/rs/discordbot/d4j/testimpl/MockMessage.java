package com.soa.rs.discordbot.d4j.testimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vdurmont.emoji.Emoji;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageTokenizer;

public class MockMessage implements IMessage {

	private IChannel channel;
	private List<IUser> mentions = new ArrayList<IUser>();
	private IGuild guild;
	private String content;
	private IDiscordClient client;
	private IUser author;

	@Override
	public IDiscordClient getClient() {
		// TODO Auto-generated method stub
		return this.client;
	}

	public void setClient(IDiscordClient client) {
		this.client = client;
	}

	@Override
	public IShard getShard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLongID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getContent() {
		// TODO Auto-generated method stub
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public IChannel getChannel() {
		// TODO Auto-generated method stub
		return this.channel;
	}

	public void setChannel(IChannel channel) {
		this.channel = channel;
	}

	@Override
	public IUser getAuthor() {
		// TODO Auto-generated method stub
		return this.author;
	}

	public void setAuthor(IUser user) {
		this.author = user;
	}

	@Override
	public LocalDateTime getTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getMentions() {
		// TODO Auto-generated method stub
		return this.mentions;
	}

	public void addUserMention(IUser user) {
		this.mentions.add(user);
	}

	@Override
	public List<IRole> getRoleMentions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IChannel> getChannelMentions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Attachment> getAttachments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IEmbed> getEmbeds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage reply(String content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage reply(String content, EmbedObject embed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage edit(String content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage edit(String content, EmbedObject embed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage edit(EmbedObject embed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean mentionsEveryone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mentionsHere() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<LocalDateTime> getEditedTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPinned() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IGuild getGuild() {
		// TODO Auto-generated method stub
		return this.guild;
	}

	public void setGuild(IGuild guild) {
		this.guild = guild;
	}

	@Override
	public String getFormattedContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IReaction> getReactions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IReaction getReactionByIEmoji(IEmoji emoji) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IReaction getReactionByEmoji(IEmoji emoji) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IReaction getReactionByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IReaction getReactionByUnicode(Emoji unicode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IReaction getReactionByUnicode(String unicode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IReaction getReactionByEmoji(ReactionEmoji emoji) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addReaction(IReaction reaction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addReaction(IEmoji emoji) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addReaction(Emoji emoji) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addReaction(String emoji) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addReaction(ReactionEmoji emoji) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeReaction(IReaction reaction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeReaction(IUser user, IReaction reaction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeReaction(IUser user, ReactionEmoji emoji) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeReaction(IUser user, IEmoji emoji) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeReaction(IUser user, Emoji emoji) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeReaction(IUser user, String emoji) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAllReactions() {
		// TODO Auto-generated method stub

	}

	@Override
	public MessageTokenizer tokenize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getWebhookLongID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSystemMessage() {
		// TODO Auto-generated method stub
		return false;
	}

}
