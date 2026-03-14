import logging
import discord
from discord.ext import commands
from discord import app_commands
from pydantic import BaseModel
from typing import Optional, Callable, List
import re
from urllib.parse import urlparse

from bot import SoAClient

logger = logging.getLogger(__name__)
SOA_STAFF=505122192896294912
JEFF_TEST=252267969617461248

# Pydantic Models
class ResourceChangeField(BaseModel):
    id: int
    guild_id: int
    field_name: str
    position: int
    link: Optional[str] = None

class ResourceChange(BaseModel):
    id: int
    username: str
    reason: str
    guild_id: int
    channel_id: int
    message_id: int

class ResourceChangeStatus(BaseModel):
    id: int
    res_change_id: int
    res_change_field: int
    res_change_value: list[str]

class InsufficientPermissionsException(Exception):
    def __init__(self, permissions: list[str]):
        self.permissions = permissions

    def __str__(self):
        return f'Bot is missing the following permissions to be able to post to channel: {', '.join(self.permissions)}'

# Discord Modals
class InitialResourceChangeModal(discord.ui.Modal, title="Add/Edit Resource Change"):
    def __init__(self, callback: Callable, name: str = None, reason: str = None, resource_change_id: int = None):
        super().__init__()
        self.callback = callback
        self.res_change_id = resource_change_id if resource_change_id is not None else -1
        self.name = discord.ui.Label(text="Clan Member(s)",
                                     component=discord.ui.TextInput(max_length=500, required=True,
                                                                         style=discord.TextStyle.short,
                                                                         default=name))
        self.reason = discord.ui.Label(text="Reason for change",
                                       component=discord.ui.TextInput(max_length=500, required=True,
                                                                         style=discord.TextStyle.short,
                                                                         default=reason))
        
        self.add_item(self.name)
        self.add_item(self.reason)

    async def on_submit(self, interaction: discord.Interaction):
        try:
            await interaction.response.defer(ephemeral=True)
            await self.callback(self.name.component.value, self.reason.component.value, interaction.guild_id, self.res_change_id)
            await interaction.followup.send("Resource Change created.", ephemeral=True)
        except ValueError as e:
            await interaction.followup.send(e, ephemeral=True)

class ResChangeFieldEditModal(discord.ui.Modal, title="Update Resource Change Field"):
    def __init__(self, fields: list[ResourceChangeField], res_change_id: int, callback: Callable):
        super().__init__()
        self.callback = callback
        self.fields = fields.copy()
        self.fields.sort(key=lambda x:x.position)
        self.res_change_id = res_change_id
        options = []
        for u in self.fields:
            options.append(discord.SelectOption(label=u.field_name, value=u.id))

        self.field = discord.ui.Label(text="Field to Update:",
                                      component=discord.ui.Select(placeholder="Select field...",
                                                                  options=options))
        self.updater = discord.ui.Label(text="Updated by...",
                                     description="Only fill this out if someone other than you performed the change",
                                     component=discord.ui.TextInput(max_length=20, required=False,
                                                                         style=discord.TextStyle.short))
        self.add_item(self.field)
        self.add_item(self.updater)

    async def on_submit(self, interaction: discord.Interaction):
        try:
            await interaction.response.defer(ephemeral=True)
            field_id=int(self.field.component.values[0])
            selected_field = next((f for f in self.fields if f.id == field_id))
            name = self.updater.component.value if self.updater.component.value != "" else interaction.user.display_name 
            await self.callback(selected_field, name, self.res_change_id)
            await interaction.followup.send("Changes saved.", ephemeral=True)
        except ValueError as e:
            await interaction.followup.send(e, ephemeral=True)

