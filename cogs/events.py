import logging

import discord
from discord.ext import commands, tasks
from discord import app_commands, ScheduledEvent
import aiohttp
import config
from dataclasses import dataclass
import datetime
from datetime import datetime, timezone, timedelta
from dateutil.rrule import rrulestr

from bot import SoAClient

logger = logging.getLogger(__name__)

@app_commands.guild_only()
class Events(commands.GroupCog, name="events"):
    def __init__(self, bot: SoAClient):
        self.bot: SoAClient = bot

    async def cog_load(self):
        self.daily_event_list_task.start()

    async def cog_unload(self):
        self.daily_event_list_task.cancel()

    @tasks.loop()
    async def daily_event_list_task(self):
        await self.bot.wait_until_ready()
        # Should give us midnight the next day
        wait_until = datetime.now(tz=timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0) + timedelta(days=1)

        logger.info("Waiting until %s to post event data", wait_until)

        # Wait until then
        await discord.utils.sleep_until(wait_until)
        
        # Loop through registered channels to receive and post that day's events!
        query = "SELECT guild_id, event_post_channel_id FROM event_post_channels"
        rows = await self.bot.pool.fetch(query)
        for row in rows:
            try:
                events = await self.get_todays_discord_events(row['guild_id'])
                embed = await self.generate_discord_daily_event_embed(events)
                channel = self.bot.get_channel(row['event_post_channel_id']) or await self.bot.fetch_channel(row['event_post_channel_id'])
                await channel.send(embed=embed)
            except discord.HTTPException as e:
                # Unregister the channel
                logger.error("HTTPException when sending message to channel: GUILD_ID: %d, CHANNEL_ID: %d", row['guild_id'], row['event_post_channel_id'])
                query = "DELETE FROM event_post_channels where guild_id = $1"
                await self.bot.pool.execute(query, row['guild_id'])


    @app_commands.command(name="list", description="List today's events")
    async def list_daily_events(self, interaction: discord.Interaction):
        await interaction.response.defer()
        events = await self.get_todays_discord_events(interaction.guild_id)
        embed = await self.generate_discord_daily_event_embed(events)
        await interaction.edit_original_response(embed=embed)

    @app_commands.command(name="enable_daily_posting", description="Enable daily listing of events in the specified channel")
    @app_commands.describe(channel="Channel to post updates in")
    @app_commands.checks.has_permissions(administrator=True)
    async def enable_daily_posting(self, interaction: discord.Interaction, channel: discord.TextChannel):
        await interaction.response.defer(ephemeral=True)
        query = """INSERT INTO event_post_channels (guild_id, event_post_channel_id) VALUES ($1, $2)
              ON CONFLICT (guild_id) DO UPDATE SET event_post_channel_id = $2"""
        await self.bot.pool.execute(query, interaction.guild_id, channel.id)
        logger.info("Enabled daily event posting. GUILD_ID: %d, CHANNEL_ID: %d", interaction.guild_id, channel.id)
        await interaction.followup.send(f"Event listing channel updated to {channel.mention}")

    @app_commands.command(name="disable_daily_posting", description="Disable daily event posting")
    @app_commands.checks.has_permissions(administrator=True)
    async def disable_daily_posting(self, interaction: discord.Interaction):
        await interaction.response.defer(ephemeral=True)
        query = "DELETE FROM event_post_channels where guild_id = $1"
        result = await self.bot.pool.execute(query, interaction.guild_id)
        if int(result.split()[1]) == 1:
            logger.info("Event listing disabled. GUILD_ID=%d", interaction.guild_id)
            await interaction.followup.send("Event listing disabled.")
            return
        else:
            await interaction.followup.send("Event listing was not enabled, so no changes have been made.")

    async def get_todays_discord_events(self, guild_id: int):
        todays_events: list[discord.ScheduledEvent] = []
        guild = self.bot.get_guild(guild_id)
        events = guild.scheduled_events
        if len(events) > 0:
            final_events_list = [e for e in events if e.status.name == "scheduled" or e.status.name == "active"]
            final_events_list.sort(key=lambda x: x.start_time)
            today = datetime.now(tz=timezone.utc).date()
            for e in final_events_list:
                if e.start_time.date() == today:
                    todays_events.append(e)
                elif e.start_time.date() < today and e.status == discord.EventStatus.active:
                    # Ongoing event
                    todays_events.append(e)

        return todays_events

    async def generate_discord_daily_event_embed(self, events: list[discord.ScheduledEvent]):
        embed = discord.Embed(title="Today's SoA Events",
                              color=discord.Color.from_str("#307326"))
        embed.set_author(name=self.bot.user.name, icon_url=self.bot.user.display_avatar.url)
        if len(events) == 0:
            embed.description="No events to list for today."
        else:
            today = datetime.now(tz=timezone.utc).date()
            for e in events:
                if e.start_time.date() < today and e.status == discord.EventStatus.active:
                    event_name = f"Ongoing: {e.name}"
                    event_str = f"Event Host: {e.creator.mention}\n[Discord Link](<{e.url}>)"
                    embed.add_field(name=event_name, value=event_str, inline=False)
                else:
                    event_str=f"Time: {discord.utils.format_dt(e.start_time, 'F')} ({discord.utils.format_dt(e.start_time, 'R')})\n"
                    event_str+=f"Event Host: {e.creator.mention}\n[Discord Link](<{e.url}>)"
                    embed.add_field(name=e.name, value=event_str, inline=False)

        return embed


async def setup(bot: SoAClient):
    await bot.add_cog(Events(bot))