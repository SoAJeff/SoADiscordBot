import sys
import traceback
import config
from pkgutil import iter_modules
import discord
from discord import app_commands
from discord.ext import commands
import logging
import logging.handlers
import asyncio
import asyncpg
import aiohttp

logger = logging.getLogger("bot")

class SoACommandTree(app_commands.CommandTree):
    # overriding the on_error method for the entire tree
    async def on_error(self, interaction: discord.Interaction, error: app_commands.AppCommandError):
        if isinstance(error, app_commands.CheckFailure):
                interaction.response.send_message("You do not have permission to execute this command", ephemeral=True)

        else:
            logger.error(f"An error occurred during command execution: {error}", exc_info=True)
            if config.ERROR_WEBHOOK:
                trace = traceback.format_exc()
                error_str = f"An unexpected error occurred within the following interaction: {interaction.command.name}\n\n```\n{trace}\n```"
                hook = discord.Webhook.from_url(config.ERROR_WEBHOOK, client=self.client)
                await hook.send(content=error_str, username="SoA Error Reporting")


class SoAClient(commands.Bot):
    green_color = int('6eb62d', 16)

    def __init__(self, *, intents: discord.Intents):
        super().__init__(command_prefix=commands.when_mentioned, intents=intents, max_messages=10000, tree_cls=SoACommandTree)
        self.start_time = discord.utils.utcnow()

    async def create_psql_pool(self) -> asyncpg.Pool:
        logger.info("Creating Postgresql connection pool")
        return await asyncpg.create_pool(
            config.PSQL_URI, 
            command_timeout=300,
            max_size=20,
            min_size=20,
        )

    async def setup_hook(self):
        self.session = aiohttp.ClientSession()
        if config.INITIAL_PRESENCE:
            self.activity = discord.Game(config.INITIAL_PRESENCE)
        self.pool = await self.create_psql_pool()
        await self.load_cogs()

    async def on_ready(self):
        logger.info(f'Logged in as {self.user}')

    async def on_error(self, event, *args, **kwargs):
        logger.error(f'An error occurred: {event}', exc_info=True)
        if config.ERROR_WEBHOOK:
            trace = traceback.format_exc()
            error_str = f"An unexpected error occurred within the following event: {event}\n\n```\n{trace}\n```"
            hook = discord.Webhook.from_url(config.ERROR_WEBHOOK, client=self)
            await hook.send(content=error_str, username="SoA Error Reporting")

    async def on_disconnect(self):
        logger.info('Bot has disconnected.')

    async def on_resumed(self):
        logger.info('Bot has resumed.')

    async def close(self) -> None:
        await super().close()
        await self.session.close()

    async def load_cogs(self):
        logger.info("Loading cogs...")
        # Load all cogs within the cogs folder
        for extension in [m.name for m in iter_modules(['cogs'], prefix='cogs.')]:
            try:
                # Loads the cog: basic.py becomes cogs.basic
                await self.load_extension(extension)
                logger.info(f"Loaded extension {extension}")
            except Exception as e:
                logger.error(f"Failed to load extension {extension}: {e}")
                raise e
        logger.info("Loaded cogs!")

    @property
    def uptime(self) -> str:
        now = discord.utils.utcnow()
        delta = now - self.start_time
        hours, remainder = divmod(int(delta.total_seconds()), 3600)
        minutes, seconds = divmod(remainder, 60)
        days, hours = divmod(hours, 24)

        fmt = "{h}h {m}m {s}s"
        if days:
            fmt = "{d}d " + fmt

        return fmt.format(d=days, h=hours, m=minutes, s=seconds)


def setup_logging(console_logging: bool):
    # Initialize logging
    fmt = '[{asctime}] [{levelname:<8}] {name}: {message}'
    dt_fmt = '%Y-%m-%d %H:%M:%S'
    lg = logging.getLogger()

    lg.setLevel(logging.INFO)
    # If in future we want debug logging on for our cogs we could use this...
    # logging.getLogger('cogs').setLevel(logging.DEBUG)
    handler = logging.handlers.RotatingFileHandler(
        filename='bot.log',
        encoding='utf-8',
        maxBytes=20*1024*1024, # 20 MB per file
        backupCount=5
    )
    formatter = logging.Formatter(fmt=fmt, datefmt=dt_fmt, style='{')
    # logging.basicConfig(level=logging.INFO, format=fmt, datefmt=dt_fmt, style='{')
    handler.setFormatter(formatter)
    lg.addHandler(handler)

    if console_logging:
        stream_handler = logging.StreamHandler()
        stream_handler.setFormatter(formatter)
        lg.addHandler(stream_handler)


# Initialize intents
def setup_intents():
    intents = discord.Intents.default()
    #intents.message_content = True
    intents.presences = True
    intents.members = True  # Required for member-related events
    return intents


async def main():
    # Validate environment variables

    cmd_args = sys.argv[1:]
    console_logging = False
    if 'consolelog' in cmd_args:
        console_logging = True

    setup_logging(console_logging)
    bot = SoAClient(intents=setup_intents())
    
    async with bot:
        await bot.start(token=config.BOT_TOKEN)

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except Exception as e:
        logger.error(f'Error running the bot: {e}')