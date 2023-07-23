package com.everneth.rp.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.everneth.rp.InviteManager;
import com.everneth.rp.models.Guild;
import com.everneth.rp.models.ActionResponse;
import com.everneth.rp.models.Invite;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("guild")
public class GuildCommand extends BaseCommand {
    @CommandPermission("emi.rp.gm")
    @Subcommand("create")
    public void onGuildPlayerCreate(CommandSender sender, String playerName, String guildName, String primColor, String secColor, @Default("false") boolean isRp)
    {
        Guild guild = new Guild(guildName, playerName, primColor, secColor, isRp);

        ActionResponse response = guild.createGuild();
        sender.sendMessage(response.getMessage());
    }
    @Subcommand("accept")
    public void onGuildAccept(CommandSender sender)
    {
        Player player = (Player) sender;
        Invite guildInvite = InviteManager.getInviteManager().findInvite(player);

        ActionResponse response = guildInvite.getGuild().joinGuild(guildInvite.getGuild().getGuildId(), guildInvite.getPlayerId());
        if(response.isSuccessfulAction())
        {
            guildInvite.accept();
        }
        else
        {
            sender.sendMessage(response.getMessage());
        }
    }
    @Subcommand("decline")
    public void onGuildDecline(CommandSender sender)
    {
        Player player = (Player) sender;
        Invite guildInvite = InviteManager.getInviteManager().findInvite(player);
        guildInvite.decline();
    }
    @Subcommand("leave")
    public void onGuildLeave(CommandSender sender)
    {
        Player player = (Player) sender;
        ActionResponse response = Guild.leaveGuild(player.getUniqueId());
        sender.sendMessage(response.getMessage());
    }

    @CommandPermission("emi.rp.guild.officer")
    @Subcommand("invite")
    public void onGuildInvite(CommandSender sender, Player invitee)
    {
        // Gather required information to send to the model
        Player officer = (Player) sender;
        Guild guild = Guild.getGuildByOfficer(officer);

        // Send this data to the guild model, let the model figure things out.
        ActionResponse response = guild.inviteToGuild(invitee, officer, guild);

        officer.sendMessage(response.getMessage());
    }
    @CommandPermission("emi.rp.guild.officer")
    @Subcommand("kick")
    public void onGuildKick(CommandSender sender, Player playerToKick)
    {
        Player officer = (Player) sender;
        Guild guild = Guild.getGuildByOfficer(officer);
        ActionResponse response = guild.kickFromGuild(playerToKick);
        sender.sendMessage(response.getMessage());
    }
    @CommandPermission("emi.rp.guild.gm")
    @Subcommand("promote")
    public void onGuildPromote(CommandSender sender, Player playerToPromote)
    {
        Player officer = (Player) sender;
        Guild guild = Guild.getGuildByOfficer(officer);
        ActionResponse response = guild.promoteMember(playerToPromote);
        sender.sendMessage(response.getMessage());
    }
    @CommandPermission("emi.rp.guild.gm")
    @Subcommand("demote")
    public void onGuildDemote(CommandSender sender, Player playerToDemote)
    {
        Player officer = (Player) sender;
        Guild guild = Guild.getGuildByOfficer(officer);
        ActionResponse response = guild.demoteMember(playerToDemote);
        sender.sendMessage(response.getMessage());
    }
    @CommandPermission("emi.rp.gm")
    @Subcommand("remove")
    public void onGuildRemove(CommandSender sender, String guildToRemove)
    {
        Guild guild = Guild.getGuildByName(guildToRemove);
        ActionResponse response = guild.removeGuild();
        sender.sendMessage(response.getMessage());
    }
}