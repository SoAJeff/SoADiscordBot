import logging
import discord
from discord.ext import commands
from discord import app_commands
from datetime import date
from pydantic import BaseModel
from dataclasses import dataclass

from bot import SoAClient

logger = logging.getLogger(__name__)

class AttendanceChannel(BaseModel):
    id: int
    guild_id: int
    channel_id: int
    thread_id: int

@dataclass
class AttendanceSubmission:
    event_name: str
    date: str
    host: str
    attendees: str

class InsufficientPermissionsException(Exception):
    def __init__(self, permissions: list[str]):
        self.permissions = permissions

    def __str__(self):
        return f'Bot is missing the following permissions to be able to post to channel: {', '.join(self.permissions)}'

# Discord Modal
class EventAttendanceSubmissionModal(discord.ui.Modal, title="Submit Event Attendance"):
    def __init__(self):
        super().__init__()
        today = date.today()
        self.event_name = discord.ui.Label(text="Event Name",
                                     component=discord.ui.TextInput(max_length=100, required=True,
                                                                         style=discord.TextStyle.short))
        self.date = discord.ui.Label(text="Event Date",
                                       component=discord.ui.TextInput(max_length=20, required=True,
                                                                         style=discord.TextStyle.short,
                                                                         default=today.strftime("%d %b %Y")))
        
        self.host = discord.ui.Label(text="Event Host",
                                     component=discord.ui.TextInput(max_length=12, required=True,
                                                                    style=discord.TextStyle.short))
        
        self.attendees = discord.ui.Label(text="Event Attendees",
                                          description="To help us process attendance, please place each name on its own line.",
                                     component=discord.ui.TextInput(max_length=2048, required=True,
                                                                    style=discord.TextStyle.long))
        
        self.add_item(self.event_name)
        self.add_item(self.date)
        self.add_item(self.host)
        self.add_item(self.attendees)

    async def on_submit(self, interaction: discord.Interaction):
        try:
            await interaction.response.defer(ephemeral=True)
            client: SoAClient = interaction.client
            cog: Attendance = client.get_cog("attendance")
            submission = AttendanceSubmission(self.event_name.component.value,
                                              self.date.component.value,
                                              self.host.component.value,
                                              self.attendees.component.value)
            logger.info("Processing attendance submission for event %s from %s (%d)", submission.event_name, interaction.user, interaction.user.id)
            await cog.submit_attendance(submission)
            await interaction.followup.send("Attendance Submitted.", ephemeral=True)
        except ValueError as e:
            await interaction.followup.send(e, ephemeral=True)

# Persistent View
class StartAttendanceSubmissionView(discord.ui.View):
    def __init__(self):
        super().__init__(timeout=None)

    @discord.ui.button(label="Submit Event Attendance", style=discord.ButtonStyle.green, custom_id="submit_event_attendance", emoji="📝")
    async def start_attendance_submission(self, interaction: discord.Interaction, button: discord.ui.Button):
        logger.info("Received button press to request to submit event attendance by %d (%s)", 
                    interaction.user.id, 
                    interaction.user)
        client: SoAClient = interaction.client
        cog: Attendance = client.get_cog("attendance")
        channel = await cog.get_attendance_thread()
        if channel.thread_id == -1:
            logger.error("No attendance submission channel has been configured.")
            await interaction.response.send_message("The event attendance submission system is not configured.", ephemeral=True)
            return
        await interaction.response.send_modal(EventAttendanceSubmissionModal())

