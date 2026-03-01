import logging
import discord
from discord.ext import commands
from discord import app_commands
import aiohttp
import config
from cogs.utils.forums import ForumRank

from bot import SoAClient

logger = logging.getLogger(__name__)
FORUMS_MEMBERS_API_ENDPOINT = "https://forums.soa-rs.com/api/core/members"
JAGEX_CLANS_API_ENDPOINT = "https://secure.runescape.com/m=clan-hiscores/members_lite.ws?clanName=Spirits%20of%20Arianwyn"
ALT_MEMBERS_API_ENDPOINT = "https://competitions.spiritsofarianwyn.com/viewnames.php?compid="

class CCMember:
    def __init__(self, name: str, rank: ForumRank):
        self.name = name
        self.rank = rank

class IpbMemberFetcher:
    def __init__(self, session: aiohttp.ClientSession, api_key: str):
        self.session = session
        self.api_key = api_key

    async def fetch(self):
        page = 1
        total_pages = 2 # So it runs at least once
        cc_members: dict[str, ForumRank] = dict()
        while page <= total_pages:
            results = await self.fetch_members_from_api(page)
            total_pages = results['totalPages']
            page += 1
            for member in results['results']:
                m: CCMember = self.map_to_member(member)
                if m is not None:
                    logger.debug(f"Processing {m.name} whose rank is {m.rank.name}")
                    cc_members[m.name] = m.rank
        return cc_members

    def map_to_member(self, result):
        # Exclude Joey - known exception case
        if result['name'] == 'Joey':
            logger.debug("Skipping Joey, known exception case")
            return None
        
        name: str = result['name']
        member_name = name.replace(" ", "_")
        rank: ForumRank = ForumRank.get_rank_by_id(result['primaryGroup']['id'])
        return CCMember(member_name, rank)

    async def fetch_members_from_api(self, page: int):
        params=[("page", page),
                ("perPage", 100)]
        async with self.session.get(url=FORUMS_MEMBERS_API_ENDPOINT,
                                    auth=aiohttp.BasicAuth(self.api_key),
                                    params=params) as resp:
            return await resp.json()

class RsClanMemberFetcher:
    def __init__(self, session: aiohttp.ClientSession):
        self.session = session
    
    async def fetch(self):
        raw_output = await self.fetch_members_from_api()
        members = self.parse_members(raw_output)
        members.pop(0)
        cc_members: dict[str, ForumRank] = dict()
        for member in members:
            m = self.map_to_member(member)
            if m is not None:
                logger.debug(f"Processing {m.name} whose rank is {m.rank.name}")
                cc_members[m.name] = m.rank
        return cc_members
            
    def map_to_member(self, member: str):
        split_listing = member.split(",")
        if split_listing[0] == "Princess_Rae":
            return CCMember("Princess_Rae", ForumRank.ELDAR)
        return CCMember(split_listing[0], ForumRank.get_rank_by_clan_chat_rank(split_listing[1]))


    def parse_members(self, raw_output: str):
        member_lines = raw_output.splitlines()
        for line in member_lines:
            print(line)
        return [s.replace(" ", "_").replace(" ", "_") for s in member_lines]

    async def fetch_members_from_api(self):
        async with self.session.get(url=JAGEX_CLANS_API_ENDPOINT) as resp:
            return await resp.text(encoding="iso-8859-1")
        
class AltFetcher:
    def __init__(self, session: aiohttp.ClientSession, alt_id: int):
        self.session = session
        self.alt_id = alt_id

    async def fetch(self):
        raw_output = await self.fetch_members_from_api()
        parsed_names = self.parse_names(raw_output)
        cc_members: dict[str, ForumRank] = dict()
        for name in parsed_names:
            cc_members[name] = ForumRank.RANKED_ALT
        return cc_members

    def parse_names(self, raw_output: str):
        begin = raw_output.index("<br><br>") + 8
        end = raw_output.index("<br><a", begin)
        names = raw_output[begin:end]
        return names.split("<br>")

    async def fetch_members_from_api(self):
        async with self.session.get(url=ALT_MEMBERS_API_ENDPOINT + str(self.alt_id)) as resp:
            return await resp.text(encoding="utf-8")

