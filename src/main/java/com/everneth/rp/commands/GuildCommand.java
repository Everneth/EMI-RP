package com.everneth.rp.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.rp.InviteManager;
import com.everneth.rp.RP;
import com.everneth.rp.models.EMIPlayer;
import com.everneth.rp.models.Guild;
import com.everneth.rp.models.GuildResponse;
import com.everneth.rp.models.Invite;
import com.everneth.rp.utils.PlayerUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.track.TrackManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import static com.everneth.rp.RP.getPermsApi;

@CommandAlias("guild")
public class GuildCommand extends BaseCommand {
    @Subcommand("create")
    @CommandPermission("emi.rp.guild")
    public void onGuildPlayerCreate(CommandSender sender, String playerName, String guildName, String primColor, String secColor, boolean isRp)
    {
        Guild guild = new Guild(guildName, playerName, primColor, secColor, isRp);

        GuildResponse response = guild.createGuild();
        sender.sendMessage(response.getMessage());
    }
    @Subcommand("accept")
    public void onGuildAccept(CommandSender sender)
    {
        GuildResponse response = new GuildResponse();
        Player player = (Player) sender;
        Invite guildInvite = InviteManager.getInviteManager().findInvite(player);

        response = guildInvite.getGuild().joinGuild(guildInvite.getGuild().getGuildId(), guildInvite.getPlayerId());
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
        GuildResponse response = Guild.leaveGuild(player.getUniqueId());
        sender.sendMessage(response.getMessage());
    }

    @Subcommand("invite")
    @CommandPermission("emi.rp.guild.officer")
    public void onGuildInvite(CommandSender sender, Player invitee)
    {
        // Gather required information to send to the model
        Player officer = (Player) sender;
        Guild guild = Guild.getGuildByOfficer(officer);

        // Send this data to the guild model, let the model figure things out.
        GuildResponse response = guild.inviteToGuild(invitee, officer, guild);

        officer.sendMessage(response.getMessage());
    }
    @Subcommand("kick")
    @CommandPermission("emi.rp.guild.officer")
    public void onGuildKick(CommandSender sender, Player playerToKick)
    {
        Player officer = (Player) sender;
        Guild guild = Guild.getGuildByOfficer(officer);
        GuildResponse response = new GuildResponse();

        response = guild.kickFromGuild(playerToKick);
        sender.sendMessage(response.getMessage());
    }
}