import aiohttp
import logging
import cogs.utils.forums as forums
from dataclasses import dataclass

logger = logging.getLogger(__name__)

@dataclass
class CCMember:
    name: str
    rank: forums.ForumRank
    xp: int
    
class RsClanMemberFetcher:
    def __init__(self, session: aiohttp.ClientSession, url: str):
        self.session = session
        self.url = url
        self.raw_output: str = None

    async def is_user_clan_member(self, name: str):
        if self.raw_output is None:
            await self.fetch_members_from_api()
        members = self.parse_members()
        for m in members:
            if m.split(",")[0].lower() == name.replace(" ", "_").lower():
                return True
        return False
    
    async def get_clan_member_info(self, name: str):
        if self.raw_output is None:
            await self.fetch_members_from_api()
        members = self.parse_members()
        for m in members:
            if m.split(",")[0].lower() == name.replace(" ", "_").lower():
                return self.map_to_member(m)

    def map_to_member(self, member: str):
        split_listing = member.split(",")
        if split_listing[0] == "Princess_Rae":
            return CCMember("Princess_Rae", forums.ForumRank.ELDAR, int(split_listing[2]))
        return CCMember(split_listing[0], forums.ForumRank.get_rank_by_clan_chat_rank(split_listing[1]), int(split_listing[2]))

    def parse_members(self):
        member_lines = self.raw_output.splitlines()
        del member_lines[0] # Delete the header row
        return [s.replace(" ", "_").replace(" ", "_") for s in member_lines]

    async def fetch_members_from_api(self):
        async with self.session.get(url=self.url) as resp:
            self.raw_output = resp.text(encoding="iso-8859-1")