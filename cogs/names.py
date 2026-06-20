import discord
from discord.ext import commands
from discord import app_commands
import logging
from pydantic import BaseModel
import re
from cogs.utils.rsapi import RsClanMemberFetcher

from bot import SoAClient

logger = logging.getLogger(__name__)

RSN_REGEX = "^[A-Za-z0-9]+([ _-]{0,10}[A-Za-z0-9]+)*$"
RSN_MAX_LENGTH = 12
MEMBERS_ENDPOINT="https://secure.runescape.com/m=clan-hiscores/members_lite.ws?clanName=Spirits%20of%20Arianwyn"

ONBOARD_SERVERS=[133922153010692096, 252267969617461248]

# RSN database model
class RuneScapeName(BaseModel):
    guild_id: int
    user_id: int
    runescape_name: str

# Discord Modals
class SetRuneScapeNameModal(discord.ui.Modal, title="Update RuneScape Name"):
    def __init__(self, runescape_name: str, initial: bool):
        super().__init__()
        self.initial = initial
        self.name = discord.ui.Label(text="RuneScape Name",
                                     description="Please enter your RuneScape Name exactly as it is shown in-game",
                                     component=discord.ui.TextInput(max_length=RSN_MAX_LENGTH,
                                                                    min_length=1,
                                                                    style=discord.TextStyle.short,
                                                                    default=runescape_name if len(runescape_name) > 0 else ''))
        self.add_item(self.name)

    async def on_submit(self, interaction: discord.Interaction):
        try:
            await interaction.response.defer(ephemeral=True)
            client: SoAClient = interaction.client
            cog: Names = client.get_cog("names")
            await cog.set_name_modal_callback(interaction.user, self.name.component.value, self.initial)
            await interaction.followup.send("RuneScape Name Updated.", ephemeral=True)
        except ValueError as e:
            logger.error("An error occurred when attempting to set the RSN: %s", e, exc_info=True)
            await interaction.followup.send(e, ephemeral=True)
        except Exception as e:
            logger.error("Unexpected error occurred when setting RSN: %s", e, exc_info=True)
            await interaction.followup.send("An unexpected error occurred when updating your RSN.", ephemeral=True)

# Dynamic Button
class WelcomeUserSetRsnDynamicButton(discord.ui.DynamicItem[discord.ui.Button], template=r'new-user-set-rsn:id:(?P<id>[0-9]+)'):
    def __init__(self, id: int):
        super().__init__(
            discord.ui.Button(
                label="Set RuneScape Name",
                style=discord.ButtonStyle.green,
                custom_id=f'new-user-set-rsn:id:{id}'
            )
        )
        self.user_id = id
    
    @classmethod
    async def from_custom_id(cls, interaction: discord.Interaction, item: discord.ui.Button, match: re.Match[str], /):
        id = int(match['id'])
        return cls(id)
    
    async def callback(self, interaction: discord.Interaction):
        if interaction.user.id != self.user_id:
            logger.error("User %d tried to use the set RSN button on a message meant for %d and was rejected.", interaction.user.id, self.user_id)
            await interaction.response.send_message("Only the person for whom this message is for can use this button.", ephemeral=True)
            return
        client: SoAClient = interaction.client
        cog: Names = client.get_cog("names")
        existing_name = cog.db_get_rsn(interaction.guild.id, interaction.user.id)
        if existing_name:
            logger.error("User %d tried to set their RSN but they have already set it as part of the onboarding process.", interaction.user.id)
            await interaction.response.send_message("You have already set your RuneScape name as part of the onboarding process.", ephemeral=True)
            return

        await interaction.response.send_modal(SetRuneScapeNameModal("", True))

# Update RSN View
class UpdateRSNView(discord.ui.View):
    def __init__(self):
        super().__init__(timeout=None)

    @discord.ui.button(label="Update RuneScape Name", style=discord.ButtonStyle.green, custom_id="update_rsn")
    async def update_rsn(self, interaction: discord.Interaction, button: discord.ui.Button):
        client: SoAClient = interaction.client
        cog: Names = client.get_cog("names")
        existing_rsn = await cog.db_get_rsn(interaction.guild_id, interaction.user.id)
        if existing_rsn:
            await interaction.response.send_modal(SetRuneScapeNameModal(existing_rsn, False))
        else:
            logger.error("User %d attempting to update their RSN however the RSN is unknown to the bot", interaction.user.id)
            await interaction.response.send_message("Your existing RuneScape Name is unknown.  Please contact a member of staff to have your name set.", ephemeral=True)

