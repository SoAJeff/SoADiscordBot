import pytest

from cogs.names import Names
from cogs.utils.rsapi import RsClanMemberFetcher

class MockClanMemberFetcher(RsClanMemberFetcher):
    def __init__(self, session, url: str):
        super().__init__(session, url)

    async def is_user_clan_member(self, name: str):
        if name == "Name A" or name == "Name B":
            return True
        return False

def test_is_valid_rsn_valid():
    cog = Names(None)
    assert cog.is_valid_rsn("Applejuiceaj") is True

def test_is_valid_rsn_too_long():
    cog = Names(None)
    with pytest.raises(ValueError, match="A RSN cannot be longer than 12 characters"):
        cog.is_valid_rsn("Applejuiceaj1234")
    
def test_is_valid_rsn_invalid_characters():
    cog = Names(None)
    with pytest.raises(ValueError, match="The name you submitted does not match the format of a valid RSN."):
        cog.is_valid_rsn("!nV@l1dC#@rs")

def test_determine_new_nickname_simple():
    cog = Names(None)
    assert cog.determine_new_nickname("Current", "Current", "New") == "New"

def test_determine_new_nickname_advanced():
    cog = Names(None)
    assert cog.determine_new_nickname("Current Name", "Current", "New") == "New Name"

@pytest.mark.asyncio(loop_scope="function")
async def test_is_user_in_clan_valid():
    original = "Name C"
    new_name = "Name A"

    fetcher = MockClanMemberFetcher(None, None)
    cog = Names(None)
    assert await cog.is_user_in_clan(fetcher, original, new_name) is True

@pytest.mark.asyncio(loop_scope="function")
async def test_is_user_in_clan_orig_in_clan():
    original = "Name B"
    new_name = "Name A"

    fetcher = MockClanMemberFetcher(None, None)
    cog = Names(None)
    assert await cog.is_user_in_clan(fetcher, original, new_name) is False

@pytest.mark.asyncio(loop_scope="function")
async def test_is_user_in_clan_new_not_in_clan():
    original = "Name C"
    new_name = "Name D"

    fetcher = MockClanMemberFetcher(None, None)
    cog = Names(None)
    assert await cog.is_user_in_clan(fetcher, original, new_name) is False

def test_parse_name_list():
    orig_list = (
        "Andrew5245 | <@335874404611260438>\n"
        "Apazos | <@856287966733533195>\n"
        "Applejuiceaj | <@134426385019043840>\n"
        "Arch u D3d 1 | <@422219213172899841>\n"
        "Aria Umbra | <@521041266423889943>\n"
    )

    cog = Names(None)
    names = cog.parse_name_list(orig_list.splitlines(), 1234)
    assert len(names) == 5

    # Checking 2 of these should be enough to ensure that everything is iterating correctly
    assert names[0].runescape_name == "Andrew5245"
    assert names[0].user_id == 335874404611260438

    assert names[2].runescape_name == "Applejuiceaj"
    assert names[2].user_id == 134426385019043840