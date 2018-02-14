package com.soa.rs.discordbot.d4j.testimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audit.ActionType;
import sx.blah.discord.handle.audit.AuditLog;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IExtendedInvite;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRegion;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IWebhook;
import sx.blah.discord.handle.obj.VerificationLevel;
import sx.blah.discord.util.Ban;
import sx.blah.discord.util.Image;

public class MockGuild implements IGuild {

	private List<IUser> users = new ArrayList<IUser>();
	private List<IRole> roles = new ArrayList<IRole>();
	private long guildID = 0;
	private String guildName = null;
	private IDiscordClient client;

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
	public IGuild copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLongID() {
		// TODO Auto-generated method stub
		return this.guildID;
	}

	public void setLongID(long id) {
		this.guildID = id;
	}

	@Override
	public long getOwnerLongID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IUser getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIconURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IChannel> getChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IChannel getChannelByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getUsers() {
		// TODO Auto-generated method stub
		return users;
	}

	public void addUser(IUser user) {
		users.add(user);
	}

	public void removeUser(IUser user) {
		users.remove(user);
	}

	@Override
	public IUser getUserByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IChannel> getChannelsByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IVoiceChannel> getVoiceChannelsByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getUsersByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getUsersByName(String name, boolean includeNicknames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getUsersByRole(IRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.guildName;
	}

	public void setName(String name) {
		this.guildName = name;
	}

	@Override
	public List<IRole> getRoles() {
		// TODO Auto-generated method stub
		return this.roles;
	}

	public void addRole(IRole role) {
		this.roles.add(role);
	}

	@Override
	public List<IRole> getRolesForUser(IUser user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRole getRoleByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRole> getRolesByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVoiceChannel getVoiceChannelByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVoiceChannel getConnectedVoiceChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVoiceChannel getAFKChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAFKTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IRole createRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getBannedUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Ban> getBans() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void banUser(IUser user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void banUser(IUser user, int deleteMessagesForDays) {
		// TODO Auto-generated method stub

	}

	@Override
	public void banUser(IUser user, String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void banUser(IUser user, String reason, int deleteMessagesForDays) {
		// TODO Auto-generated method stub

	}

	@Override
	public void banUser(long userID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void banUser(long userID, int deleteMessagesForDays) {
		// TODO Auto-generated method stub

	}

	@Override
	public void banUser(long userID, String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void banUser(long userID, String reason, int deleteMessagesForDays) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pardonUser(long userID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void kickUser(IUser user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void kickUser(IUser user, String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void editUserRoles(IUser user, IRole[] roles) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDeafenUser(IUser user, boolean deafen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMuteUser(IUser user, boolean mute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUserNickname(IUser user, String nick) {
		// TODO Auto-generated method stub

	}

	@Override
	public void edit(String name, IRegion region, VerificationLevel level, Image icon, IVoiceChannel afkChannel,
			int afkTimeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeRegion(IRegion region) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeVerificationLevel(VerificationLevel verification) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeIcon(Image icon) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeAFKChannel(IVoiceChannel channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeAFKTimeout(int timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public void leave() {
		// TODO Auto-generated method stub

	}

	@Override
	public IChannel createChannel(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVoiceChannel createVoiceChannel(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRegion getRegion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VerificationLevel getVerificationLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRole getEveryoneRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IChannel getGeneralChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IChannel getDefaultChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IExtendedInvite> getExtendedInvites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reorderRoles(IRole... rolesInOrder) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getUsersToBePruned(int days) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int pruneUsers(int days) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IAudioManager getAudioManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocalDateTime getJoinTimeForUser(IUser user) {
		LocalDateTime time = LocalDateTime.of(1970, 1, 1, 0, 0); // the epoch
		return time;
	}

	@Override
	public IMessage getMessageByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IEmoji> getEmojis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEmoji getEmojiByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEmoji getEmojiByName(String name) {
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
	public List<IWebhook> getWebhooks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalMemberCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AuditLog getAuditLog() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuditLog getAuditLog(ActionType actionType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuditLog getAuditLog(IUser user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuditLog getAuditLog(IUser user, ActionType actionType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEmoji createEmoji(String name, Image image, IRole[] roles) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICategory createCategory(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ICategory> getCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICategory getCategoryByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ICategory> getCategoriesByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IChannel getSystemChannel() {
		// TODO Auto-generated method stub
		return null;
	}

}
