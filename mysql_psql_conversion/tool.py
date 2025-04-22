import asyncio
import asyncpg
import aiomysql
import config
import logging
from datetime import datetime

logger = logging.getLogger("migration_tool")

class DatabaseConverter:
    def __init__(self, mysql_pool: aiomysql.Pool, psql_pool: asyncpg.Pool):
        self.mysql_pool = mysql_pool
        self.psql_pool = psql_pool

    async def migrate_mysql_database(self):
         await self.migrate_users()
         await self.migrate_recent_actions()
         await self.migrate_nicknames()
    
    async def migrate_users(self):
         select_query = "SELECT * FROM users"
         insert_query = """INSERT INTO users (user_id, guild_id, user_name, known_name, display_name, last_seen, joined_server, left_server, last_active)
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)"""
         async with self.mysql_pool.acquire() as conn:
              async with conn.cursor(aiomysql.DictCursor) as cur:
                   await cur.execute(select_query)
                   rows = await cur.fetchall()
                   for row in rows:
                        await self.psql_pool.execute(insert_query, 
                                                     row['snowflake'],
                                                     row['guildsnowflake'],
                                                     row['username'],
                                                     row['knownname'],
                                                     row['displayname'],
                                                     row['lastseen'],
                                                     row['joinedserver'],
                                                     row['leftserver'],
                                                     row['lastactive'])
    
    async def migrate_recent_actions(self):
         select_query = "SELECT * FROM recentactions"
         insert_query = "INSERT INTO recent_actions (date, user_id, guild_id, action, original_value, new_value) VALUES ($1, $2, $3, $4, $5, $6)"
         async with self.mysql_pool.acquire() as conn:
              async with conn.cursor(aiomysql.DictCursor) as cur:
                   await cur.execute(select_query)
                   rows = await cur.fetchall()
                   for row in rows:
                        await self.psql_pool.execute(insert_query,
                                                     row['date'],
                                                     row['usersnowflake'],
                                                     row['guildsnowflake'],
                                                     row['action'],
                                                     row['originalvalue'],
                                                     row['newvalue'])
                        
    async def migrate_nicknames(self):
         select_query = "SELECT * FROM nicknames"
         insert_query = "INSERT INTO nicknames (user_id, guild_id, nickname) VALUES ($1, $2, $3)"
         async with self.mysql_pool.acquire() as conn:
              async with conn.cursor(aiomysql.DictCursor) as cur:
                   await cur.execute(select_query)
                   rows = await cur.fetchall()
                   for row in rows:
                        await self.psql_pool.execute(insert_query,
                                                     row['usersnowflake'],
                                                     row['guildsnowflake'],
                                                     row['displayName'])


async def create_mysql_pool():
     logger.info("Creating Mysql connection pool")
     return await aiomysql.create_pool(host=config.MYSQL_HOST, port=3306,
                                       user=config.MYSQL_USER,
                                       password=config.MYSQL_PASSWORD,
                                       db=config.MYSQL_DB)

async def create_psql_pool() -> asyncpg.Pool:
        logger.info("Creating Postgresql connection pool")
        return await asyncpg.create_pool(
            config.PSQL_URI, 
            command_timeout=300,
            max_size=20,
            min_size=20,
        )

def setup_logging():
    # Initialize logging
    fmt = '[{asctime}] [{levelname:<8}] {name}: {message}'
    dt_fmt = '%Y-%m-%d %H:%M:%S'
    lg = logging.getLogger()

    lg.setLevel(logging.INFO)
    formatter = logging.Formatter(fmt=fmt, datefmt=dt_fmt, style='{')
    # logging.basicConfig(level=logging.INFO, format=fmt, datefmt=dt_fmt, style='{')

    stream_handler = logging.StreamHandler()
    stream_handler.setFormatter(formatter)
    lg.addHandler(stream_handler)

async def main():
    setup_logging()
    # Connect to Mongo
    mysql_pool = await create_mysql_pool()
    # Connect to PSQL
    psql_pool = await create_psql_pool()
    # Convert Class
    conversion_tool = DatabaseConverter(mysql_pool, psql_pool)
    # Begin Conversion
    await conversion_tool.migrate_mysql_database()
    logger.info("Database migration completed.")

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except Exception as e:
        logger.error("Error running conversion tool:", exc_info=True)
