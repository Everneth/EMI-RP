package com.everneth.rp.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import co.aikar.idb.DbStatement;
import com.everneth.rp.RP;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@CommandAlias("guild")
public class GuildCommand extends BaseCommand {
    @Subcommand("create")
    @CommandPermission("emi.rp.guild")
    public void onGuildPlayerCreate(CommandSender sender, String name)
    {
        createGuild(name, (Player)sender);
    }
    @Subcommand("invite")
    @CommandPermission("emi.rp.guild.officer")
    public void onGuildInvite(CommandSender sender, Player player)
    {
        DbRow invitee = getPlayerRow(player);
        DbRow officer = getGuildMember((Player) sender);
        if(isGuilded(invitee))
        {
           sender.sendMessage("Cannot invite " + player.getName() + " to the guild. They must leave their current guild first.");
        }
        else if(officer.getInt("rank_id") > 1)
        {
            sender.sendMessage("Cannot invite " + player.getName() + " to the guild. You aren't an officer you jackwang...");
        }
        else
        {
            try {
                DB.executeInsert(
                        "INSERT INTO guild_members (guild_id, player_id, rank_id) VALUES (?,?,?)",
                        officer.getInt("guild_id"),
                        invitee.getInt("player_id"),
                        3
                );
            }
            catch (SQLException e)
            {
                RP.getPlugin().getLogger().info(e.getMessage());
            }
        }
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


    private void createGuild(String name, Player p1)
    {
        DB.createTransactionAsync(stm -> createGuild(name, p1, stm));
    }

    private boolean createGuild(String name, Player p1, DbStatement stm)
    {
        try {
            stm.startTransaction();
            // setup for queries
            CompletableFuture<DbRow> futurePlayer;
            DbRow result = new DbRow();
            // Let's select the player we need
            futurePlayer = DB.getFirstRowAsync(
                    "SELECT * FROM players WHERE player_uuid = ?",
                    p1.getUniqueId().toString()
            );
            // Extract it from the future and put it into a row object we can access
            try {
                result = futurePlayer.get();
            } catch (Exception e) {
                RP.getPlugin().getLogger().info(e.getMessage());
            }
            // Is this player already in a guild?
            if(isGuilded(result))
            {
                // Yes, we're done here. Notify the player that their a dumbass and leave their current guild.
                p1.sendMessage("You're already in a guild ya jackwang");
                stm.rollback();
                return false;
            }
            else
            {
                long guildId = 0;
                // First, we need to insert the new guild so we can have the ID
                // Insert returns the primary key (guild_id)
                 guildId = DB.executeInsert(
                        "INSERT INTO guilds (guild_name, leader_id) VALUES (?,?)",
                        name,
                        result.getInt("player_id")
                );

                // if we have an Id, it shouldn't be 0
                if(guildId != 0)
                {
                    // Insert the new guild leader as a member, leader ID is 3
                    DB.executeInsert(
                            "INSERT INTO guild_members (guild_id, player_id, rank_id) VALUES (?,?,?)",
                            guildId,
                            result.getInt("player_id"),
                            3
                    );
                    // return true so the transaction can commit
                    stm.commit();
                    return true;
                }
                else
                {
                    // If guildId is 0, the query failed. Lets notify the player
                    // return false so the transaction can rollback
                    stm.rollback();
                    p1.sendMessage("An error ocurred during guild creation. " +
                            "Please notify Comms and follow up with your guild request with a GM");
                    return false;
                }
            }
        }
        catch (SQLException e)
        {
            RP.getPlugin().getLogger().info(e.getMessage());
            // if for whatever reason this entire thing fails, return false for safety.
            return false;
        }
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