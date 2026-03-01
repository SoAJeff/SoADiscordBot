import discord
from discord.ext import commands
from discord import app_commands
import datetime
from datetime import datetime, timezone
import io

from bot import SoAClient

class Utilities(commands.Cog):
    def __init__(self, bot: SoAClient):
        self.bot = bot

    @app_commands.command(name="gametime", description="Provides the current time in UTC")
    async def gametime(self, interaction: discord.Interaction):
        dt: datetime = datetime.now(tz=timezone.utc)
        await interaction.response.send_message(f"Game time is currently {dt.strftime('%d-%b %H:%M')} UTC")

    @commands.command(hidden=True)
    @commands.guild_only()
    @commands.is_owner()
    async def get_guests(self, ctx: commands.Context):
        role_ids = [133924019073318913, 133924012677005312, 133924011351605248, 133923999506759680, 133924008650342400, 476542270833688597,
                    133924402097029120, 133924265949921280, 133924584456978432, 133924334266744832, 140532802541060096]
        
        guest_list: list[discord.Member] = []
        
        for member in ctx.guild.members:
            if member.bot:
                continue
            has_role = False
            for r in member.roles:
                if r.id in role_ids and has_role is False:
                    has_role = True
            
            if has_role is False:
                guest_list.append(member)

        guest_list.sort(key=lambda x:x.display_name)

        response_str = ""
        for m in guest_list:
            response_str += f"{m.display_name} | {m.name} | {m.id}\n"

        attachment = discord.File(io.BytesIO(response_str.encode()), "guest_list.txt")
        await ctx.reply("Guest list attached", file=attachment)

async def setup(bot: SoAClient):
    await bot.add_cog(Utilities(bot))