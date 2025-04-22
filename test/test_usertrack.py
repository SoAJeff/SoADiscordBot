import pytest
from cogs.usertrack import GuildUser, RecentCache

class FakeCache(RecentCache):
    def get_query(self):
        return "SAMPLE TEST QUERY"

    def get_cache_name(self):
        return "Test Cache"
    
    async def update_database(self):
        print(f"Update database in cache {self.get_cache_name()}")

class TestGuildUser:
    def test_guild_user_equality(self):
        user1 = GuildUser(user_id=123, guild_id=456)
        user2 = GuildUser(user_id=456, guild_id=789)
        user3 = GuildUser(user_id=123, guild_id=456)

        assert user1 == user3
        assert user2 != user3

class TestRecentCacheOperation:
    def test_add_to_cache(self):
        user1 = GuildUser(user_id=123, guild_id=456)
        user2 = GuildUser(user_id=456, guild_id=789)
        user3 = GuildUser(user_id=123, guild_id=456)

        cache = FakeCache(None)
        cache.insert_into_cache(user1)
        cache.insert_into_cache(user2)
        cache.insert_into_cache(user3)

        assert len(cache.cache.keys()) == 2

    def test_get_query(self):
        cache = FakeCache(None)
        assert cache.get_query() == "SAMPLE TEST QUERY"

    def test_get_cache_name(self):
        cache = FakeCache(None)
        assert cache.get_cache_name() == "Test Cache"
    
    @pytest.mark.asyncio(loop_scope="function")
    async def test_update_database(self):
        user1 = GuildUser(user_id=123, guild_id=456)
        user2 = GuildUser(user_id=456, guild_id=789)
        user3 = GuildUser(user_id=123, guild_id=456)

        cache = FakeCache(None)
        cache.insert_into_cache(user1)
        cache.insert_into_cache(user2)
        cache.insert_into_cache(user3)

        await cache.flush_cache()

        assert len(cache.cache.keys()) == 0