class ResChangeEditStatusModal(discord.ui.Modal, title="Edit Resource Change Field"):
    def __init__(self, status: ResourceChangeStatus, callback: Callable):
        super().__init__()
        self.callback = callback
        self.res_change_id = status.res_change_id
        self.status_id = status.id
        options = []
        for u in status.res_change_value:
            options.append(discord.SelectOption(label=u, value=u))
        logger.info("Names determined to be present for status %d: %s", status.id, ', '.join(status.res_change_value))

        self.user_field = discord.ui.Label(text="User to Remove:",
                                           component=discord.ui.Select(placeholder="Select user...",
                                                                       options=options))
        self.add_item(self.user_field)

    async def on_submit(self, interaction: discord.Interaction):
        try:
            await interaction.response.defer(ephemeral=True)
            user = self.user_field.component.values[0]
            await self.callback(user, self.res_change_id, self.status_id)
            await interaction.followup.send("Changes saved.", ephemeral=True)
        except ValueError as e:
            await interaction.followup.send(e, ephemeral=True)

#Dynamic Button
class UpdateResourceChangeDynamicButton(discord.ui.DynamicItem[discord.ui.Button], template=r'update-reschange:id:(?P<id>[0-9]+)'):
    def __init__(self, id: int):
        super().__init__(
            discord.ui.Button(
                label="Update Resource Change",
                style=discord.ButtonStyle.green,
                custom_id=f'update-reschange:id:{id}',
            )
        )
        self.id: int = id

    @classmethod
    async def from_custom_id(cls, interaction: discord.Interaction, item: discord.ui.Button, match: re.Match[str], /):
        id = int(match['id'])
        return cls(id)

    async def callback(self, interaction: discord.Interaction):
        client: SoAClient = interaction.client
        cog: ResourceChanges = client.get_cog("resource_changes")
        fields = await cog.get_res_change_fields(interaction.guild_id)
        logger.info("Received button press to request to update Resource Change ID %d by %d (%s)", 
                    self.id, 
                    interaction.user.id, 
                    interaction.user)
        await interaction.response.send_modal(ResChangeFieldEditModal(fields, self.id, cog.update_res_change_field_callback))

class StartResourceChangeView(discord.ui.View):
    def __init__(self):
        super().__init__(timeout=None)

    @discord.ui.button(label="Add New Resource Change", style=discord.ButtonStyle.green, custom_id="start_new_res_change", emoji="📝")
    async def add_new_res_change(self, interaction: discord.Interaction, button: discord.ui.Button):
        client: SoAClient = interaction.client
        cog: ResourceChanges = client.get_cog("resource_changes")
        logger.info("Received button press to request to add new resource change by %d (%s)", 
                    interaction.user.id, 
                    interaction.user)
        await interaction.response.send_modal(InitialResourceChangeModal(cog.add_new_change_callback))

