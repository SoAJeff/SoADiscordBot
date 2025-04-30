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
API_ENDPOINT = "https://forums.soa-rs.com/api/calendar/events"
DATE_FORMAT = "%Y-%m-%d"
DAILY_RECURRING = "FREQ=DAILY;INTERVAL=1;"

@dataclass
class Event:
    id: int
    title: str
    url: str
    calendar_id: int
    calendar_name: str
    start_time: datetime
    end_time: datetime
    recurrence: str
    member: str
    description: str
    is_ongoing: bool
    is_valid: bool


@app_commands.guild_only()
class Events(commands.GroupCog, name="events"):
    def __init__(self, bot: SoAClient):
        self.bot: SoAClient = bot
        self.embed_colors: dict[int, discord.Color]= dict()
        self.create_embed_color_mapping()

    def create_embed_color_mapping(self):
        self.embed_colors[1] = discord.Color.from_str("#730099") # Green, Game Event
        self.embed_colors[2] = discord.Color.from_str("#307326") # Purple, Forum Event
        self.embed_colors[3] = discord.Color.from_str("#298deb") # Teal, Discord Event

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
        
        # Generate the embeds for the day
        embeds = await self.generate_daily_event_embeds()

        # Loop through registered channels to receive and post it!
        query = "SELECT guild_id, event_post_channel_id FROM event_post_channels"
        rows = await self.bot.pool.fetch(query)
        for row in rows:
            try:
                channel = self.bot.get_channel(row['event_post_channel_id']) or await self.bot.fetch_channel(row['event_post_channel_id'])
                await channel.send(embeds=embeds)
            except discord.HTTPException as e:
                # Unregister the channel
                logger.error("HTTPException when sending message to channel: GUILD_ID: %d, CHANNEL_ID: %d", row['guild_id'], row['event_post_channel_id'])
                query = "DELETE FROM event_post_channels where guild_id = $1"
                await self.bot.pool.execute(query, row['guild_id'])


    @app_commands.command(name="list", description="List today's events")
    async def list_daily_events(self, interaction: discord.Interaction):
        await interaction.response.defer()
        event_embeds = await self.generate_daily_event_embeds()
        await interaction.edit_original_response(embeds=event_embeds)

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

    async def generate_daily_event_embeds(self):
        date: datetime = datetime.today().astimezone(tz=timezone.utc)
        events: list[Event] = await self.fetch_events_for_day(date)

        categorized_events: dict[int, list[Event]] = self.categorize_events(events)

        return self.create_event_embeds(categorized_events)

    def categorize_events(self, events: list[Event]):
        categorized_events: dict[int, list[Event]] = dict()
        for e in events:
            if categorized_events.get(e.calendar_id) is None:
                categorized_events[e.calendar_id] = list()
            categorized_events.get(e.calendar_id).append(e)
        # Return the events sorted by calendar ID (this makes in-game events be listed first)
        return dict(sorted(categorized_events.items()))
    
    def create_event_embeds(self, categorized_events: dict[int, list[Event]]):
        embeds: list[discord.Embed] = list()
        for key in categorized_events.keys():
            event_list = categorized_events.get(key)
            embed = discord.Embed(title=f"Today's {event_list[0].calendar_name}",
                                  color=self.embed_colors.get(key))
            embed.set_author(name=self.bot.user.name, icon_url=self.bot.user.display_avatar.url)
            for e in event_list:
                event_prefix = "Ongoing: " if e.is_ongoing else ""
                event_str = f"Time: {discord.utils.format_dt(e.start_time, 'F')} ({discord.utils.format_dt(e.start_time, 'R')})\n" if not e.is_ongoing else ""
                event_links = f"[Forum Link](<{e.url}>)"
                embed.add_field(name=f"{event_prefix}{e.title}", value=f"{event_str}Event Host: {e.member}\n{event_links}", inline=False)
            embeds.append(embed)
        
        return embeds
                


    async def fetch_events_for_day(self, date: datetime):
        day = date.strftime("%Y-%m-%d")
        page = 1
        total_pages = 2 #  So it runs at least once
        events: list[Event] = []
        while page < total_pages:
            results = await self.fetch_events_from_api(day, page)
            total_pages = results['totalPages']
            page += 1
            events.extend(self.get_events_from_results(results['results']))
        
        self.sort_events_by_date(events)
        return events

    def get_events_from_results(self, results):
        events: list[Event] = []
        for e in results:
            event_id = e['id']
            event_title = e['title']
            event_url = e['url']
            event_calendar_id = e['calendar']['id']
            event_calendar_name = e['calendar']['name']
            event_start_time = datetime.fromisoformat(e['start'])
            event_end_time = datetime.fromisoformat(e['end']) if e['end'] else None
            event_recurrence = e['recurrence'] or None
            event_member = e['author']['name']
            event_description = e['description']

            event = Event(event_id, event_title, event_url, event_calendar_id,
                                event_calendar_name, event_start_time, event_end_time,
                                event_recurrence, event_member, event_description,
                                False, True)
            
            self.is_ongoing_event(event)
            if event.recurrence is not None:
                self.get_date_for_recurring_event(event)

            if event.is_valid:
                events.append(event)
        
        return events

    def is_ongoing_event(self, event: Event):
        today = datetime.today().astimezone(tz=timezone.utc)
        if today.date() != event.start_time.date() and event.recurrence is not None and event.recurrence.startswith(DAILY_RECURRING):
            event.is_ongoing = True
        elif today.date() > event.start_time.date() and event.recurrence is None:
            event.is_ongoing = True

    def get_date_for_recurring_event(self, event: Event):
        # Forums give a recurrence rule that is not timezone aware, so strip the timezone for now.
        rrule_start_date = event.start_time.replace(tzinfo=None)
        try:
            logger.debug("Recurrence Rule for event [%s]: %s", event.title, event.recurrence)
            rrule = rrulestr(event.recurrence, dtstart=rrule_start_date)
        except ValueError:
            # To my knowledge this has never actually happened.  But just in case...
            logger.warning("Failed to parse rule, setting date to midnight")
            event.start_time = datetime.combine(datetime.today().astimezone(tz=timezone.utc), datetime.min.time())
        for dt in rrule.xafter(rrule_start_date, count=300, inc=True):
            if dt.date() == datetime.today().astimezone(tz=timezone.utc).date():
                # Date found.  Put the timezone back into the date and sub it into the event object
                start_date = dt.replace(tzinfo=timezone.utc)
                event.start_time = start_date
                logging.debug("Date for recurring event [%s] found!: [%s]", event.title, start_date)
                return
        
        # If we've gotten here, then this is not a valid event
        logger.warning("Failsafe reached for event [%s], no valid date found, event will not be displayed.", event.title)
        event.is_valid = False
        
    def sort_events_by_date(self, events: list[Event]):
        events.sort(key=lambda x:x.start_time)

    async def fetch_events_from_api(self, date: str, page: int):
        """Gets page of events from the forums API for the provided date."""
        params = [("rangeStart", date),
              ("rangeEnd", date),
              ("sortBy", "start"),
              ("page", page)]
        async with self.bot.session.get(url=API_ENDPOINT,
                                    auth=aiohttp.BasicAuth(config.FORUMS_API_KEY),
                                    params=params) as resp:
            return await resp.json()

async def setup(bot: SoAClient):
    await bot.add_cog(Events(bot))