class ClanCompareProcessor:
    def __init__(self, ipb_members, rs_members, alt_members):
        self.ipb_members: dict[str, ForumRank] = ipb_members
        self.rs_members: dict[str, ForumRank] = rs_members
        self.alt_members: dict[str, ForumRank] = alt_members

    def process(self):
        self.results: list[str] = list()
        self.compared_users: set[str] = set()
        for member in self.ipb_members.keys():
            self.compare_forum_member(member)
        # We've now processed forum users.  Now lets check for alts/outliers.
        self.verify_alts()
        return self.results

    def compare_forum_member(self, member: str):
        self.compared_users.add(member)
        if self.ipb_members[member] != ForumRank.UNKNOWN:
            forum_rank = self.ipb_members[member]
            if member not in self.rs_members.keys():
                if forum_rank != ForumRank.SERE and forum_rank != ForumRank.REGISTERED:
                    msg = f"{member} is ranked as {forum_rank.name} on the forums but is not ranked in the clan chat."
                    logger.info(msg)
                    self.results.append(msg)
            else:
                cc_rank = self.rs_members[member]
                if forum_rank.clan_chat_rank != cc_rank.clan_chat_rank:
                    msg = f"{member} is ranked as {forum_rank.name} on the forums but as {cc_rank.name} in the clan chat."
                    logger.info(msg)
                    self.results.append(msg)
        else:
            if member in self.rs_members.keys():
                msg = f"{member} is ranked as an Unknown Group on the forums but is ranked as {self.rs_members[member].name} in the clan chat."
                logger.info(msg)
                self.results.append(msg)

    def verify_alts(self):
        alts: list[str] = list()
        for member in self.rs_members.keys():
            if member not in self.compared_users:
                if member in self.alt_members.keys():
                    alts.append(member)
                    if self.rs_members[member].clan_chat_rank != self.alt_members[member].clan_chat_rank:
                        msg = f"{member} is ranked as {self.rs_members[member].clan_chat_rank} in the clan chat but should be ranked an alt ({self.alt_members[member].clan_chat_rank})."
                        logger.info(msg)
                        self.results.append(msg)
                else:
                    msg = f"{member} is ranked as {self.rs_members[member].name} in the clan chat, but is not a forum user."
                    logger.info(msg)
                    self.results.append(msg)

        for alt in self.alt_members.keys():
            if alt not in alts:
                msg = f"{alt} is listed as an alt but is not ranked in the clan chat."
                logger.info(msg)
                self.results.append(msg)

@app_commands.guild_only()
@app_commands.default_permissions(moderate_members=True)
class ClanCompare(commands.GroupCog, name="clancompare"):
    def __init__(self, bot: SoAClient):
        self.bot = bot
    
    @app_commands.command(name="execute", description="Compare the SoA forum member listing against the in-game clan listing")
    async def run_clan_compare(self, interaction: discord.Interaction):
        await interaction.response.defer(ephemeral=True)
        try:
            alt_id = await self.get_alt_id_for_guild(interaction.guild_id)
            if alt_id is None:
                raise ValueError("Could not fetch alt tracker ID (it may not have been set).")
        except ValueError as e:
            logger.error("Unable to run ClanCompare: %s", e)
            await interaction.followup.send(content=f"Unable to run ClanCompare: {e}")
            return
        # Run ClanCompare and get the results
        logger.info("Executing Clan Compare...")
        results = await self.get_clan_compare_results(alt_id)
        msg = "**Clan Compare Results:**\n"
        if len(results) > 0:
            for result in results:
                msg += "\n- " + result
        else:
            msg += "\n Records for the forums and clan chat match."
        
        await interaction.followup.send(content=msg)

    async def get_clan_compare_results(self, alt_id: int):
        ipb_fetcher = IpbMemberFetcher(self.bot.session, config.FORUMS_API_KEY)
        ipb_members = await ipb_fetcher.fetch()
        rs_fetcher = RsClanMemberFetcher(self.bot.session)
        rs_members = await rs_fetcher.fetch()
        alt_fetcher = AltFetcher(self.bot.session, alt_id)
        alt_members = await alt_fetcher.fetch()
        processor = ClanCompareProcessor(ipb_members, rs_members, alt_members)
        results = processor.process()
        return results

    async def get_alt_id_for_guild(self, guild_id: int):
        try:
            query = "SELECT alt_comp_id FROM clan_compare WHERE guild_id = $1"
            return await self.bot.pool.fetchval(query, guild_id)
        except Exception:
            raise ValueError("Could not fetch alt tracker ID (it may not have been set).")
        
    async def set_alt_id_for_guild(self, guild_id: int, alt_id: int):
        query = "INSERT INTO clan_compare (guild_id, alt_comp_id) VALUES ($1, $2) ON CONFLICT (guild_id) DO UPDATE SET alt_comp_id = $2"
        await self.bot.pool.execute(query, guild_id, alt_id)

    @app_commands.command(name="update_alt_comp_id", description="Sets the ID of the alt competition tracker")
    @app_commands.describe(alt_id="Tracker ID of the alt competition to fetch the known list of alts from")
    async def update_alt_comp_id(self, interaction: discord.Interaction, alt_id: int):
        await interaction.response.defer(ephemeral=True)
        logger.info("Updating the alt tracker ID to %d for guild %d", alt_id, interaction.guild_id)
        await self. set_alt_id_for_guild(interaction.guild_id, alt_id)
        await interaction.followup.send(content=f"Alt tracker ID has been updated to {alt_id}")
        
async def setup(bot: SoAClient):
    await bot.add_cog(ClanCompare(bot))