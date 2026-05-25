from cogs.utils.rsapi import RsClanMemberFetcher
import cogs.utils.forums as forums
import pytest

SAMPLE_OUTPUT="""Clanmate, Clan Rank, Total XP, Kills
Princess Rae,Owner,3173211584,10
Applejuiceaj,Deputy Owner,1653176209,3
MandyPandy,Deputy Owner,3719991284,1
Scotty,Deputy Owner,710916200,0
Firesteal918,Deputy Owner,2895116122,0
Boss Shanks,Deputy Owner,3472210442,0
Tyrian Jr,Overseer,1283491641,0
Sesquialtera,Coordinator,497885361,0
Msottement,Coordinator,254227639,0
Elktz,Coordinator,949966796,0
Tim,Coordinator,200000000,0"""

class MockClanMemberFetcher(RsClanMemberFetcher):
    def __init__(self, session, url: str):
        super().__init__(session, url)

    async def fetch_members_from_api(self):
        self.raw_output = SAMPLE_OUTPUT
    
class TestClanMemberFetcher:

    @pytest.mark.asyncio(loop_scope="function")
    async def test_parse_members(self):
        fetcher = MockClanMemberFetcher(None, None)
        assert len(SAMPLE_OUTPUT.splitlines()) == 12
        await fetcher.fetch_members_from_api()
        assert len(fetcher.raw_output.splitlines()) == 12
        members = fetcher.parse_members()
        assert len(members) == 11

    @pytest.mark.asyncio(loop_scope="function")
    async def test_is_user_clan_member(self):
        fetcher = MockClanMemberFetcher(None, None)
        assert await fetcher.is_user_clan_member("Applejuiceaj") == True
        assert await fetcher.is_user_clan_member("Princess_Rae") == True
        assert await fetcher.is_user_clan_member("Noob") == False

    @pytest.mark.asyncio(loop_scope="function")
    async def test_get_clan_member_info(self):
        fetcher = MockClanMemberFetcher(None, None)
        member = await fetcher.get_clan_member_info("Applejuiceaj")
        assert member.name == "Applejuiceaj"
        assert member.rank == forums.ForumRank.ELDAR
        assert member.xp == 1653176209

        member = await fetcher.get_clan_member_info("Princess Rae")
        assert member.name == "Princess_Rae"
        assert member.rank == forums.ForumRank.ELDAR
        assert member.xp == 3173211584