import logging
import discord
from discord.ext import commands, tasks
from discord import app_commands
import datetime
from datetime import datetime
import asyncio
from pydantic import BaseModel, AfterValidator
from typing import Annotated, Union, Optional
import threading
from enum import Enum
from abc import ABC, abstractmethod
from .utils.paginator import ButtonPaginator

from bot import SoAClient

logger = logging.getLogger(__name__)

def remove_tzinfo(date: datetime):
    return date.replace(tzinfo=None)

class AuditingChannelType(Enum):
    USER_JOIN = "user_join_channel_id"
    USER_LEAVE = "user_left_channel_id"

class UserEntry(BaseModel, validate_assignment=True):
    user_id: int
    guild_id: int
    user_name: str
    known_name: str = ""
    display_name: str
    last_seen: Annotated[datetime, AfterValidator(remove_tzinfo)] = datetime(1970, 1, 1).replace(tzinfo=None)
    joined_server: Annotated[datetime, AfterValidator(remove_tzinfo)] = datetime(1970, 1, 1).replace(tzinfo=None)
    left_server: Annotated[datetime, AfterValidator(remove_tzinfo)] = datetime(1970, 1, 1).replace(tzinfo=None)
    last_active: Annotated[datetime, AfterValidator(remove_tzinfo)] = datetime(1970, 1, 1).replace(tzinfo=None)

class RecentAction(BaseModel):
    id: int = -1
    date: datetime = discord.utils.utcnow().replace(tzinfo=None)
    user_name: Optional[str] = "" # This is not in the table, but we bring this in normally with a join.
    user_id: int
    guild_id: int
    action: str
    original_value: str
    new_value: str

class GuildUser(BaseModel, frozen=True):
    user_id: int
    guild_id: int

class RecentCache(ABC):
    def __init__(self, bot: SoAClient):
        self.bot: SoAClient = bot
        self.cache: dict[GuildUser, datetime] = dict()
        self._lock = threading.RLock()

    def insert_into_cache(self, guild_user: GuildUser):
        with self._lock:
            self.cache[guild_user] = discord.utils.utcnow().replace(tzinfo=None)

    async def flush_cache(self):
        if len(self.cache.keys()) > 0:
            with self._lock:
                try:
                    logger.info("Submitting info for %d users in the %s", len(self.cache.keys()), self.get_cache_name())
                    await self.update_database()
                    self.cache.clear()
                except Exception as e:
                    logger.error("Error found when submitting users from %s: %s", self.get_cache_name(), e, exc_info=True)

    async def update_database(self):
        async with self.bot.pool.acquire() as connection:
            async with connection.transaction():
                for user in self.cache.keys():
                    await connection.execute(self.get_query(), self.cache[user], user.user_id, user.guild_id)

    @abstractmethod
    def get_query(self):
        pass

    @abstractmethod
    def get_cache_name(self):
        pass

class LastSeenCache(RecentCache):
    def __init__(self, bot: SoAClient):
        super().__init__(bot)
        
    def get_query(self):
        return "UPDATE users SET last_seen = $1 WHERE user_id = $2 AND guild_id = $3"

    def get_cache_name(self):
        return "Last Seen Cache"
    
class LastActiveCache(RecentCache):
    def __init__(self, bot: SoAClient):
        super().__init__(bot)

    def get_query(self):
        return "UPDATE users SET last_active = $1 WHERE user_id = $2 AND guild_id = $3"

    def get_cache_name(self):
        return "Last Active Cache"

