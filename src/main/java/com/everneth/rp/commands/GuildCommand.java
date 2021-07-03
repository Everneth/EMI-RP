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
    @Subcommand("remove")
    @CommandPermission("emi.rp.guild.officer")
    public void onGuildRemove(CommandSender sender, Player player)
    {
        // Get officer record and member record
        DbRow officer = getGuildMember((Player) sender);
        DbRow member = getGuildMember(player);
        // Is the officer actually an officer?
        if(officer.getInt("rank_id") > 1) {
            // Yes, are we removing a player that belongs to the officers guild?
            if(officer.getInt("guild_id").equals(member.getInt("guild_id"))) {
                // Yes, run the query
                try {
                    DB.executeUpdate(
                            "DELETE FROM guild_members WHERE player_id = ?",
                            member.getInt("player_id")
                    );
                } catch (SQLException e) {
                    RP.getPlugin().getLogger().info(e.getMessage());
                }
            }
            else
            {
                // No, silly goose!
                sender.sendMessage(player.getName() + " is not a member of your guild! >_>");
            }
        }
        else
        {
            // We're not even an officer. Why is this happening?!
            sender.sendMessage("T_T This is for officers only.");
        }
    }
    /***
     * Helper/class methods below for cleanliness.
     * Keep commands at the top.
     *
     *  NO GUILD = 0
     *  GUILD MEMBER = 1
     *  GUILD OFFICER = 2
     *  GUILD LEADER = 3
     *
     */

    private DbRow getGuildMember(Player p)
    {
        CompletableFuture<DbRow> futurePlayer;
        DbRow member = new DbRow();
        futurePlayer = DB.getFirstRowAsync(
                "SELECT player_id, guild_id, rank_id FROM players p " +
                        "INNER JOIN guild_members gm ON p.player_id = gm.player_id " +
                        "WHERE player_uuid = ?",
                p.getUniqueId().toString()
        );
        try {
            member = futurePlayer.get();
        }
        catch (Exception e)
        {
            RP.getPlugin().getLogger().info(e.getMessage());
        }
        return member;
    }

    private DbRow getPlayerRow(Player p)
    {
        CompletableFuture<DbRow> futurePlayer;
        DbRow player = new DbRow();
        futurePlayer = DB.getFirstRowAsync("SELECT * FROM players WHERE player_uuid = ?", p.getUniqueId().toString());
        try {
            player = futurePlayer.get();
        }
        catch (Exception e)
        {
            RP.getPlugin().getLogger().info(e.getMessage());
        }
        return player;
    }

    private boolean isGuilded(DbRow playerRow)
    {
        CompletableFuture<DbRow> futureRow;
        DbRow row = new DbRow();
        futureRow = DB.getFirstRowAsync(
                "SELECT * FROM guild_members WHERE player_id = ?",
                playerRow.getInt("player_id")
        );
        try
        {
            row = futureRow.get();
        }
        catch (Exception e)
        {
            RP.getPlugin().getLogger().info(e.getMessage());
        }
        return row.isEmpty();
    }
}