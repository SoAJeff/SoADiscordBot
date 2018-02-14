package com.soa.rs.discordbot.d4j.testimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IRegion;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.Image;

public class MockDiscordClient implements IDiscordClient {

	private List<IGuild> guilds = new ArrayList<IGuild>();

	@Override
	public EventDispatcher getDispatcher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleLoader getModuleLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IShard> getShards() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getShardCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void login() {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeUsername(String username) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeAvatar(Image avatar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changePlayingText(String playingText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void online(String playingText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void online() {
		// TODO Auto-generated method stub

	}

	@Override
	public void idle(String playingText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void idle() {
		// TODO Auto-generated method stub

	}

	@Override
	public void streaming(String playingText, String streamingUrl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dnd(String playingText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void invisible() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mute(IGuild guild, boolean isSelfMuted) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deafen(IGuild guild, boolean isSelfDeafened) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLoggedIn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IUser getOurUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IChannel> getChannels(boolean includePrivate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IChannel> getChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IChannel getChannelByID(long channelID) {
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
	public List<IGuild> getGuilds() {
		// TODO Auto-generated method stub
		return this.guilds;
	}

	public void addGuild(IGuild guild) {
		this.guilds.add(guild);
	}

	@Override
	public IGuild getGuildByID(long guildID) {
		Iterator<IGuild> iter = this.guilds.iterator();

		while (iter.hasNext()) {
			IGuild guild = iter.next();
			if (guild.getLongID() == guildID) {
				return guild;
			}
		}
		return null;
	}

	@Override
	public List<IUser> getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IUser getUserByID(long userID) {
		for (IGuild guild : this.guilds) {
			for (IUser user : guild.getUsers()) {
				if (user.getLongID() == userID) {
					return user;
				}
			}
		}
		return null;
	}

	@Override
	public IUser fetchUser(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getUsersByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getUsersByName(String name, boolean ignoreCase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRole> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRole getRoleByID(long roleID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IMessage> getMessages(boolean includePrivate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IMessage> getMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage getMessageByID(long messageID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel(IUser user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IInvite getInviteForCode(String code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRegion> getRegions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRegion getRegionByID(String regionID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApplicationDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApplicationIconURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApplicationClientID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApplicationName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IUser getApplicationOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ICategory> getCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICategory getCategoryByID(long categoryID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ICategory> getCategoriesByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