@app_commands.guild_only()
@app_commands.default_permissions(moderate_members=True)
class UserTrack(commands.GroupCog, name="users"):
    def __init__(self, bot: SoAClient):
        self.bot: SoAClient = bot
        self.last_seen_cache = LastSeenCache(bot)
        self.last_active_cache = LastActiveCache(bot)

    async def cog_load(self):
        """Load the initial status and start the tasks."""
        self.flush_caches_task.start()
        asyncio.create_task(self.profile_guild_users())

    async def cog_unload(self):
        self.flush_caches_task.cancel()
        # Flush one more time
        await self.flush_caches()
        del self.last_active_cache
        del self.last_seen_cache

    @tasks.loop(minutes=1)
    async def flush_caches_task(self):
        await self.flush_caches()

    async def flush_caches(self):
        await self.last_seen_cache.flush_cache()
        await self.last_active_cache.flush_cache()

    async def profile_guild_users(self):
        await self.bot.wait_until_ready()
        for guild in self.bot.guilds:
            await self.profile_guild(guild)

    async def profile_guild(self, guild: discord.Guild):
        logger.info("Profiling guild %d, containing %d members", guild.id, len(guild.members))
        db_member_list = await self.get_db_members_for_guild(guild.id)
        member_list: list[UserEntry] = list()
        for member in guild.members:
            # Filter out bot users
            if member.bot is True:
                continue
            logger.info("Reviewing user %s (%d)", member, member.id)
            existing_member = False
            if member.id in db_member_list:
                logger.info("User found in database, marking as existing user")
                db_member_list.remove(member.id)
                existing_member = True
            member_list.append(await self.profile_member(guild, member, existing_member))
        
        # Now mark any left in db_member_list as left
        for id in db_member_list:
            logger.info("Marking %d as having left server", id)
            left_user = await self.get_db_member_for_guild(id, guild.id)
            left_user.left_server = discord.utils.utcnow()
            await self.add_recent_action(RecentAction(user_id=left_user.user_id,
                                                      guild_id=left_user.guild_id,
                                                      action="Left the server",
                                                      original_value="",
                                                      new_value=""))
            member_list.append(left_user)
        
        # Now, submit the list
        await self.submit_profiled_users_for_guild(member_list)

    async def profile_member(self, guild: discord.Guild, member: discord.Member, existing_member: bool):
        new_guild_user = self.create_new_member(member)
        
        if existing_member:
            existing_user = await self.get_db_member_for_guild(member.id, guild.id)
            new_guild_user.known_name = existing_user.known_name if existing_user.known_name != None else None

            # Check Username equality
            await self.check_username_equality(existing_user, new_guild_user)
            # Check Display name equality
            await self.check_display_name_equality(existing_user, new_guild_user)
            # Check if joined server matches
            await self.check_joined_date_equality(existing_user, new_guild_user)
            # Check if display name in nicknames
            await self.check_nicknames_for_user(new_guild_user)
            # Set last seen, last active, left server as epoch
            new_guild_user.last_seen = existing_user.last_seen
            new_guild_user.last_active = existing_user.last_active
            new_guild_user.left_server = datetime(1970, 1, 1)
        else:
            new_guild_user.last_active = member.joined_at
            new_guild_user.last_seen = member.joined_at
            new_guild_user.left_server = datetime(1970, 1, 1)
            await self.add_recent_action(RecentAction(user_id=new_guild_user.user_id,
                                                      guild_id=new_guild_user.guild_id,
                                                      action="Joined the server",
                                                      original_value="",
                                                      new_value=""))
            await self.add_nickname_for_user(new_guild_user)
        
        if member.status != discord.Status.offline:
            new_guild_user.last_seen = discord.utils.utcnow()

        return new_guild_user

    def create_new_member(self, member: discord.Member):
        return UserEntry(user_id=member.id,
                         guild_id=member.guild.id,
                         user_name=member.name,
                         display_name=member.display_name,
                         joined_server=member.joined_at)
    
    async def check_username_equality(self, existing_user: UserEntry, new_guild_user: UserEntry):
        if existing_user.user_name != new_guild_user.user_name:
            logger.info("User %s (%d) has updated their user handle", new_guild_user.user_name, new_guild_user.user_id)
            await self.add_recent_action(RecentAction(user_id=existing_user.user_id,
                                                      guild_id=existing_user.guild_id,
                                                      action="Changed their user handle",
                                                      original_value=existing_user.user_name,
                                                      new_value=new_guild_user.user_name))
            
    async def check_display_name_equality(self, existing_user: UserEntry, new_guild_user: UserEntry):
        if existing_user.display_name != new_guild_user.display_name:
            logger.info("User %s (%d) has updated their display name", new_guild_user.display_name, new_guild_user.user_id)
            await self.add_recent_action(RecentAction(user_id=existing_user.user_id,
                                                      guild_id=existing_user.guild_id,
                                                      action="Changed their display name",
                                                      original_value=existing_user.display_name,
                                                      new_value=new_guild_user.display_name))
            
    async def check_joined_date_equality(self, existing_user: UserEntry, new_guild_user: UserEntry):
        if existing_user.joined_server != new_guild_user.joined_server:
            # If someone joins while the bot is running, the precision of the datetime is different than what will be seen on a restart
            # It is likely the case that Discord.py calls discord.utils.utcnow() when a member joins and stores that in the cache
            # This may have different precision than what is received from Discord on a bot restart.
            # It should be within a second, so check if the join time is within a 2 second window.  If it is, then we should be fine.
            duration = existing_user.joined_server - new_guild_user.joined_server
            if duration.total_seconds() > 2:
                logger.info("Joined server time did not match, assuming rejoined.  Duration between joined server dates: %.3f", duration.total_seconds())
                await self.add_recent_action(RecentAction(user_id=existing_user.user_id,
                                                        guild_id=existing_user.guild_id,
                                                        action="Joined the server",
                                                        original_value="",
                                                        new_value=""))
                
    async def check_nicknames_for_user(self, user: UserEntry):
        query = "SELECT nickname FROM nicknames WHERE user_id = $1 and guild_id = $2"
        rows = await self.bot.pool.fetch(query, user.user_id, user.guild_id)
        for row in rows:
            if user.display_name == row['nickname']:
                return
        await self.add_nickname_for_user(user)
    
    async def add_nickname_for_user(self, user: UserEntry):
        insert_query = "INSERT INTO nicknames (user_id, guild_id, nickname) VALUES ($1, $2, $3)"
        await self.bot.pool.execute(insert_query, user.user_id, user.guild_id, user.display_name)
    
    async def get_nicknames_for_user(self, user: UserEntry):
        query = "SELECT nickname FROM nicknames WHERE user_id = $1 and guild_id = $2"
        rows = await self.bot.pool.fetch(query, user.user_id, user.guild_id)
        return [row['nickname'] for row in rows]

    async def add_recent_action(self, recent_action: RecentAction):
        query = "INSERT INTO recent_actions (user_id, guild_id, action, original_value, new_value) VALUES ($1, $2, $3, $4, $5)"
        logger.info("Adding new recent action: %s", recent_action)
        await self.bot.pool.execute(query, recent_action.user_id, recent_action.guild_id, recent_action.action, recent_action.original_value, recent_action.new_value)

    async def get_recent_actions_for_guild(self, guild_id: int, num_entries: int):
        query = """SELECT r.*, u.user_name FROM recent_actions r 
                   LEFT JOIN users u ON u.user_id = r.user_id AND u.guild_id = r.guild_id
                   WHERE r.guild_id = $1
                   ORDER BY r.id DESC LIMIT $2"""
        rows = await self.bot.pool.fetch(query, guild_id, num_entries)
        if len(rows) > 0:
            return [RecentAction(**row) for row in rows]
        return None

    async def get_db_members_for_guild(self, guild_id: int):
        query = "SELECT user_id FROM users WHERE guild_id = $1 and left_server = $2"
        rows = await self.bot.pool.fetch(query, guild_id, datetime(1970, 1, 1))
        db_member_list: list[int] = list()
        for row in rows:
            db_member_list.append(row['user_id'])

        return db_member_list

    async def get_db_member_for_guild(self, user_id: int, guild_id: int):
        query = "SELECT * FROM users WHERE guild_id = $1 AND user_id = $2"
        row = await self.bot.pool.fetchrow(query, guild_id, user_id)
        if row is not None:
            return UserEntry(**row)
        return None
    
    async def get_db_member_for_all_guilds(self, user_id: int):
        query = "SELECT * FROM users WHERE user_id = $1"
        rows = await self.bot.pool.fetch(query, user_id)
        if len(rows) > 0:
            return [UserEntry(**row) for row in rows]
        return None
    
    async def get_db_members_for_guild_by_search_term(self, search_term: str, guild_id: int):
        query = """SELECT DISTINCT u.* FROM users u 
                   LEFT JOIN nicknames n ON u.user_id = n.user_id AND u.guild_id = n.guild_id
                   WHERE (
                   LOWER(u.user_name) LIKE $1
                   OR LOWER(u.known_name) LIKE $1
                   OR LOWER(u.display_name) LIKE $1
                   OR LOWER(n.nickname) LIKE $1)
                   AND u.guild_id = $2"""
        rows = await self.bot.pool.fetch(query, f"%{search_term.lower()}%", guild_id)
        return [UserEntry(**row) for row in rows]

    async def get_db_members_for_guild_by_rsn(self, search_term: str, guild_id: int):
        query = """SELECT * FROM users WHERE (
                   LOWER(user_name) LIKE $1
                   OR LOWER(known_name) LIKE $1
                   OR LOWER(display_name) LIKE $1)
                   AND guild_id = $2"""
        rows = await self.bot.pool.fetch(query, f"%{search_term.lower()}%", guild_id)
        if len(rows) > 0:
            return [UserEntry(**row) for row in rows]
        return None
    
    async def submit_profiled_users_for_guild(self, member_list: list[UserEntry]):
        query = """INSERT INTO users (user_id, guild_id, user_name, known_name, display_name, last_seen, joined_server, left_server, last_active)
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                    ON CONFLICT (user_id, guild_id)
                    DO UPDATE SET user_name = $3, known_name = $4, display_name = $5, last_seen = $6, joined_server = $7, left_server = $8, last_active = $9"""
        logger.info("Submitting %d users for guild %d", len(member_list), member_list[0].guild_id)
        async with self.bot.pool.acquire() as connection:
            async with connection.transaction():
                for member in member_list:
                    await connection.execute(query, 
                                             member.user_id,
                                             member.guild_id,
                                             member.user_name,
                                             member.known_name,
                                             member.display_name,
                                             member.last_seen,
                                             member.joined_server,
                                             member.left_server,
                                             member.last_active)
                    
    async def get_auditing_channel(self, guild_id: int, channel_type: AuditingChannelType):
        query = f"SELECT {channel_type.value} FROM auditing_channels WHERE guild_id = $1"
        try:
            val = await self.bot.pool.fetchval(query, guild_id)
            return val
        except Exception as e:
            logger.warning("No auditing channel found.")
            return None
        
    def create_user_join_embed(self, member: discord.Member):
        embed = discord.Embed(title="User Join Notification",
                              timestamp=discord.utils.utcnow(),
                              color=self.bot.green_color,
                              description=f"{member.mention} (`{member}`) has joined the server!")
        embed.set_thumbnail(url=member.display_avatar.url)
        embed.add_field(name="Creation Date", value=f"{discord.utils.format_dt(member.created_at, style='f')}\n"
                                                    f"{discord.utils.format_dt(member.created_at, style='R')}", inline=False)
        embed.set_footer(text=f"There are {member.guild.member_count} members.")
        return embed

    def create_user_leave_embed(self, member: discord.Member):
        embed = discord.Embed(title="User Leave Notification", 
                              timestamp=discord.utils.utcnow(),
                              color=self.bot.green_color,
                              description=f"{member.mention} (`{member}`) has left the server!")
        embed.set_thumbnail(url=member.display_avatar.url)
        embed.add_field(name="Roles", value=', '.join(map(str, member.roles)), inline=False)
        embed.set_footer(text=f"There are {member.guild.member_count} members.")
        return embed

    def create_user_ban_embed(self, guild: discord.Guild, user: Union[discord.Member, discord.User]):
        embed = discord.Embed(title="User Ban Notification", 
                              timestamp=discord.utils.utcnow(),
                              color=self.bot.green_color,
                              description=f"{user.mention} (`{user}`) was banned from the server!")
        embed.set_thumbnail(url=user.display_avatar.url)
        embed.set_footer(text=f"There are {guild.member_count} members.")
        return embed
                    
    @commands.Cog.listener()
    async def on_message(self, message: discord.Message):
        if message.guild is not None and message.author.bot is False:
            logger.debug("Updating last seen/last active for user %d in guild %d due to user sending a message", message.author.id, message.guild.id)
            self.last_active_cache.insert_into_cache(GuildUser(user_id=message.author.id, guild_id=message.guild.id))
            self.last_seen_cache.insert_into_cache(GuildUser(user_id=message.author.id, guild_id=message.guild.id))

    @commands.Cog.listener()
    async def on_message_edit(self, before: discord.Message, after: discord.Message):
        if after.guild is not None and after.author.bot is False:
            logger.debug("Updating last seen/last active for user %d in guild %d due to user editing a message", after.author.id, after.guild.id)
            self.last_active_cache.insert_into_cache(GuildUser(user_id=after.author.id, guild_id=after.guild.id))
            self.last_seen_cache.insert_into_cache(GuildUser(user_id=after.author.id, guild_id=after.guild.id))

    @commands.Cog.listener()
    async def on_voice_state_update(self, member: discord.Member, before, after):
        if member.bot is False:
            logger.debug("Updating last seen/last active for user %d in guild %d due to user's voice state updating", member.id, member.guild.id)
            self.last_active_cache.insert_into_cache(GuildUser(user_id=member.id, guild_id=member.guild.id))
            self.last_seen_cache.insert_into_cache(GuildUser(user_id=member.id, guild_id=member.guild.id))

    @commands.Cog.listener()
    async def on_reaction_add(self, reaction, user: Union[discord.Member, discord.User]):
        if user.guild is not None and user.bot is False:
            logger.debug("Updating last seen/last active for user %d in guild %d due to user adding a reaction", user.id, user.guild.id)
            self.last_active_cache.insert_into_cache(GuildUser(user_id=user.id, guild_id=user.guild.id))
            self.last_seen_cache.insert_into_cache(GuildUser(user_id=user.id, guild_id=user.guild.id))

    @commands.Cog.listener()
    async def on_presence_update(self, before: discord.Member, after: discord.Member):
        if after.guild is not None and after.bot is False and before.status != after.status:
            logger.debug("Updating last seen for user %d in guild %d due to user's presence updating", after.id, after.guild.id)
            self.last_seen_cache.insert_into_cache(GuildUser(user_id=after.id, guild_id=after.guild.id))
    
    @commands.Cog.listener()
    async def on_interaction(self, interaction: discord.Interaction):
        logger.debug("Updating last seen/last active for user %d in guild %d due to user sending an interaction", interaction.user.id, interaction.guild.id)
        self.last_active_cache.insert_into_cache(GuildUser(user_id=interaction.user.id, guild_id=interaction.guild.id))
        self.last_seen_cache.insert_into_cache(GuildUser(user_id=interaction.user.id, guild_id=interaction.guild.id))

    @commands.Cog.listener()
    async def on_typing(self, channel: discord.abc.Messageable, user: Union[discord.User, discord.Member], when: datetime):
        if isinstance(channel, discord.TextChannel):
            if user.guild is not None and user.bot is False:
                logger.debug("Updating last seen/last active for user %d in guild %d due to user typing in a channel", user.id, user.guild.id)
                self.last_active_cache.insert_into_cache(GuildUser(user_id=user.id, guild_id=user.guild.id))
                self.last_seen_cache.insert_into_cache(GuildUser(user_id=user.id, guild_id=user.guild.id))
    
    @commands.Cog.listener()
    async def on_member_join(self, member: discord.Member):
        if member.bot is False:
            logger.info("Profiling newly joined user %d in guild %d", member.id, member.guild.id)
            entry: UserEntry = await self.profile_member(member.guild, member, False)
            await self.submit_profiled_users_for_guild([entry])
        channel_id = await self.get_auditing_channel(member.guild.id, AuditingChannelType.USER_JOIN)
        if channel_id:
            await member.guild.get_channel(channel_id).send(embed=self.create_user_join_embed(member)) 

    @commands.Cog.listener()
    async def on_member_remove(self, member: discord.Member):
        if member.bot is False:
            logger.info("Marking user %d as leaving guild %d", member.id, member.guild.id)
            await self.add_recent_action(RecentAction(user_id=member.id, guild_id=member.guild.id,
                                                    action="Left the server",
                                                    original_value="",
                                                    new_value=""))
            
            entry: UserEntry = await self.get_db_member_for_guild(member.id, member.guild.id)
            entry.left_server = discord.utils.utcnow()
            await self.submit_profiled_users_for_guild([entry])
        channel_id = await self.get_auditing_channel(member.guild.id, AuditingChannelType.USER_LEAVE)
        if channel_id:
            await member.guild.get_channel(channel_id).send(embed=self.create_user_leave_embed(member)) 

    @commands.Cog.listener()
    async def on_member_update(self, before: discord.Member, after: discord.Member):
        # The only thing we actually care about here is display name
        if before.display_name != after.display_name and after.bot is False:
            logger.debug("Updating user %d for guild %d as their display name has been updated", after.id, after.guild.id)
            await self.add_recent_action(RecentAction(user_id=after.id,
                                                      guild_id=after.guild.id,
                                                      action="Changed their display name",
                                                      original_value=before.display_name,
                                                      new_value=after.display_name))
            entry: UserEntry = await self.get_db_member_for_guild(after.id, after.guild.id)
            entry.display_name = after.display_name
            await self.submit_profiled_users_for_guild([entry])
            await self.check_nicknames_for_user(entry)

    @commands.Cog.listener()
    async def on_member_ban(self, guild: discord.Guild, user: Union[discord.User, discord.Member]):
        if user.bot is False:
            logger.debug("Logging user ban for user %d in guild %d", user.id, guild.id)
            await self.add_recent_action(RecentAction(user_id=user.id, guild_id=guild.id,
                                         action="Was banned from the server",
                                         original_value="",
                                         new_value=""))
        channel_id = await self.get_auditing_channel(guild.id, AuditingChannelType.USER_LEAVE)
        if channel_id:
            logger.info("Channel id is %d", channel_id)
            await guild.get_channel(channel_id).send(embed=self.create_user_ban_embed(guild, user))

    @commands.Cog.listener()
    async def on_user_update(self, before: discord.User, after: discord.User):
        if after.bot is False and before.name != after.name:
            logger.info("Logging user %d updated their user handle", after.id)
            entries = await self.get_db_member_for_all_guilds(after.id)
            for entry in entries:
                await self.add_recent_action(RecentAction(user_id=after.id,
                                                        guild_id=entry.guild_id,
                                                        action="Changed their user handle",
                                                        original_value=before.name,
                                                        new_value=after.name))
                entry.user_name = after.name
                entry.last_seen = discord.utils.utcnow()
            
            await self.submit_profiled_users_for_guild(entries)

    @commands.Cog.listener()
    async def on_guild_join(self, guild: discord.Guild):
        await self.profile_guild(guild)

    async def create_profile_embed(self, entry: UserEntry):
        embed = discord.Embed(title=entry.display_name, description=f"{entry.user_name} in server: {self.bot.get_guild(entry.guild_id).name}", 
                              timestamp=discord.utils.utcnow(), color=self.bot.green_color)
        user = await self.get_discord_user(entry)
        if user is not None:
            embed.set_author(name=user.name, icon_url=user.display_avatar.url)
        if len(entry.known_name) > 0:
            embed.add_field(name="Known as", value=entry.known_name, inline=False)
        nicknames = await self.get_nicknames_for_user(entry)
        embed.add_field(name="All Display Names", value=self.create_nickname_string(nicknames), inline=False)
        embed.add_field(name="Joined server date", value=discord.utils.format_dt(entry.joined_server), inline=True)
        if entry.left_server > datetime(1970, 1, 1).replace(tzinfo=None):
            embed.add_field(name="Left server date", value=discord.utils.format_dt(entry.left_server), inline=True)
        else:
            embed.add_field(name="Last seen date", value=discord.utils.format_dt(entry.last_seen), inline=True)
            embed.add_field(name="Last active date", value=discord.utils.format_dt(entry.last_active), inline=False)
        embed.set_footer(text=f"User ID: {entry.user_id} • Guild ID: {entry.guild_id}")

        return embed
    
    async def get_discord_user(self, entry: UserEntry):
        try:
            user = self.bot.get_guild(entry.guild_id).get_member(entry.user_id) or await self.bot.get_guild(entry.guild_id).get_member(entry.user_id)
            return user
        except Exception:
            logger.warning("A search for a guild member profile failed, checking for user instead")

        try:
            user = await self.bot.fetch_user(entry.user_id)
            return user
        except Exception:
            logger.error("This user does not seem to exist on Discord anymore.")
            return None

    def create_nickname_string(self, nicknames: list[str]):
        nickname_str: str = nicknames[0]
        name_count = 1
        for name in nicknames[1:]:
            if len(nickname_str + name) > 975:
                nickname_str += f" and {name_count} additional names."
                return nickname_str
            nickname_str += f", {name}"
            name_count +=1
        return nickname_str
    
    @app_commands.command(name="set_join_audit_channel", description="Log user joins to this channel")
    @app_commands.describe(channel="Channel to log user joins to")
    async def set_join_audit_channel(self, interaction: discord.Interaction, channel: discord.TextChannel):
        await interaction.response.defer(ephemeral=True)
        if interaction.user.guild_permissions.administrator:
            query = """INSERT INTO auditing_channels (guild_id, user_join_channel_id) VALUES ($1, $2)
                ON CONFLICT (guild_id) DO UPDATE SET user_join_channel_id = $2"""
            await self.bot.pool.execute(query, interaction.guild_id, channel.id)
            await interaction.followup.send(f"Join log channel updated to {channel.mention}")
        else:
            interaction.followup.send("You do not have permissions to set this value.")

    @app_commands.command(name="set_leave_audit_channel", description="Log user leaves to this channel")
    @app_commands.describe(channel="Channel to log user leaves to")
    async def set_leave_audit_channel(self, interaction: discord.Interaction, channel: discord.TextChannel):
        await interaction.response.defer(ephemeral=True)
        if interaction.user.guild_permissions.administrator:
            query = """INSERT INTO auditing_channels (guild_id, user_left_channel_id) VALUES ($1, $2)
                ON CONFLICT (guild_id) DO UPDATE SET user_left_channel_id = $2"""
            await self.bot.pool.execute(query, interaction.guild_id, channel.id)
            await interaction.followup.send(f"Leave log channel updated to {channel.mention}")
        else:
            interaction.followup.send("You do not have permissions to set this value.")
            
    @app_commands.command(name="search", description="Search users database for a user by name")
    @app_commands.describe(name="Name of user to search for")
    async def search(self, interaction: discord.Interaction, name: str):
        await interaction.response.defer(ephemeral=True)
        entries: list[UserEntry] = await self.get_db_members_for_guild_by_search_term(name, interaction.guild_id)
        if len(entries) == 0:
            logger.info("User %d searched for %s in guild %d and found no results", interaction.user.id, name, interaction.guild_id)
            await interaction.followup.send("No entries were found with the provided search term.")
            return
        
        logger.info("User %d searched for %s in guild %d and found %d results", interaction.user.id, name, interaction.guild_id, len(entries))
        embeds: list[discord.Embed] = list()
        for entry in entries:
            embeds.append(await self.create_profile_embed(entry))

        paginator = ButtonPaginator(embeds)
        await paginator.start(interaction)

    @app_commands.command(name="profile", description="Get profile for a specific user")
    @app_commands.describe(user="User to get profile of")
    async def profile(self, interaction: discord.Interaction, user: discord.User):
        await interaction.response.defer(ephemeral=True)
        entry = await self.get_db_member_for_guild(user.id, interaction.guild_id)
        if entry is not None:
            logger.info("User %d searched for member with ID %d in guild %d", interaction.user.id, user.id, interaction.guild_id)
            embed = await self.create_profile_embed(entry)
            await interaction.followup.send(embed=embed)
        else:
            logger.info("User %d searched for member with ID %d in guild %d and found no results", interaction.user.id, user.id, interaction.guild_id)
            await interaction.followup.send("There is no entry for that user.")

    @app_commands.command(name="set_known_name", description="Updates user database with known name for user")
    @app_commands.describe(user="User to set name for", name="Name to set for user")
    async def set_known_name(self, interaction: discord.Interaction, user: discord.User, name: str):
        await interaction.response.defer(ephemeral=True)
        entry = await self.get_db_member_for_guild(user.id, interaction.guild_id)
        if entry is None:
            await interaction.followup.send("Unable to set a known name for this user as they have never been a member of the server.")
            return
        entry.known_name = name
        await self.submit_profiled_users_for_guild([entry])
        await interaction.followup.send(f"Known name for {user.mention} has been set to {name}")

    def create_recent_actions_str(self, recent_actions: list[RecentAction]):
        list_str: list[str] = list()
        actions_str = ""
        for recent_action in recent_actions:
            tmp_str = f"- On {discord.utils.format_dt(recent_action.date)}, **{recent_action.user_name or 'An unknown user'}** {recent_action.action}"
            if len(recent_action.original_value) > 0:
                tmp_str+=f", Original Value: `{recent_action.original_value}`, New Value: `{recent_action.new_value}`"
            tmp_str+="\n"
            if len(actions_str + tmp_str) > 4000:
                list_str.append(actions_str)
                actions_str = ""
            actions_str += tmp_str
        
        list_str.append(actions_str)
        return list_str

    @app_commands.command(name="get_recent_actions", description="List recent changes to users in the server")
    @app_commands.describe(items="Number of entries to list; defaults to 15 if not set")
    async def get_recent_actions(self, interaction: discord.Interaction, items: Optional[int]):
        if items is None:
            items = 15
        await interaction.response.defer(ephemeral=True)
        recent_actions: list[RecentAction] = await self.get_recent_actions_for_guild(interaction.guild_id, items)
        if len(recent_actions) > 0:
            actions_strs = self.create_recent_actions_str(recent_actions)
            embeds: list[discord.Embed] = list()
            for action_str in actions_strs:
                embed = discord.Embed(title=f"Recent Actions in {interaction.guild.name}", color=self.bot.green_color, 
                                      timestamp=discord.utils.utcnow(), description=action_str)
                embed.set_footer(text=f"Guild ID: {interaction.guild.id}")
                embeds.append(embed)
            paginator = ButtonPaginator(embeds)
            await paginator.start(interaction)
        else:
            await interaction.followup.send("There are no recorded recent actions.")

    @app_commands.command(name="activity_check", description="Provides activity information about a list of users")
    async def activity_check(self, interaction: discord.Interaction):
        await interaction.response.send_modal(UserActivityModal(self))

    async def process_activity_check(self, interaction: discord.Interaction, activity_text: str):
        names = activity_text.splitlines()
        final_names: list[str] = list()
        for name in names:
            if name.__contains__('~'):
                idx = name.find('~')
                almost_final_name = name[0:idx]
                if len(almost_final_name) <= 12:
                    final_names.append(almost_final_name)
            else:
                if len(name) <= 12:
                    final_names.append(name)
        
        activity_strs: list[str] = list()
        for name in final_names:
            logger.debug("Searching guild %d for info on user %s", interaction.guild_id, name)
            entries: list[UserEntry] = await self.get_db_members_for_guild_by_rsn(name, interaction.guild_id)
            if entries is None:
                activity_strs.append(f"- {name}: No activity data found.\n")
            else:
                for entry in entries:
                    activity_strs.append(f"- {name}: {entry.last_active.strftime('%Y-%m-%d %H:%M:%S')}\n")
        
        final_activity_strs: list[str] = list()
        activity_str = ""
        for string in activity_strs:
            if len(activity_str + string) > 4000:
                final_activity_strs.append(activity_str)
                activity_str = ""
            activity_str +=string
        final_activity_strs.append(activity_str)

        embeds: list[discord.Embed] = list()
        for final_str in final_activity_strs:
            embed = discord.Embed(title=f"Recent Activity for Users", timestamp=discord.utils.utcnow(), color=self.bot.green_color, description=final_str)
            embeds.append(embed)
        paginator = ButtonPaginator(embeds)
        await paginator.start(interaction, ephemeral=True)

class UserActivityModal(discord.ui.Modal, title="User Activity Checker"):
    activity_text = discord.ui.TextInput(label="Enter List of Users, one per line:", max_length=4000, required=True, style=discord.TextStyle.long)
    def __init__(self, cog: UserTrack):
        super().__init__()
        self.cog = cog

    async def on_submit(self, interaction: discord.Interaction):
        await interaction.response.defer(ephemeral=True)
        await self.cog.process_activity_check(interaction, self.activity_text.value)

    async def on_error(self, interaction: discord.Interaction, error: Exception):
        await interaction.followup.send(f"An error occurred when processing the activity check: {error}", ephemeral=True)
        logger.error("An error occurred when processing the activity check", error, exc_info=True)

async def setup(bot: SoAClient):
    await bot.add_cog(UserTrack(bot))