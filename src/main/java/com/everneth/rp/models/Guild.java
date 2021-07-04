package com.everneth.rp.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.rp.RP;
import com.everneth.rp.utils.PlayerUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.track.DemotionResult;
import net.luckperms.api.track.PromotionResult;
import net.luckperms.api.track.Track;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Guild {

    private int guildId;
    private String name;
    private int leaderId;
    private int score;
    private String primaryColor;
    private String secondaryColor;
    private String bannerPath;
    private boolean isRp;
    private String createdDate;
    private int tier;

    public Guild(){}

    // guild creation constructor
    public Guild(String name, String guildLeaderName, String primaryColor, String secondaryColor, boolean isRp)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.name = name;
        this.leaderId = getLeaderPlayerId(guildLeaderName);
        this.score = 0;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.bannerPath = null;
        this.isRp = isRp;
        this.createdDate = format.format(now);
        this.tier = 1;
    }

    // guild get constructor
    public Guild(int guildId, String name, int guildLeaderId, int score, String primaryColor, String secondaryColor, String createdDate, int tier)    {

        this.guildId = guildId;
        this.name = name;
        this.leaderId = guildLeaderId;
        this.score = score;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.createdDate = createdDate;
        this.tier = tier;
    }

    public GuildResponse createGuild()
    {
        GuildResponse response = checksPass(this);
        long guild_id = 0;
        if(response.isSuccessfulAction())
        {
            try {
                guild_id = DB.executeInsert(
                        "INSERT INTO guilds (guild_name, guild_leader_id, guild_score, guild_primary_color," +
                                " guild_secondary_color, guild_banner_path, guild_is_rp, guild_created_date, guild_tier) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        this.getName(),
                        this.getLeaderId(),
                        this.getScore(),
                        this.getPrimaryColor(),
                        this.getSecondaryColor(),
                        null,
                        this.isRp(),
                        this.getCreatedDate(),
                        this.getTier()
                );
                DB.executeInsert("INSERT INTO guild_members (guild_id, player_id, rank) VALUES (?, ?, ?)",
                        guild_id,
                        this.getLeaderId(),
                        1);
                response.setSuccessfulAction(true);
                response.setMessage("Guild creation successful!");
            } catch (SQLException e) {
                RP.getPlugin().getLogger().warning(e.getMessage());
                response.setMessage("Guild creation failed. Please check logs as this may be a SQL error.");
                response.setSuccessfulAction(false);
            }
        }
        return response;
    }

    public GuildResponse kickFromGuild(Player playerToKick)
    {
        GuildResponse response = new GuildResponse();
        EMIPlayer ep = PlayerUtils.getEMIPlayer(playerToKick.getUniqueId());
        // To get this far, the sender must be an officer. Do the guilds match?
        GuildMember playerGuildInfo = this.getGuildMember(playerToKick);

        if(playerGuildInfo.getGuildId() == this.guildId) {
            try {
                DB.executeUpdate(
                        "UPDATE guild_members SET guild_id = 0 WHERE player_id = ?",
                        ep.getId()
                );
                response.setMessage("Member has been removed from the guild.");
                response.setSuccessfulAction(true);
            } catch (SQLException e) {
                RP.getPlugin().getLogger().info(e.getMessage());
                response.setMessage("An error occurred during removal. Please contact a GM.");
                response.setSuccessfulAction(false);
            }
        }
        else
        {
            response.setMessage("This player is not a member of your guild!");
            response.setSuccessfulAction(false);
        }
        return response;
    }

    public static GuildResponse leaveGuild(UUID playerUuid) {
        DbRow result = new DbRow();
        GuildResponse response = new GuildResponse();
        try {
            result = DB.getFirstRowAsync("SELECT p.player_id, gm.guild_id FROM players p INNER JOIN guild_members gm ON players.player_id = gm.player_id WHERE player_uuid = ?", playerUuid.toString()).get();
        } catch (Exception e) {
            response.setMessage("Could not leave guild. There is no guild to leave.");
            response.setSuccessfulAction(false);
        }
        try {
            DB.executeUpdate("DELETE FROM guild_members WHERE player_id = ?",
                    result.getInt("player_id"));
            response.setMessage("Successfully left guild.");
            response.setSuccessfulAction(true);
        } catch (SQLException e) {
            response.setMessage("Error while removing player from guild. Please contact staff.");
            response.setSuccessfulAction(false);
        }
        return response;
    }

    public GuildResponse inviteToGuild(Player invitedPlayer, Player guildOfficer, Guild guild)
    {
        GuildResponse response = new GuildResponse();
        int invitedPlayerId = 0;
        for(EMIPlayer ep : RP.getOnlinePlayers())
            if(ep.getUniqueId().equals(invitedPlayer.getUniqueId().toString()))
                invitedPlayerId = ep.getId();
        Invite guildInvite = new Invite(guild, invitedPlayerId, invitedPlayer, guildOfficer);
        if(!isGuilded(invitedPlayerId))
        {
            if(invitedPlayerId != 0)
            {
                guildInvite.send();
                response.setMessage(invitedPlayer.getName() + " has been invited to your guild.");
                response.setSuccessfulAction(true);
            }
            else
            {
                response.setMessage("An error occurred while sending your invite. Is the player online?");
                response.setSuccessfulAction(false);
            }
        }
        else
        {
            response.setMessage("This player is already in a guild!");
            response.setSuccessfulAction(false);
        }
        return response;
    }

    public GuildResponse demoteMember(Player playerToDemote)
    {
        LuckPerms LP = RP.getPermsApi();
        GuildResponse response = new GuildResponse();
        EMIPlayer ep = PlayerUtils.getEMIPlayer(playerToDemote.getUniqueId());
        // To get this far, the sender must be an officer. Do the guilds match?
        GuildMember playerGuildInfo = this.getGuildMember(playerToDemote);

        if(playerGuildInfo.getGuildId() == this.guildId) {
            // call LP to do the thing
            Track track = LP.getTrackManager().getTrack(
                    this.getName().replaceAll("\\s", "").toLowerCase());
            User user = LP.getUserManager().getUser(playerToDemote.getUniqueId());
            ImmutableContextSet ctx = ImmutableContextSet.of("server", "main");
            DemotionResult result = track.demote(user, ctx);
            if(result.wasSuccessful())
            {
                response.setMessage(playerToDemote.getName() + " was demoted!");
                response.setSuccessfulAction(true);
            }
            else
            {
                response.setMessage("Error demoting member. Please contact a GM.");
                response.setSuccessfulAction(false);
            }
        }
        else
        {
            response.setSuccessfulAction(false);
            response.setMessage("This player is not a part of your guild!");
        }

        return response;
    }

    public GuildResponse promoteMember(Player playerToPromote)
    {
        LuckPerms LP = RP.getPermsApi();
        GuildResponse response = new GuildResponse();
        EMIPlayer ep = PlayerUtils.getEMIPlayer(playerToPromote.getUniqueId());
        // To get this far, the sender must be an officer. Do the guilds match?
        GuildMember playerGuildInfo = this.getGuildMember(playerToPromote);

        if(playerGuildInfo.getGuildId() == this.guildId) {
            Track track = LP.getTrackManager().getTrack(
                    this.getName().replaceAll("\\s", "").toLowerCase());
            User user = LP.getUserManager().getUser(playerToPromote.getUniqueId());
            ImmutableContextSet ctx = ImmutableContextSet.of("server", "main");
            PromotionResult result = track.promote(user, ctx);
            if(result.wasSuccessful())
            {
                response.setMessage(playerToPromote.getName() + " was promoted!");
                response.setSuccessfulAction(true);
            }
            else
            {
                response.setMessage("Error promoting member. Please contact a GM.");
                response.setSuccessfulAction(false);
            }
        }
        else
        {
            response.setSuccessfulAction(false);
            response.setMessage("This player is not a part of your guild!");
        }

        return response;
    }

    public GuildResponse joinGuild(int guildId, int playerId) {
        GuildResponse response = new GuildResponse();
        EMIPlayer invitedPlayer = PlayerUtils.getEMIPlayer(playerId);
        try {
            DB.executeInsert(
                    "INSERT INTO guild_members (guild_id, player_id, rank_id) VALUES (?,?,?)",
                    guildId,
                    playerId,
                    3
            );
            response.setMessage(invitedPlayer.getName() + " has joined the guild!");
            response.setSuccessfulAction(true);
        } catch (SQLException e) {
            RP.getPlugin().getLogger().info(e.getMessage());
            response.setMessage("AN error occurred. Please contact a GM for assistance.");
            response.setSuccessfulAction(false);
        }
        return response;
    }

    private GuildResponse checksPass(Guild guild)
    {
        if(guild.getLeaderId() == 0)
            return new GuildResponse("Invalid player name. Are you sure the name is spelled correctly?", false);
        // If we made it this far, let's check and see if this player is guilded
        if(isGuilded(guild.getLeaderId()))
            return new GuildResponse("This player is still guilded... please have them /gquit and try again.", false);
        // Valid player, not guilded... lets check and see if there is a guild by the name we want
        if(guild.guildExists(guild.getName()))
            return new GuildResponse("A guild already exists by this name.", false);
        // We've passed all checks
        return new GuildResponse("Guild creation checks passed. If this message is returned, an error occurred during creation.", true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getBannerPath() {
        return bannerPath;
    }

    public void setBannerPath(String bannerPath) {
        this.bannerPath = bannerPath;
    }

    public boolean isRp() {
        return isRp;
    }

    public void setRp(boolean rp) {
        isRp = rp;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    private int getLeaderPlayerId(String leader)
    {

        DbRow result = PlayerUtils.getPlayerRow(leader);
        if(result.isEmpty())
            return 0;
        else
            return result.getInt("guild_leader_id");
    }
    public int getGuildId()
    {
        return this.guildId;
    }
    public void setGuildId(int guildId)
    {
        this.guildId = guildId;
    }

    public static boolean isGuilded(int playerId)
    {
        CompletableFuture<DbRow> futureRow;
        DbRow row = new DbRow();
        futureRow = DB.getFirstRowAsync(
                "SELECT * FROM guild_members WHERE player_id = ?",
                playerId
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

    private boolean guildExists(String name)
    {
        CompletableFuture<DbRow> futureRow;
        DbRow row = new DbRow();
        futureRow = DB.getFirstRowAsync(
                "SELECT * FROM guilds WHERE guild_name = ?",
                name
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

    private GuildMember getGuildMember(Player p)
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
        return new GuildMember(member.getInt("player_id"), member.getInt("guild_id"));
    }

    public static Guild getGuildByOfficer(Player player)
    {
        EMIPlayer officer = new EMIPlayer();
        for(EMIPlayer p : RP.getOnlinePlayers())
            if(p.getUniqueId().equals(player.getUniqueId().toString()))
                officer = p;
        CompletableFuture<DbRow> futureRow;
        DbRow row = new DbRow();
        futureRow = DB.getFirstRowAsync(
                "SELECT * FROM guilds g JOIN guild_members gm ON" +
                        "guilds.guild_id = guild_members.guild_id WHERE " +
                        "gm.guild_member_id = ?",
                officer.getId()
        );
        try
        {
            row = futureRow.get();
        }
        catch (Exception e)
        {
            RP.getPlugin().getLogger().info(e.getMessage());
        }
        return new Guild(
                row.getInt("guild_id"),
                row.getString("guild_name"),
                row.getInt("guild_leader_id"),
                row.getInt("guild_score"),
                row.getString("guild_primary_color"),
                row.getString("guild_secondary_color"),
                row.getString("guild_created_date"),
                row.getInt("guild_tier")
        );
    }
}
