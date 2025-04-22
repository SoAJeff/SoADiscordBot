import logging
import discord
from discord.ext import commands
from discord import app_commands
import aiohttp
import config
import datetime
import calendar
from datetime import datetime
from dataclasses import dataclass
from cogs.utils.forums import ForumRank

from bot import SoAClient

logger = logging.getLogger(__name__)
API_ENDPOINT = "https://forums.soa-rs.com/api/core/members"

@dataclass
class MemberBirthdate:
    name: str
    birthdate: int

class Members(commands.Cog):
    def __init__(self, bot: SoAClient):
        self.bot = bot
        self.session = aiohttp.ClientSession()

    async def cog_unload(self):
        await self.session.close()

    @app_commands.command(name="birthdays", description="Gets a list of SoA forum members with birthdays during the current month")
    @app_commands.describe(month="Month to search (as word)")
    @app_commands.guild_only()
    async def birthdays(self, interaction: discord.Interaction, month: str):
        await interaction.response.defer(ephemeral=True)
        try:
            month_int = self.get_month_number(month)
        except ValueError:
            await interaction.edit_original_response(content="The month entered is not a valid value.")
            return
        members: list[MemberBirthdate] = await self.get_birthdays_for_month(month_int)
        if len(members) > 0:
            month_name = calendar.month_name[month_int]
            logger.info("Total users with birthday in %s: %d", month_name, len(members))
            embed = discord.Embed(title=f"SoA Birthdays in {month_name}", color=self.bot.green_color, timestamp=discord.utils.utcnow())
            embed.set_author(name=self.bot.user.name, icon_url=self.bot.user.display_avatar.url)
            dates = list()
            for member in members:
                dates.append(f"- {member.name}: {month_name} {member.birthdate}")
            embed.description='\n'.join(dates)
            await interaction.edit_original_response(embed=embed)
        else:
            await interaction.edit_original_response(content=f"There are no birthdays in {month}")


    async def get_birthdays_for_month(self, month: int):
        page = 1
        total_pages = 2 # So it runs at least once
        members: list[MemberBirthdate] = list()
        while page < total_pages:
            results = await self.fetch_members_from_api(page, self.get_groups_for_members())
            total_pages = results['totalPages']
            page += 1
            members.extend(self.check_birthdates(results['results'], month))

        members.sort(key=lambda x:x.birthdate)
        return members
    
    def check_birthdates(self, results, month: int):
        members: list[MemberBirthdate] = list()
        for member in results:
            if member['birthday'] is not None:
                birthdate: str = member['birthday']
                parsed_date = birthdate.split("/")
                if int(parsed_date[0]) == month:
                    logger.debug("User has birthdate in month: %s, date: %d", member['name'], int(parsed_date[1]))
                    members.append(MemberBirthdate(member['name'], int(parsed_date[1])))
        return members

    def get_month_number(self, month_name):
        try:
            return datetime.strptime(month_name, "%B").month
        except ValueError:
            return datetime.strptime(month_name, "%b").month
        
    def get_groups_for_members(self):
        tuples = list()
        tuples.append(("group[]", ForumRank.ONTARI.id))
        tuples.append(("group[]", ForumRank.ELDAR.id))
        tuples.append(("group[]", ForumRank.LIAN.id))
        tuples.append(("group[]", ForumRank.ADMINISTRATOR.id))
        tuples.append(("group[]", ForumRank.ADELE.id))
        tuples.append(("group[]", ForumRank.ARQUENDI.id))
        tuples.append(("group[]", ForumRank.VORONWE.id))
        tuples.append(("group[]", ForumRank.ELENDUR.id))
        tuples.append(("group[]", ForumRank.SADRON.id))
        tuples.append(("group[]", ForumRank.ATHEM.id))
        tuples.append(("group[]", ForumRank.MYRTH.id))
        tuples.append(("group[]", ForumRank.TYLAR.id))
        return tuples
        
    async def fetch_members_from_api(self, page: int, groups: list):
        params=[("page", page),
                ("perPage", 100)]
        params.extend(groups)
        async with self.session.get(url=API_ENDPOINT, 
                                    auth=aiohttp.BasicAuth(config.FORUMS_API_KEY),
                                    params=params) as resp:
            return await resp.json()


async def setup(bot: SoAClient):
    await bot.add_cog(Members(bot))