# Onboarding CV2 View
class OnboardingView(discord.ui.LayoutView):
    def build_view(self, member: discord.Member):
        container = discord.ui.Container()
        container.add_item(discord.ui.MediaGallery(discord.MediaGalleryItem(media="https://soa-rs.com/img/slider/prifddinas.png")))

        # Set RSN Text
        main_content = f"## Hey there {member.mention} - welcome to the Spirits of Arianwyn Discord server!\n\nTo complete our server onboarding process, please use the button below to set your RSN."
        main_content_section = discord.ui.TextDisplay(main_content)
        container.add_item(main_content_section)

        # Set RSN Button
        set_rsn_action_row = discord.ui.ActionRow()
        set_rsn_action_row.add_item(WelcomeUserSetRsnDynamicButton(member.id))

        container.add_item(set_rsn_action_row)

        container.add_item(discord.ui.Separator(visible=True, spacing=discord.SeparatorSpacing.small))

        about_soa_content="Feel free to hop into our clan chat as a [guest](<https://runescape.wiki/w/Clan_Chat#Guests>) at **Spirits of Arianwyn**.  You are welcome to guest for as long as you'd like!"
        about_soa_content += "\n\n"
        about_soa_content += "If you're interested in applying, you can visit our [how to apply](<https://discord.com/channels/133922153010692096/1196502791523401762>) channel to learn about the process, and can start your application in the [submit application](<https://discord.com/channels/133922153010692096/1509011367682769098>) channel."
        about_soa_content += "\n\n"
        about_soa_content += "We look forward to meeting you!"

        about_soa_section = discord.ui.TextDisplay(about_soa_content)
        container.add_item(about_soa_section)

        self.add_item(container)


