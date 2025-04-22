import logging
import discord
from discord.ext import commands, tasks
from discord import app_commands
import datetime
import feedparser
from datetime import datetime, timedelta
from time import mktime
import re
import xml.etree.ElementTree as ET


from bot import SoAClient

logger = logging.getLogger(__name__)

RUNESCAPE_NEWS_URL = "https://secure.runescape.com/m=news/latest_news.rss"
OLDSCHOOL_NEWS_URL = "https://secure.runescape.com/m=news/latest_news.rss?oldschool=true"
SOA_FORUMS_FEED_URL = "https://forums.soa-rs.com/rss/1-soa-promos-and-news.xml"

RUNESCAPE_NEWS_ICON = "https://i.imgur.com/wy894Wu.png"
OLDSCHOOL_NEWS_ICON = "https://i.imgur.com/2uDRr1Z.jpeg"

@app_commands.guild_only()
@app_commands.default_permissions(administrator=True)
class Feeds(commands.GroupCog, name="feeds"):
    def __init__(self, bot: SoAClient):
        self.bot: SoAClient = bot
    
    async def cog_load(self):
        self.rs_news_map: dict[str, datetime] = dict()
        self.os_news_map: dict[str, datetime] = dict()
        # Set a variable so we don't post on the first run.  This is to prevent us from spamming things we already posted on a refresh
        self.feeds_initialized = False
        logger.info("Prepopulating RuneScape News Items")
        self.initial_jagex_news_population(RUNESCAPE_NEWS_URL, self.rs_news_map)
        logger.info("Prepopulating Old School RuneScape News Items")
        self.initial_jagex_news_population(OLDSCHOOL_NEWS_URL, self.os_news_map)
        logger.info("Starting feed tasks")
        self.runescape_feed_task.start()
        self.oldschool_feed_task.start()
        self.soa_feed_task.start()

    async def cog_unload(self):
        logger.info("Stopping feed tasks due to cog unload")
        self.runescape_feed_task.cancel()
        self.oldschool_feed_task.cancel()
        self.soa_feed_task.cancel()
        del self.rs_news_map
        del self.os_news_map

    @tasks.loop(minutes=10)
    async def runescape_feed_task(self):
        await self.bot.wait_until_ready()
        logger.info("Running RuneScape RSS feed task")
        embeds = self.jagex_feed_task(RUNESCAPE_NEWS_URL, self.rs_news_map)
        if embeds is not None and len(embeds) > 0:
            query = "SELECT news_feed_channel_id from runescape_news_feed"
            rows = await self.bot.pool.fetch(query)
            for row in rows:
                logger.info("Sending embeds to %d", row['news_feed_channel_id'])
                await self.bot.get_channel(row['news_feed_channel_id']).send(embeds=embeds)

    @tasks.loop(minutes=10)
    async def oldschool_feed_task(self):
        await self.bot.wait_until_ready()
        logger.info("Running Oldschool RSS feed task")
        embeds = self.jagex_feed_task(OLDSCHOOL_NEWS_URL, self.os_news_map)
        if embeds is not None:
            query = "SELECT news_feed_channel_id from oldschool_news_feed"
            rows = await self.bot.pool.fetch(query)
            for row in rows:
                logger.info("Sending embeds to %d", row['news_feed_channel_id'])
                await self.bot.get_channel(row['news_feed_channel_id']).send(embeds=embeds)

    @tasks.loop(minutes=10)
    async def soa_feed_task(self):
        await self.bot.wait_until_ready()
        logger.info("Running SoA RSS feed task")
        new_entries = self.delta_check_soa_news()
        if len(new_entries) > 0 and self.feeds_initialized is True:
            embeds = self.create_soa_news_embed(new_entries)
            if embeds is not None:
                query = "SELECT news_feed_channel_id from soa_news_feed"
                rows = await self.bot.pool.fetch(query)
                for row in rows:
                    logger.info("Sending embeds to %d", row['news_feed_channel_id'])
                    await self.bot.get_channel(row['news_feed_channel_id']).send(embeds=embeds)
        else:
            self.feeds_initialized = True

    def jagex_feed_task(self, url: str, map: dict):
        new_entries = self.delta_check_jagex_news(url, map)
        if len(new_entries) > 0:
            return self.create_news_embed(new_entries, True if url == OLDSCHOOL_NEWS_URL else False)
        return None

    def initial_jagex_news_population(self, url: str, map: dict):
        feed:feedparser.FeedParserDict = feedparser.parse(url)
        for item in feed.entries:
            logger.debug("Prepopulated news item: %s", item.title)
            map[item.title] = item.published

    def delta_check_jagex_news(self, url: str, map: dict):
        new_entries = list()
        feed:feedparser.FeedParserDict = feedparser.parse(url)
        for item in feed.entries:
            if item.title not in map:
                logger.info("New item found! Title: %s", item.title)
                new_entries.append(item)
                map[item.title] = item.published
        return new_entries
    
    def create_news_embed(self, new_entries: list, oldschool: bool):
        embeds: list[discord.Embed] = list()
        for item in new_entries:
            logger.info("Creating embed for %s", item.title)
            embed = discord.Embed(title=item.title, url=item.link, timestamp=discord.utils.utcnow(), description=item.description)
            embed.set_author(name=f"{'OldSchool RuneScape News' if oldschool else 'RuneScape News'}", icon_url=OLDSCHOOL_NEWS_ICON if oldschool else RUNESCAPE_NEWS_ICON)
            embed.set_image(url=item.enclosures[0].href)
            embeds.append(embed)
        return embeds

    def delta_check_soa_news(self):
        new_entries = list()
        feed:feedparser.FeedParserDict = feedparser.parse(SOA_FORUMS_FEED_URL)
        now = datetime.now()
        for item in feed.entries:
            published_time = datetime.fromtimestamp(mktime(item.published_parsed))
            if (now - published_time) < timedelta(minutes=10):
                new_entries.append(item)
            else:
                logger.debug("%s, posted at %s is not within the last 10 minutes, done parsing.", item.title, published_time)
                break
        return new_entries

    def create_soa_news_embed(self, new_entries: list):
        embeds: list[discord.Embed] = list()
        for item in new_entries:
            logger.info("Creating embed for %s", item.title)
            embed = discord.Embed(title=item.title, url=item.link, timestamp=discord.utils.utcnow())
            embed.description = self.get_normalized_soa_description(item.description)
            embed.set_author(name='Spirits of Arianwyn', icon_url=self.bot.user.display_avatar.url)
            if len(item.enclosures) > 0:
                embed.set_image(url=item.enclosures[0].href)
            embeds.append(embed)
        return embeds

    def get_normalized_soa_description(self, description: str):
        fix = '<root>{}</root>'.format(description)
        content = ET.fromstring(fix).text
        normalized_description = repr(content).replace('\\n', ' ').replace('\\t', ' ')
        final_description = re.sub(r"\s+", " ", normalized_description).strip()

        # Cap description at 500 chars.  Remove first (and potentially last) due to text starting and ending with '
        if len(final_description) > 500:
            return final_description[1:501] + "..."
        return final_description[1:-1]
    
    @app_commands.command(name="enable_jagex_news_feed", description="Enable a Jagex News feed to post in a channel")
    @app_commands.describe(channel="Channel to post updates to", game="Game to post updates for")
    @app_commands.choices(game=[
        app_commands.Choice(name='RuneScape', value='runescape_news_feed'),
        app_commands.Choice(name='Old School RuneScape', value='oldschool_news_feed')
    ])
    @app_commands.checks.has_permissions(administrator=True)
    async def enable_jagex_news_feed(self, interaction: discord.Interaction, channel: discord.TextChannel, game: app_commands.Choice[str]):
        await interaction.response.defer(ephemeral=True)
        query = f"""INSERT INTO {game.value} (guild_id, news_feed_channel_id) VALUES ($1, $2)
              ON CONFLICT (guild_id) DO UPDATE SET news_feed_channel_id = $2"""
        await self.bot.pool.execute(query, interaction.guild_id, channel.id)
        logger.info("Enabled Jagex news posting. GUILD_ID: %d, CHANNEL_ID: %d, GAME: %s", interaction.guild_id, channel.id, game.name)
        await interaction.followup.send(f"News channel for {game.name} updated to {channel.mention}")
    
    @app_commands.command(name="disable_jagex_news_feed", description="Disable Jagex News feed for a specific game")
    @app_commands.describe(game="Game to disable updates for")
    @app_commands.choices(game=[
        app_commands.Choice(name='RuneScape', value='runescape_news_feed'),
        app_commands.Choice(name='Old School RuneScape', value='oldschool_news_feed')
    ])
    @app_commands.checks.has_permissions(administrator=True)
    async def disable_jagex_news_feed(self, interaction: discord.Interaction, game: app_commands.Choice[str]):
        await interaction.response.defer(ephemeral=True)
        query = f"DELETE FROM {game.value} where guild_id = $1"
        result = await self.bot.pool.execute(query, interaction.guild_id)
        if int(result.split()[1]) == 1:
            logger.info("Jagex feed disabled. GUILD_ID=%d, GAME=%s", interaction.guild_id, game.name)
            await interaction.followup.send(f"News listing disabled for {game.name}.")
            return
        else:
            await interaction.followup.send(f"News posting was not enabled for {game.name}, so no changes have been made.")

    @app_commands.command(name="enable_soa_news_feed", description="Enable the SoA News feed to post in a channel")
    @app_commands.describe(channel="Channel to post updates to")
    @app_commands.checks.has_permissions(administrator=True)
    async def enable_soa_news_feed(self, interaction: discord.Interaction, channel: discord.TextChannel):
        await interaction.response.defer(ephemeral=True)
        query = f"""INSERT INTO soa_news_feed (guild_id, news_feed_channel_id) VALUES ($1, $2)
              ON CONFLICT (guild_id) DO UPDATE SET news_feed_channel_id = $2"""
        await self.bot.pool.execute(query, interaction.guild_id, channel.id)
        logger.info("Enabled SoA news posting. GUILD_ID: %d, CHANNEL_ID: %d", interaction.guild_id, channel.id)
        await interaction.followup.send(f"News channel for SoA updated to {channel.mention}")

    @app_commands.command(name="disable_soa_news_feed", description="Disable SoA News feed for this server")
    @app_commands.checks.has_permissions(administrator=True)
    async def disable_soa_news_feed(self, interaction: discord.Interaction):
        await interaction.response.defer(ephemeral=True)
        query = f"DELETE FROM soa_news_feed where guild_id = $1"
        result = await self.bot.pool.execute(query, interaction.guild_id)
        if int(result.split()[1]) == 1:
            logger.info("SoA feed disabled. GUILD_ID=%d", interaction.guild_id)
            await interaction.followup.send(f"SoA News listing disabled.")
            return
        else:
            await interaction.followup.send(f"SoA News posting was not enabled, so no changes have been made.")

async def setup(bot: SoAClient):
    await bot.add_cog(Feeds(bot))