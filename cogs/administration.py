import logging
from pkgutil import iter_modules
from typing import Optional, Literal

import discord
from discord.ext import commands
from discord import app_commands

from bot import SoAClient

logger = logging.getLogger(__name__)

class Admin(commands.Cog):
    def __init__(self, bot: SoAClient):
        self.bot: SoAClient = bot

    @commands.command(hidden=True)
    @commands.guild_only()
    @commands.is_owner()
    async def sync(self, ctx: commands.Context, guilds: commands.Greedy[discord.Object],
                   spec: Optional[Literal["~", "*", "^"]] = None) -> None:
        """Implementation of Umbra's Sync command:
       https://about.abstractumbra.dev/discord.py/2023/01/29/sync-command-example.html """

        message = await ctx.send("Syncing commands...")
        if not guilds:
            if spec == "~":
                logger.info(f"Processing request to sync guild commands to guild {ctx.guild}")
                synced = await ctx.bot.tree.sync(guild=ctx.guild)
            elif spec == "*":
                logger.info(f"Processing request to sync global commands to guild {ctx.guild}")
                ctx.bot.tree.copy_global_to(guild=ctx.guild)
                synced = await ctx.bot.tree.sync(guild=ctx.guild)
            elif spec == "^":
                logger.info(f"Processing request to clear guild commands from guild {ctx.guild}")
                ctx.bot.tree.clear_commands(guild=ctx.guild)
                await ctx.bot.tree.sync(guild=ctx.guild)
                synced = []
            else:
                logger.info(f"Processing request to sync global commands")
                synced = await ctx.bot.tree.sync()

            logger.info(f"Sync completed, synced {len(synced)} commands {'globally' if spec is None else f'to guild {ctx.guild}.'}")
            await message.edit(content=
                               f"Synced {len(synced)} commands {'globally' if spec is None else 'to the current guild.'}"
                               )
            return

        # We will likely never use this part, but it was part of the implementation so it's here just in case.
        ret = 0
        for guild in guilds:
            try:
                await ctx.bot.tree.sync(guild=guild)
            except discord.HTTPException:
                pass
            else:
                ret += 1

        await message.edit(content=f"Synced the tree to {ret}/{len(guilds)}.")

    # Slash command to set the bot's game status
    @commands.command(hidden=True)
    @commands.is_owner()
    async def setgame(self, ctx: commands.Context, *, game: str):
        if await self.bot.is_owner(ctx.author):
            await self.bot.change_presence(activity=discord.Game(name=game))
            await ctx.send(f"Game status has been set to '{game}'")
        else:
            await ctx.send("You do not have permission to run this command.")

    # Load not loaded module
    @commands.command(hidden=True)
    @commands.is_owner()
    async def load(self, ctx: commands.Context, *, module: str):
        """Loads a module.  Adapted from
        https://github.com/Rapptz/RoboDanny/blob/cf7e4ec88882175eb18b1152ea60755d08c05de2/cogs/admin.py#L115"""
        if not module.startswith("cogs"):
            module = f"cogs.{module}"
        try:
            logger.info(f"Attempting to load cog {module}")
            await self.bot.load_extension(module)
        except commands.ExtensionError as e:
            logger.error(f'{e.__class__.__name__}: {e}')
            await ctx.send(f'{e.__class__.__name__}: {e}')
        else:
            logger.info(f"Cog {module} loaded successfully.")
            await ctx.send(f"Cog {module} loaded")

    # Unload a module
    @commands.command(hidden=True)
    @commands.is_owner()
    async def unload(self, ctx: commands.Context, *, module: str):
        """Unloads a module.  Adapted from
        https://github.com/Rapptz/RoboDanny/blob/cf7e4ec88882175eb18b1152ea60755d08c05de2/cogs/admin.py#L125"""
        if not module.startswith("cogs"):
            module = f"cogs.{module}"
        try:
            logger.info(f"Attempting to unload cog {module}")
            await self.bot.unload_extension(module)
        except commands.ExtensionError as e:
            logger.error(f'{e.__class__.__name__}: {e}')
            await ctx.send(f'{e.__class__.__name__}: {e}')
        else:
            logger.info(f"Cog {module} unloaded successfully.")
            await ctx.send(f"Module {module} unloaded.")

    # Reload modules
    @commands.group(name='reload', hidden=True, invoke_without_command=True)
    @commands.is_owner()
    async def _reload(self, ctx: commands.Context, *, module: str):
        """Reloads a module.  Adapted from
        https://github.com/Rapptz/RoboDanny/blob/cf7e4ec88882175eb18b1152ea60755d08c05de2/cogs/admin.py#L135"""
        if not module.startswith("cogs"):
            module = f"cogs.{module}"
        try:
            logger.info(f"Attempting to reload cog {module}")
            await self.bot.reload_extension(module)
        except commands.ExtensionError as e:
            logger.error(f'{e.__class__.__name__}: {e}', exc_info=True)
            await ctx.send(f'{e.__class__.__name__}: {e}')
        else:
            logger.info(f"Cog {module} reloaded successfully.")
            await ctx.send(f"Module {module} reloaded.")

    # Reload all modules
    @_reload.command(name='all', hidden=True)
    @commands.is_owner()
    async def _reload_all(self, ctx: commands.Context):
        """Reloads all modules.  Adapted from
        https://github.com/Rapptz/RoboDanny/blob/cf7e4ec88882175eb18b1152ea60755d08c05de2/cogs/admin.py#L170"""
        modules = []
        for extension in [m.name for m in iter_modules(['cogs'], prefix='cogs.')]:
            try:
                logger.info(f"Attempting to reload cog {extension}")
                await self.reload_or_load_extension(extension)
                modules.append(extension)
            except commands.ExtensionError as e:
                logger.error(f'{e.__class__.__name__}: {e}')
                await ctx.send(f'{e.__class__.__name__}: {e}')
        logger.info(f"Reload completed, successfully reloaded {len(modules)} cogs.")
        await ctx.send(f"Modules {modules} reloaded.")

    async def reload_or_load_extension(self, module: str) -> None:
        try:
            await self.bot.reload_extension(module)
        except commands.ExtensionNotLoaded:
            await self.bot.load_extension(module)

    @load.error
    @unload.error
    @_reload.error
    @_reload_all.error
    @sync.error
    async def on_command_error(self, ctx: commands.Context, error):
        if isinstance(error, commands.errors.CheckFailure):
            # We want to log this error, but pretend the command doesn't exist, so don't tell them there was an error!
            logger.error(f"User {ctx.author} ({ctx.author.id}) attempted to load or unload modules or sync commands "
                         f"but did not have permission!")
        else:
            logger.error(f"An error occurred during command execution: {error}", exc_info=True)
            await ctx.send("An error occurred while processing the command.")

async def setup(bot):
    await bot.add_cog(Admin(bot))