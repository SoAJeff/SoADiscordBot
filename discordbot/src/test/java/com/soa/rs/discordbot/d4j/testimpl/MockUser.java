package com.soa.rs.discordbot.d4j.testimpl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IVoiceState;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.cache.LongMap;

public class MockUser implements IUser {

	private long id;
	private String name = null;
	private String displayName = null;
	private String discriminator = null;
	private String nickname = null;
	private IPresence presence;
	private List<IRole> roles = new ArrayList<IRole>();

	@Override
	public IDiscordClient getClient() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IShard getShard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IUser copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLongID() {
		// TODO Auto-generated method stub
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getAvatar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAvatarURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPresence getPresence() {
		// TODO Auto-generated method stub
		return this.presence;
	}

	public void setPresence(IPresence presence) {
		this.presence = presence;
	}

	@Override
	public String getDisplayName(IGuild guild) {
		if (this.displayName == null)
			return this.name;
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String mention() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String mention(boolean mentionWithNickname) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDiscriminator() {
		if (this.discriminator == null)
			return new String("1234");
		return this.discriminator;
	}

	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	@Override
	public List<IRole> getRolesForGuild(IGuild guild) {
		return this.roles;
	}

	@Override
	public Color getColorForGuild(IGuild guild) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumSet<Permissions> getPermissionsForGuild(IGuild guild) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNicknameForGuild(IGuild guild) {
		if (this.nickname == null)
			return this.name;
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Override
	public IVoiceState getVoiceStateForGuild(IGuild guild) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongMap<IVoiceState> getVoiceStatesLong() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveToVoiceChannel(IVoiceChannel channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isBot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addRole(IRole role) {
		this.roles.add(role);

	}

	@Override
	public void removeRole(IRole role) {
		Iterator<IRole> iter = this.roles.iterator();
		while (iter.hasNext()) {
			IRole checkRole = iter.next();
			if (checkRole.getName().equals(role.getName()))
				iter.remove();
		}

	}

	@Override
	public LongMap<IVoiceState> getVoiceStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasRole(IRole role) {
		// TODO Auto-generated method stub
		return false;
	}

}