@app_commands.guild_only()
class Attendance(commands.GroupCog, name="attendance"):
    """Commands for interacting with Resource Changes"""
    def __init__(self, bot: SoAClient):
        self.bot = bot
        self.event_attendance_view = StartAttendanceSubmissionView()

    async def cog_load(self):
        self.bot.add_view(self.event_attendance_view)

    async def cog_unload(self):
        self.event_attendance_view.stop()

    async def update_attendance_thread(self, guild_id: int, channel_id: int, thread_id: int):
        query = "UPDATE attendance_channel SET guild_id = $1, channel_id = $2, thread_id = $3 WHERE id = 1"
        await self.bot.pool.execute(query, guild_id, channel_id, thread_id)

    async def get_attendance_thread(self) -> AttendanceChannel:
        query = "SELECT * FROM attendance_channel WHERE id = 1"
        row = await self.bot.pool.fetchrow(query)
        return AttendanceChannel(**row)
    
    def format_attendance_post(self, submission: AttendanceSubmission) -> str:
        sorted_list: list[str] = submission.attendees.splitlines()
        sorted_list.sort()
        return (
            f"{submission.event_name}\n"
            f"{submission.date}\n"
            f"{submission.host}\n\n"
            f"{'\n'.join(sorted_list)}"
        )
    
    def can_post_in_channel(self, user: discord.Member, channel: discord.TextChannel | discord.Thread):
        missing_permissions = []
        # Currently, we should just need to be able to see the channel and post to it
        if not channel.permissions_for(user).read_messages:
            missing_permissions.append("Read Messages")
        if not channel.permissions_for(user).send_messages:
            missing_permissions.append("Send Messages")
        if len(missing_permissions) > 0:
            raise InsufficientPermissionsException("Bot is missing permissions to be able to post to the channel", missing_permissions)
    
    async def submit_attendance(self, submission: AttendanceSubmission):
        submission_text = self.format_attendance_post(submission)
        attendance_thread = await self.get_attendance_thread()
        channel = self.bot.get_channel(attendance_thread.channel_id)
        if channel is not None:
            thread = channel.get_thread(attendance_thread.thread_id)
        if channel is None or thread is None:
            logger.debug("Attendance Channel or thread was none.")
            try:
                thread = await self.bot.fetch_channel(attendance_thread.thread_id)
            except Exception as e:
                logger.error("Error encountered when fetching thread from Discord: %s", e, exc_info=True)
                raise ValueError("An error occurred when finding the Discord thread to submit attendance to.")
        try:
            await thread.send(content=submission_text)
            logger.info("Event attendance for event %s has been submitted to thread %d",
                        submission.event_name, attendance_thread)
        except Exception as e:
            logger.error("An error was encountered when posting event attendance to attendance thread %d: %s",
                         thread.id, e, exc_info=True)
            raise ValueError("An error occurred when posting to the Discord attendance thread.")
    
    @app_commands.command(name="set_submission_thread", description="Set the attendance submission thread")
    @app_commands.describe(thread="thread to post attendance in")
    @app_commands.checks.has_permissions(bypass_slowmode=True)
    async def set_submission_thread(self, interaction: discord.Interaction, thread: discord.Thread):
        await interaction.response.defer(ephemeral=True)
        try:
            self.can_post_in_channel(interaction.guild.me, thread)
        except InsufficientPermissionsException as e:
            logger.error("Cannot set resource change channel as bot missing permissions: %s", ', '.join(e.permissions))
            await interaction.followup.send(e)
            return
        try:
            logger.info("User %s (%d) is setting attendance submission thread to %s (%d), located in parent %s (%d)",
                        interaction.user,
                        interaction.user.id,
                        thread,
                        thread.id,
                        thread.parent,
                        thread.parent_id)
            await self.update_attendance_thread(interaction.guild_id, thread.parent_id, thread.id)
            await interaction.followup.send(f"Submission thread set to {thread.mention}")
        except Exception as e:
            logger.error("Error encountered when setting the attendance thread: %s", e, exc_info=True)
            await interaction.followup.send("An error was encountered when setting the submission thread.")

    @app_commands.command(name="submit", description="Submit event attendance")
    async def submit(self, interaction: discord.Interaction):
        logger.info("User %s (%d) requesting event attendance submission modal",
                    interaction.user,
                    interaction.user.id)
        channel = await self.get_attendance_thread()
        if channel.thread_id == -1:
            logger.error("No attendance submission channel has been configured.")
            await interaction.response.send_message("The event attendance submission system is not configured.", ephemeral=True)
            return
        await interaction.response.send_modal(EventAttendanceSubmissionModal())

    @commands.command(hidden=True)
    @commands.guild_only()
    @commands.is_owner()
    async def add_event_attendance_submission_embed(self, ctx: commands.Context, channel: discord.TextChannel | discord.ForumChannel | discord.Thread):
        try:
            self.can_post_in_channel(ctx.guild.me, channel)
            embed = discord.Embed(title="Submit Event Attendance", color=self.bot.green_color)
            embed.description="Use the button below to submit event attendance."
            embed.set_author(name=ctx.guild.me.display_name, icon_url=ctx.guild.me.display_avatar.url)
            if isinstance(channel, discord.ForumChannel):
                await channel.create_thread(name="Submit Attendance Here!", embed=embed, view=self.event_attendance_view)
            else:
                await channel.send(embed=embed, view=self.event_attendance_view)
        except InsufficientPermissionsException as e:
            await ctx.reply(e)
            return
        
    @set_submission_thread.error
    async def on_command_error(self, interaction: discord.Interaction, error: app_commands.AppCommandError):
        if isinstance(error, app_commands.MissingPermissions):
            await interaction.response.send_message(f"You do not have permission to use this command. Required permissions: {', '.join(error.missing_permissions)}", ephemeral=True)
        else:
            logger.error(f"An error occurred during command execution: {error}", exc_info=True)
            await interaction.response.send_message("An error occurred while processing the command.", ephemeral=True)

async def setup(bot: SoAClient):
    await bot.add_cog(Attendance(bot))
