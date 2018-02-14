package com.soa.rs.discordbot.d4j.testimpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IExtendedInvite;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IWebhook;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.AttachmentPartEntry;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageHistory;
import sx.blah.discord.util.cache.LongMap;

public class MockChannel implements IChannel {

	private boolean messageSent = false;
	private IDiscordClient client;
	private long ID;
	private IGuild guild;
	private String message;

	public boolean wasMessageSent() {
		if (messageSent) {
			messageSent = false;
			return true;
		}
		return false;
	}

	public String getMessage() {
		return this.message;
	}

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
	public IChannel copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLongID() {
		// TODO Auto-generated method stub
		return this.ID;
	}

	public void setLongID(long ID) {
		this.ID = ID;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistory(int messageCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryFrom(LocalDateTime startDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryFrom(LocalDateTime startDate, int maxMessageCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryFrom(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryFrom(long id, int maxMessageCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryTo(LocalDateTime endDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryTo(LocalDateTime endDate, int maxMessageCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryTo(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryTo(long id, int maxMessageCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryIn(LocalDateTime startDate, LocalDateTime endDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryIn(LocalDateTime startDate, LocalDateTime endDate, int maxMessageCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryIn(long beginID, long endID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getMessageHistoryIn(long beginID, long endID, int maxMessageCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageHistory getFullMessageHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IMessage> bulkDelete() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IMessage> bulkDelete(List<IMessage> messages) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxInternalCacheCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInternalCacheCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IMessage getMessageByID(long messageID) {
		// TODO Auto-generated method stub
		return null;
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
	public boolean isPrivate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNSFW() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTopic() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String mention() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage sendMessage(String content) {
		// TODO Auto-generated method stub
		this.messageSent = true;
		this.message = content;
		return null;
	}

	@Override
	public IMessage sendMessage(EmbedObject embed) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendMessage(String content, boolean tts) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendMessage(String content, EmbedObject embed) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendMessage(String content, EmbedObject embed, boolean tts) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFile(File file) throws FileNotFoundException {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFiles(File... files) throws FileNotFoundException {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFile(String content, File file) throws FileNotFoundException {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFiles(String content, File... files) throws FileNotFoundException {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFile(EmbedObject embed, File file) throws FileNotFoundException {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFiles(EmbedObject embed, File... files) throws FileNotFoundException {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFile(String content, InputStream file, String fileName) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFiles(String content, AttachmentPartEntry... entries) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFile(EmbedObject embed, InputStream file, String fileName) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFiles(EmbedObject embed, AttachmentPartEntry... entries) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFile(String content, boolean tts, InputStream file, String fileName) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFiles(String content, boolean tts, AttachmentPartEntry... entries) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFile(String content, boolean tts, InputStream file, String fileName, EmbedObject embed) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFiles(String content, boolean tts, EmbedObject embed, AttachmentPartEntry... entries) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IMessage sendFile(MessageBuilder builder, InputStream file, String fileName) {
		this.messageSent = true;
		return null;
	}

	@Override
	public IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean unique) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void toggleTypingStatus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTypingStatus(boolean typing) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getTypingStatus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void edit(String name, int position, String topic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changePosition(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeTopic(String topic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeNSFW(boolean isNSFW) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub

	}

	@Override
	public LongMap<sx.blah.discord.handle.obj.PermissionOverride> getUserOverridesLong() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongMap<sx.blah.discord.handle.obj.PermissionOverride> getRoleOverridesLong() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IUser user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removePermissionsOverride(IUser user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePermissionsOverride(IRole role) {
		// TODO Auto-generated method stub

	}

	@Override
	public void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) {
		// TODO Auto-generated method stub

	}

	@Override
	public void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IExtendedInvite> getExtendedInvites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getUsersHere() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IMessage> getPinnedMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pin(IMessage message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unpin(IMessage message) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IWebhook> getWebhooks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWebhook getWebhookByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IWebhook> getWebhooksByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWebhook createWebhook(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWebhook createWebhook(String name, Image avatar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWebhook createWebhook(String name, String avatar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LongMap<sx.blah.discord.handle.obj.PermissionOverride> getUserOverrides() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongMap<sx.blah.discord.handle.obj.PermissionOverride> getRoleOverrides() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void changeCategory(ICategory category) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ICategory getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

}
