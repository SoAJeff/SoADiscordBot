import discord
from discord.ext import commands
from discord import app_commands
import datetime
from datetime import datetime, timezone

from bot import SoAClient

class Utilities(commands.Cog):
    def __init__(self, bot: SoAClient):
        self.bot = bot

    @app_commands.command(name="gametime", description="Provides the current time in UTC")
    async def gametime(self, interaction: discord.Interaction):
        dt: datetime = datetime.now(tz=timezone.utc)
        await interaction.response.send_message(f"Game time is currently {dt.strftime('%d-%b %H:%M')} UTC")

async def setup(bot: SoAClient):
    await bot.add_cog(Utilities(bot))