@app_commands.guilds(SOA_STAFF, JEFF_TEST)
class ResourceChanges(commands.GroupCog, name="resource_changes"):
    """Commands for interacting with Resource Changes"""
    def __init__(self, bot: SoAClient):
        self.bot = bot
        self.dynamic_items = [UpdateResourceChangeDynamicButton]
        self.new_res_change_view = StartResourceChangeView()

    async def cog_load(self):
        self.bot.add_dynamic_items(*self.dynamic_items)
        self.bot.add_view(self.new_res_change_view)

    async def cog_unload(self):
        self.bot.remove_dynamic_items(*self.dynamic_items)
        self.new_res_change_view.stop()

    async def add_new_resource_change(self, name: str, reason: str, guild_id: int) -> int:
        if len(name) > 0 and len(reason) > 0:
            query = "INSERT INTO resource_change (username, reason, guild_id, channel_id, message_id) VALUES ($1, $2, $3, $4, $5) RETURNING id"
            id = await self.bot.pool.fetchval(query, name, reason, guild_id, -1, -1)
            return id
        
    async def get_resource_change(self, res_change_id: int):
        query = "SELECT * FROM resource_change WHERE id = $1"
        row = await self.bot.pool.fetchrow(query, res_change_id)
        if row is not None:
            return ResourceChange(**row)
        return None
        
    async def user_update_resource_change(self, res_change_id: int, name: str, reason: str):
        query = "UPDATE resource_change SET username = $1, reason = $2 WHERE id = $3"
        await self.bot.pool.execute(query, name, reason, res_change_id)
        
    async def set_message_id_res_change(self, res_change_id: int, channel_id: int, message_id: int):
        query = "UPDATE resource_change SET channel_id = $1, message_id = $2 WHERE id = $3"
        await self.bot.pool.execute(query, channel_id, message_id, res_change_id)

    async def get_res_change_fields(self, guild_id: int) -> list[ResourceChangeField]:
        query = "SELECT id, guild_id, field_name, position, link FROM resource_change_fields WHERE guild_id = $1 ORDER BY position"
        result = await self.bot.pool.fetch(query, guild_id)
        fields = []
        for r in result:
            fields.append(ResourceChangeField(**r))
        return fields

    async def add_initial_res_change_statuses(self, fields: list[ResourceChangeField], resource_change_id):
        query = "INSERT INTO resource_change_statuses (res_change_id, res_change_field) VALUES ($1, $2)"
        for f in fields:
            await self.bot.pool.execute(query,resource_change_id, f.id)

    async def update_res_change_status(self, status: ResourceChangeStatus):
        query = "UPDATE resource_change_statuses SET res_change_value = $1 WHERE id = $2 AND res_change_id = $3"
        await self.bot.pool.execute(query, status.res_change_value, status.id, status.res_change_id)

    async def get_res_change_status(self, status_id: int) -> ResourceChangeStatus:
        query = """SELECT id, res_change_id, res_change_field, res_change_value
                   FROM resource_change_statuses
                   WHERE id = $1"""
        result = await self.bot.pool.fetchrow(query, status_id)
        return ResourceChangeStatus(**result)

    async def get_res_change_statuses_for_change(self, id: int) -> list[ResourceChangeStatus]:
        query = """SELECT r.id, r.res_change_id, r.res_change_field, r.res_change_value
                   FROM resource_change_statuses r
                   JOIN resource_change_fields f ON f.id = r.res_change_field 
                   WHERE r.res_change_id = $1
                   ORDER BY f.position"""
        result = await self.bot.pool.fetch(query, id)
        statuses = []
        for r in result:
            statuses.append(ResourceChangeStatus(**r))
        return statuses
    
    async def get_res_change_status_for_field(self, res_change_id: int, field_id: int) -> ResourceChangeStatus:
        query = """SELECT id, res_change_id, res_change_field, res_change_value 
                   FROM resource_change_statuses 
                   WHERE res_change_id = $1 AND res_change_field = $2"""
        result = await self.bot.pool.fetchrow(query, res_change_id, field_id)
        return ResourceChangeStatus(**result)
    
    async def add_res_change_field(self, guild_id: int, name, position, link = None):
        query = "INSERT INTO resource_change_fields (guild_id, field_name, position, link) VALUES ($1, $2, $3, $4)"
        await self.bot.pool.execute(query, guild_id, name, position, link)

    async def rename_res_change_field(self, id, name):
        query = "UPDATE resource_change_fields SET field_name = $1 WHERE id = $2"
        await self.bot.pool.execute(query, name, id)

    async def move_res_change_field(self, id, position):
        query = "UPDATE resource_change_fields SET position = $1 WHERE id = $2"
        await self.bot.pool.execute(query, position, id)

    async def update_res_change_field_link(self, id, url):
        query = "UPDATE resource_change_fields SET link = $1 WHERE id = $2"
        await self.bot.pool.execute(query, url, id)

    async def set_res_change_channel_for_guild(self, guild_id: int, channel_id: int):
        query = """INSERT INTO resource_change_settings (guild_id, channel_id) VALUES ($1, $2)
             ON CONFLICT (guild_id) DO UPDATE SET channel_id = $2"""
        await self.bot.pool.execute(query, guild_id, channel_id)

    async def get_res_change_channel_for_guild(self, guild_id: int) -> int:
        query = "SELECT channel_id FROM resource_change_settings WHERE guild_id = $1"
        return await self.bot.pool.fetchval(query, guild_id)
    
    async def validate_res_change_field_exists(self, field_id: int, guild_id: int) -> bool:
        query = "SELECT EXISTS(SELECT 1 FROM resource_change_fields WHERE id = $1 AND GUILD_ID = $2)"
        result = await self.bot.pool.fetchval(query, field_id, guild_id)
        return result

    def can_post_in_channel(self, user: discord.Member, channel: discord.TextChannel):
        missing_permissions = []
        # Currently, we should just need to be able to see the channel and post to it
        if not channel.permissions_for(user).read_messages:
            missing_permissions.append("Read Messages")
        if not channel.permissions_for(user).send_messages:
            missing_permissions.append("Send Messages")
        if len(missing_permissions) > 0:
            raise InsufficientPermissionsException("Bot is missing permissions to be able to post to the channel", missing_permissions)
        
    async def add_new_change_callback(self, names: str, reason: str, guild_id: int, unused_res_change_id: int):
        logger.info("Received request to add new resource change. [Names: %s, Reason: %s]",
                     names,
                     reason)
        res_change_id = await self.add_new_resource_change(names, reason, guild_id)
        logger.debug("Request to add new res change successful, ID: %d", res_change_id)
        fields = await self.get_res_change_fields(guild_id)
        await self.add_initial_res_change_statuses(fields, res_change_id)
        logger.debug("Added fields to new resource change with ID: %d", res_change_id)
        embed = await self.create_res_change_embed(res_change_id)
        res_change_channel = await self.get_res_change_channel_for_guild(guild_id)

        view = discord.ui.View(timeout=None)
        view.add_item(UpdateResourceChangeDynamicButton(res_change_id))
        msg = await self.bot.get_channel(res_change_channel).send(embed=embed, view=view)

        # Update with Message ID
        await self.set_message_id_res_change(res_change_id, res_change_channel, msg.id)
        logger.info("Successfully added new resource change with ID: %d and posted to channel", res_change_id)

    async def update_res_change_initial_callback(self, names: str, reason: str, guild_id: int, res_change_id: int):
        if res_change_id != -1:
            logger.info("Received request to update the initial information in Resource Change: Names: %s, Reason: %s, res_change_id: %d",
                         names,
                         reason,
                         res_change_id)
            await self.user_update_resource_change(res_change_id, names, reason)
            logger.debug("Values updated for res change ID: %d in database", res_change_id)
            res_change = await self.get_resource_change(res_change_id)
            # Generate new Embed
            embed = await self.create_res_change_embed(res_change_id)
            msg = self.bot.get_guild(res_change.guild_id).get_channel(res_change.channel_id).get_partial_message(res_change.message_id)
            await msg.edit(embed=embed)
            logger.info("Successfully updated resource change with ID: %d and posted to channel", res_change_id)
        else:
            logger.error("Unexpected error when updating the initial values of a resource change.")
            raise ValueError("Invalid resource change ID was returned.  This is an error!")

    async def update_res_change_field_callback(self, field: ResourceChangeField, value: str, res_change_id: int):
        logger.info("Received request to update resource change field: field name: %s, add value: %s, res_change_id: %d",
                         field.field_name,
                         value,
                         res_change_id)
        status: ResourceChangeStatus = await self.get_res_change_status_for_field(res_change_id, field.id)
        if value not in status.res_change_value:
            logger.debug("Value was not already in field, adding.")
            status.res_change_value.append(value)
        await self.update_res_change_status(status)
        logger.debug("Field update successful.")
        res_change = await self.get_resource_change(res_change_id)
        # Generate new Embed
        embed = await self.create_res_change_embed(res_change_id)
        msg = self.bot.get_guild(res_change.guild_id).get_channel(res_change.channel_id).get_partial_message(res_change.message_id)
        await msg.edit(embed=embed)
        logger.info("Successfully updated resource change with ID: %d and posted to channel", res_change_id)

    async def update_res_change_status_callback(self, user: str, res_change_id: int, status_id: int):
        logger.info("Received request to remove user from resource change status: status id: %d, name to remove: %s, res_change_id: %d",
                         status_id,
                         user,
                         res_change_id)
        status = await self.get_res_change_status(status_id)
        if user in status.res_change_value:
            logger.debug("Removing value from status.")
            status.res_change_value.remove(user)
        await self.update_res_change_status(status)
        logger.debug("Status update successful.")
        res_change = await self.get_resource_change(res_change_id)
        # Generate new Embed
        embed = await self.create_res_change_embed(res_change_id)
        msg = self.bot.get_guild(res_change.guild_id).get_channel(res_change.channel_id).get_partial_message(res_change.message_id)
        await msg.edit(embed=embed)
        logger.info("Successfully updated resource change with ID: %d and posted to channel", res_change_id)
        
    async def create_res_change_embed(self, res_change_id: int) -> discord.Embed:
        res_change = await self.get_resource_change(res_change_id)
        fields = await self.get_res_change_fields(res_change.guild_id)
        statuses = await self.get_res_change_statuses_for_change(res_change_id)

        embed = discord.Embed(title="Resource Change", color=self.bot.green_color)
        embed.description = f"Please Update with Changes to:\n\n**RSN:** {res_change.username}\n**Reason:** {res_change.reason}\n\n"

        for s in statuses:
            field = next((f for f in fields if f.id == s.res_change_field))
            embed.description += f"- [{field.field_name}](<{field.link}>): " if field.link is not None else f"- {field.field_name}: "
            embed.description +=f"{', '.join(s.res_change_value)}\n"
        
        embed.description = embed.description.strip()

        embed.set_footer(text=f"Resource Change ID: {res_change_id}")

        return embed

    @app_commands.command(name="set_resource_change_channel", description="Set the channel for Resource Changes to be posted in")
    @app_commands.describe(channel="Channel to post resource changes in")
    @app_commands.checks.has_permissions(administrator=True)
    async def set_resource_change_channel(self, interaction: discord.Interaction, channel: discord.TextChannel):
        await interaction.response.defer(ephemeral=True)
        try:
            self.can_post_in_channel(interaction.guild.me, channel)
        except InsufficientPermissionsException as e:
            logger.error("Cannot set resource change channel as bot missing permissions: %s", ', '.join(e.permissions))
            await interaction.followup.send(e)
            return
        try:
            await self.set_res_change_channel_for_guild(interaction.guild_id, channel.id)
            logger.info("Set resource change channel to %d for guild %d", channel.id, interaction.guild_id)
            await interaction.followup.send(f"Resource Changes channel set to {channel.mention}")
        except Exception as e:
            logger.error("Error setting resource change channel: %s", e, exc_info=True)
            await interaction.followup.send("An error was encountered when setting the resource change channel.")
            return
        
    @app_commands.command(name="add_new", description="Add new resource change")
    async def add_new(self, interaction: discord.Interaction):
        await interaction.response.send_modal(InitialResourceChangeModal(self.add_new_change_callback))

    @app_commands.command(name="update_change_details", description="Update either the names or reason for the change")
    @app_commands.describe(res_change_id="ID of the resource change")
    async def update_change_details(self, interaction: discord.Interaction, res_change_id: int):
        res_change = await self.get_resource_change(res_change_id)
        if res_change is not None and res_change.guild_id == interaction.guild_id:
            await interaction.response.send_modal(InitialResourceChangeModal(self.update_res_change_initial_callback,
                                                                            res_change.username,
                                                                            res_change.reason,
                                                                            res_change_id))
        else:
            await interaction.response.send_message("A resource change with that ID does not exist for this guild.", ephemeral=True)

    @app_commands.command(name="undo_entry_update", description="Remove a user from a field for an existing res change")
    @app_commands.describe(res_change_id="ID of the resource change", field_name="Name of field to update")
    async def undo_entry_update(self, interaction: discord.Interaction, res_change_id: int, field_name: int):
        # Field name is really the field ID - hacky, but it works!  Maybe even genius!
        logger.info("Processing command to query user for status to remove a user from initated by %d for res change %d", interaction.user.id, res_change_id)
        try:
            status = await self.get_res_change_status_for_field(res_change_id, field_name)
        except:
            logger.error("No status exists for field id/resource change combo %d, %d", field_name, res_change_id)
            await interaction.response.send_message("No status exists for that field name/resource change ID combination", ephemeral=True)
            return
        if len(status.res_change_value) == 0:
            logger.error("Request to remove a name from status was rejected because there are no names assigned to that status.")
            await interaction.response.send_message("No names are assigned to this field in this resource change.", ephemeral=True)
            return
        await interaction.response.send_modal(ResChangeEditStatusModal(status, self.update_res_change_status_callback))

    @undo_entry_update.autocomplete('field_name')
    async def field_name_autocomplete(self, interaction: discord.Interaction, current: str) -> List[app_commands.Choice[str]]:
        fields = await self.get_res_change_fields(interaction.guild_id)
        # Set the display to be the name, but set the value to be the integer,
        # since that's the key we need for the DB lookups later.
        return [
            app_commands.Choice(name=field.field_name, value=field.id)
            for field in fields if current.lower() in field.field_name.lower()
        ]

    @app_commands.command(name="add_field", description="Add a Resource Change Field")
    @app_commands.describe(field_name="Name of field", position="Position in list to put field", link="Link for field (optional)")
    @app_commands.checks.has_permissions(administrator=True)
    async def add_field(self, interaction: discord.Interaction, field_name: str, position: int, link: Optional[str]):
        await interaction.response.defer(ephemeral=True)
        if position < 1:
            logger.error("Request to add new field from %d has the position at %d which is less than 1", interaction.user.id, position)
            await interaction.followup.send("The value of position must be greater than 0.")
            return
        if link is not None:
            parsed_url = urlparse(link)
            if all([parsed_url.scheme, parsed_url.netloc]) is False:
                logger.error("Request to add new field from %d has a URL that is not a valid url", interaction.user.id, link)
                await interaction.followup.send("The provided URL is not valid.")
                return
        try:
            await self.add_res_change_field(interaction.guild_id, field_name, position, link)
            logger.info("New Resource Changes field added by %d.  Parameters: Name: %s, position: %d, link: %s",
                        interaction.user.id, field_name, position, link if link is not None else "No link provided.")
            await interaction.followup.send("Field added.")
        except Exception as e:
            logger.error("Error encountered when adding field to database: %s", e, exc_info=True)
            await interaction.followup.send("Error encountered adding field.")

    @app_commands.command(name="view_fields", description="View all fields and their associated details")
    async def view_fields(self, interaction: discord.Interaction):
        await interaction.response.defer(ephemeral=True)
        try:
            fields = await self.get_res_change_fields(interaction.guild_id)
            if len(fields) > 0:
                embed = discord.Embed(title="Resource Change Fields", color=self.bot.green_color, timestamp=discord.utils.utcnow())
                embed.set_author(name=interaction.guild.me.display_name, icon_url=interaction.guild.me.display_avatar.url)
                embed.description = ""
                for f in fields:
                    embed.description += f"- **Field ID {f.id} at Position {f.position}**: `{f.field_name}`."
                    if f.link is not None:
                        embed.description += f" Configured Link: {f.link}"
                    else:
                        embed.description += " No link configured."
                    embed.description += "\n"
                
                embed.description.strip()
                await interaction.followup.send(embed=embed)
            else:
                await interaction.followup.send("No fields are configured for this guild.")
                return
        except Exception as e:
            logger.error("Error encountered when getting field info: %s", e, exc_info=True)
            await interaction.followup.send("Error encountered getting field information.")

    @app_commands.command(name="update_field_name", description="Update the name of a field")
    @app_commands.describe(field_id="Field ID to update", updated_name="Updated name for field")
    @app_commands.checks.has_permissions(administrator=True)
    async def update_field_name(self, interaction: discord.Interaction, field_id: int, updated_name: str):
        await interaction.response.defer(ephemeral=True)
        if await self.validate_res_change_field_exists(field_id, interaction.guild_id):
            try:
                await self.rename_res_change_field(field_id, updated_name)
                logger.info("Updated res change field.  ID: %d, New Name: %s", field_id, updated_name)
                await interaction.followup.send("Field name updated.")
            except Exception as e:
                logger.error("Error encountered updating field name: %s", e, exc_info=True)
                await interaction.followup.send("An error was encountered updating the field name.")
        else:
            logger.info("User %d requested to update res change field that did not exist for guild. ID: %d", interaction.user.id, field_id)
            await interaction.followup.send("That field does not exist for this server.")

    @app_commands.command(name="update_field_position", description="Update the position of a field")
    @app_commands.describe(field_id="Field ID to update", position="Updated position for field")
    @app_commands.checks.has_permissions(administrator=True)
    async def update_field_position(self, interaction: discord.Interaction, field_id: int, position: int):
        await interaction.response.defer(ephemeral=True)
        if position < 1:
            logger.error("Request to update field from %d has the position at %d which is less than 1", interaction.user.id, position)
            await interaction.followup.send("The value of position must be greater than 0.")
            return
        if await self.validate_res_change_field_exists(field_id, interaction.guild_id):
            try:
                await self.move_res_change_field(field_id, position)
                logger.info("Updated res change field.  ID: %d, New position: %d", field_id, position)
                await interaction.followup.send("Field position updated.")
            except Exception as e:
                logger.error("Error encountered updating field position: %s", e, exc_info=True)
                await interaction.followup.send("An error was encountered updating the field position.")
        else:
            logger.info("User %d requested to update res change field that did not exist for guild. ID: %d", interaction.user.id, field_id)
            await interaction.followup.send("That field does not exist for this server.")

    @app_commands.command(name="update_field_link", description="Update the link for a field")
    @app_commands.describe(field_id="Field ID to update", link="Updated link for field. Leave blank to remove")
    @app_commands.checks.has_permissions(administrator=True)
    async def update_field_link(self, interaction: discord.Interaction, field_id: int, link: Optional[str]):
        await interaction.response.defer(ephemeral=True)
        if link is not None:
            parsed_url = urlparse(link)
            if all([parsed_url.scheme, parsed_url.netloc]) is False:
                logger.error("Request to update field from %d has a URL that is not a valid url", interaction.user.id, link)
                await interaction.followup.send("The provided URL is not valid.")
                return
        if await self.validate_res_change_field_exists(field_id, interaction.guild_id):
            try:
                await self.update_res_change_field_link(field_id, link)
                logger.info("Updated res change field.  ID: %d, New link: %d", field_id, link)
                await interaction.followup.send("Field link updated.")
            except Exception as e:
                logger.error("Error encountered updating field link: %s", e, exc_info=True)
                await interaction.followup.send("An error was encountered updating the field link.")
        else:
            logger.info("User %d requested to update res change field that did not exist for guild. ID: %d", interaction.user.id, field_id)
            await interaction.followup.send("That field does not exist for this server.")

    @commands.command(hidden=True)
    @commands.guild_only()
    @commands.is_owner()
    async def add_res_change_start_embed(self, ctx: commands.Context, channel: discord.TextChannel):
        try:
            self.can_post_in_channel(ctx.guild.me, channel)
            embed = discord.Embed(title="Create New Resource Change", color=self.bot.green_color)
            embed.description="Use the button below to start a new resource change."
            embed.set_author(name=ctx.guild.me.display_name, icon_url=ctx.guild.me.display_avatar.url)
            view = StartResourceChangeView()
            await channel.send(embed=embed, view=self.new_res_change_view)
        except InsufficientPermissionsException as e:
            await ctx.reply(e)
            return

    # Catch the permission error on these...
    @set_resource_change_channel.error
    @add_field.error
    @update_field_name.error
    @update_field_position.error
    @update_field_link.error
    async def on_command_error(self, interaction: discord.Interaction, error: app_commands.AppCommandError):
        if isinstance(error, app_commands.MissingPermissions):
            await interaction.response.send_message(f"You do not have permission to use this command. Required permissions: {', '.join(error.missing_permissions)}", ephemeral=True)
        else:
            logger.error(f"An error occurred during command execution: {error}", exc_info=True)
            await interaction.response.send_message("An error occurred while processing the command.", ephemeral=True)

async def setup(bot: SoAClient):
    await bot.add_cog(ResourceChanges(bot))
