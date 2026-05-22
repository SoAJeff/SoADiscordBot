import logging
import discord
import traceback
import io
import config
from discord import app_commands
from discord.ext import commands
from bot import SoAClient

logger = logging.getLogger(__name__)

class ErrorHandling(commands.Cog):
    def __init__(self, bot:  SoAClient):
        self.bot: SoAClient = bot

    async def cog_load(self):
        # Save the original handlers
        self.original_command_error = self.bot.on_error
        self.original_app_error = self.bot.tree.on_error
        
        # Assign the new handlers to the bot...
        self.bot.on_error = self.on_general_error
        self.bot.tree.on_error = self.on_app_command_error

    async def cog_unload(self):
        #Load the original error handlers back into the bot when this Cog unloads.
        self.bot.on_command_error = self.original_command_error
        self.bot.tree.on_error = self.original_app_error

    async def on_app_command_error(self, interaction: discord.Interaction, error: app_commands.AppCommandError):
        # Ignore if the command already has a specific error handler
        if interaction.command.on_error:
            return
        
        if isinstance(error, app_commands.CheckFailure):
            logger.error("Caught unhandled CheckFailure from interaction: %s", interaction.command.name)
            if interaction.response.is_done():
                await interaction.followup.send("You do not have permission to use this command.")
            else:
                await interaction.response.send_message("You do not have permission to use this command.", ephemeral=True)
            return
        else:
            logger.error(f"An unexpected error occurred during command execution: {error}", exc_info=True)
            if len(config.ERROR_WEBHOOK) > 0:
                await self.report_error_to_webhook(traceback.format_exc(), f"Interaction: {interaction.command.name}")

    async def on_general_error(self, event, *args, **kwargs):
        logger.error(f'An error occurred: {event}', exc_info=True)
        if len(config.ERROR_WEBHOOK) > 0:
            await self.report_error_to_webhook(traceback.format_exc(), event)

    async def report_error_to_webhook(self, traceback: str, event: str):
        error_str = f"An unexpected error occurred within the following event: `{event}`"
        # Try and get the actual reasoning for the exception
        error_str += f"\n\nReason for exception:\n```\n{self.find_exception_reason(traceback)}```"
        hook = discord.Webhook.from_url(config.ERROR_WEBHOOK, client=self.bot)
        if len(traceback) < 1600:
            error_str += f"\n\nTraceback:\n```\n{traceback}\n```"
            await hook.send(content=error_str, username="SoA Error Reporting")
        else:
            error_str += "\n\nTraceback in attached file due to size"
            # Put full traceback into a file and upload it
            now = discord.utils.utcnow()
            attachment = discord.File(io.BytesIO(traceback.encode()), f"traceback-{now.isoformat()}.txt")
            await hook.send(content=error_str, file=attachment, username="SoA Error Reporting")

    def find_exception_reason(self, traceback: str):
        lines = traceback.splitlines()
        if "During handling of the above exception, another exception occurred:" in lines:
            # The exception would be 2 lines before this, looking at a traceback where this happens
            return lines[lines.index("During handling of the above exception, another exception occurred:") - 2]
        return lines[-1]


async def setup(bot):
    await bot.add_cog(ErrorHandling(bot))