@app_commands.guild_only()
class Names(commands.GroupCog, name="names"):

    def __init__(self, bot: SoAClient):
        self.bot: SoAClient = bot
        self.dynamic_items = [WelcomeUserSetRsnDynamicButton]
        self.update_rsn_view = UpdateRSNView()

    async def cog_load(self):
        self.bot.add_dynamic_items(*self.dynamic_items)
        self.bot.add_view(self.update_rsn_view)

    async def cog_unload(self):
        self.bot.remove_dynamic_items(*self.dynamic_items)
        self.update_rsn_view.stop()

    async def db_set_rsn(self, name: str, guild_id: int, user_id: int):
        try:
            query = """INSERT INTO runescape_names (guild_id, user_id, runescape_name) VALUES ($1, $2, $3)
                ON CONFLICT (guild_id, user_id) DO UPDATE SET runescape_name = $3"""
            await self.bot.pool.execute(query, guild_id, user_id, name)
        except Exception:
            raise ValueError("Database error when setting RSN.")
        
    async def db_get_rsn(self, guild_id: int, user_id: int):
        try:
            query = "SELECT runescape_name FROM runescape_names WHERE guild_id = $1 AND user_id = $2"
            value = await self.bot.pool.fetchval(query, guild_id, user_id)
            return value
        except Exception:
            raise ValueError("Database error when retrieving RSN.")
        
    async def set_name_modal_callback(self, member: discord.Member, name: str, initial: bool):
        self.is_valid_rsn(name)

        if initial:
            await self.set_initial_name_modal_callback(member, name)
        else:
            await self.set_existing_name_modal_callback(member, name)

    async def set_initial_name_modal_callback(self, member: discord.Member, name: str):
        await self.db_set_rsn(name, member.guild.id, member.id)
        await member.edit(nick=name)

    async def set_existing_name_modal_callback(self, member: discord.Member, name: str):
        existing_name = await self.db_get_rsn(member.guild.id, member.id)
        fetcher = RsClanMemberFetcher(self.bot.session, MEMBERS_ENDPOINT)
        if self.is_user_in_clan(fetcher, existing_name, name):
            logger.info("Determined existing user %d who requested name change to %s is in clan.  Updating name",
                        member.id, name)
            await self.db_set_rsn(name, member.guild.id, member.id)
            new_nickname = self.determine_new_nickname(member.display_name, existing_name, name)
            await member.edit(nick=new_nickname)
            await self.add_resource_change(existing_name, name, member.guild.id)
        else:
            raise ValueError("Unable to verify name change: either the new RSN is not in the clan, or the existing RSN is still in the clan.")
        
    async def add_resource_change(self, existing_name: str, new_name: str, guild_id: int):
        logger.info("Publishing Resource Change for name change")
        reschange_cog = self.bot.get_cog("resource_changes")
        await reschange_cog.add_new_change_callback(f"{existing_name} -> {new_name}", "User submitted name change", guild_id, -1)

    @commands.Cog.listener()
    async def on_member_join(self, member: discord.Member):
        if member.guild.id in ONBOARD_SERVERS:
            view = OnboardingView()
            view.build_view(member)
            if member.guild.system_channel is not None:
                await member.guild.system_channel.send(view=view)

    @commands.command(hidden=True)
    @commands.guild_only()
    @commands.is_owner()
    async def test_member_onboard(self, ctx: commands.Context):
        await self.on_member_join(ctx.author)

    @commands.command(hidden=True)
    @commands.guild_only()
    @commands.is_owner()
    async def add_update_rsn_embed(self, ctx: commands.Context, channel: discord.TextChannel):
        try:
            embed = discord.Embed(title="Update RuneScape Name", color=self.bot.green_color)
            embed.description="Use the button below to update your RuneScape Name."
            embed.set_author(name=ctx.guild.me.display_name, icon_url=ctx.guild.me.display_avatar.url)
            await channel.send(embed=embed, view=self.update_rsn_view)
        except Exception as e:
            await ctx.reply(e)
            return
        
    @commands.command(hidden=True)
    @commands.guild_only()
    @commands.is_owner()
    async def bulk_import_rsns(self, ctx: commands.Context, attachment: discord.Attachment):
        attachment_bytes = await attachment.read()
        names_list = attachment_bytes.decode("utf-8").splitlines()
        rsn_list = self.parse_name_list(names_list, ctx.guild.id)

        for n in rsn_list:
            await self.db_set_rsn(n.runescape_name, ctx.guild.id, n.user_id)

        await ctx.reply(f"Ingested {len(rsn_list)} users into the RuneScape Names table.")
        
    def parse_name_list(self, names_list: list[str], guild_id: int):
        names: list[RuneScapeName] = list()
        for name in names_list:
            n = name.split("|")
            rsn = n[0].strip()
            id = n[1].strip().removeprefix("<@").removesuffix(">")
            id = id.removeprefix("<@").removesuffix(">")
            names.append(RuneScapeName(guild_id=guild_id, user_id=id, runescape_name=rsn))
        return names

    def is_valid_rsn(self, name: str):
        if len(name) > 12:
            raise ValueError("A RSN cannot be longer than 12 characters")
        if not re.match(RSN_REGEX, name):
            raise ValueError("The name you submitted does not match the format of a valid RSN.")
        return True
    
    async def is_user_in_clan(self, fetcher: RsClanMemberFetcher, original_name: str, new_name: str):
        logger.debug("Checking if user %s is not in clan and %s is in clan for name change",
                     original_name, new_name)
        is_original_in_clan = await fetcher.is_user_clan_member(original_name)
        is_new_in_clan = await fetcher.is_user_clan_member(new_name)

        if is_original_in_clan is False and is_new_in_clan is True:
            logger.info("User %s found to be in clan and existing name is no longer in clan", new_name)
            return True
        logger.info("User %s new name was NOT found to be in clan, refusing change", original_name)
        return False
    
    def determine_new_nickname(self, current_nickname: str, existing_rsn: str, new_rsn: str):
        return current_nickname.replace(existing_rsn, new_rsn, 1)


async def setup(bot: SoAClient):
    await bot.add_cog(Names(bot))

