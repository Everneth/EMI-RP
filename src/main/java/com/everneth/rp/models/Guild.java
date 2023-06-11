package com.everneth.rp.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.rp.RP;
import com.everneth.rp.utils.PlayerUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.track.DemotionResult;
import net.luckperms.api.track.PromotionResult;
import net.luckperms.api.track.Track;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
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
    private String friendlyName;
    private String prefix;
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
        this.friendlyName = makeFriendlyName();
        this.prefix = makePrefix();
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

    private String makeFriendlyName()
    {
        return this.getName().replaceAll(" ", "-").toLowerCase();
    }

    private String makePrefix()
    {
        //trim the string to get rid of leading or trailing whitespace, if any
        String trimmedString = this.getName().trim();
        return trimmedString.substring(0, 3) + "_";
    }

    public ActionResponse createGuild()
    {
        ActionResponse response = checksPass(this);
        long guild_id = 0;
        if(response.isSuccessfulAction())
        {
            try {
                guild_id = DB.executeInsert(
                        "INSERT INTO guilds (guild_name, guild_leader_id, guild_score, guild_primary_color," +
                                " guild_secondary_color, guild_banner_path, guild_is_rp, guild_created_date, guild_tier," +
                                " guild_friendly_name, guild_prefix) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        this.getName(),
                        this.getLeaderId(),
                        this.getScore(),
                        this.getPrimaryColor(),
                        this.getSecondaryColor(),
                        null,
                        this.isRp(),
                        this.getCreatedDate(),
                        this.getTier(),
                        this.getFriendlyName(),
                        this.getPrefix()
                );
                DB.executeInsert("INSERT INTO guild_members (guild_id, player_id, guild_rank) VALUES (?, ?, ?)",
                        guild_id,
                        this.getLeaderId(),
                        1);
                this.buildGuildPermissions();
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

    private void buildGuildPermissions()
    {
        // define the group names
        final String GUILD_MASTER = this.getPrefix() + "GuildMaster";
        final String GUILD_OFFICER = this.getPrefix() + "Officer";
        final String GUILD_MEMBER = this.getPrefix() + "Member";
        LuckPerms LP = RP.getPermsApi();
        //
        CompletableFuture<Track> trackFuture = LP.getTrackManager().createAndLoadTrack(this.getFriendlyName());
        CompletableFuture<Group> gmGrpFuture = LP.getGroupManager().createAndLoadGroup(GUILD_MASTER);
        CompletableFuture<Group> officerGrpFuture = LP.getGroupManager().createAndLoadGroup(GUILD_OFFICER);
        CompletableFuture<Group> memberGrpFuture = LP.getGroupManager().createAndLoadGroup(GUILD_MEMBER);

        EMIPlayer guildLeader = PlayerUtils.getEMIPlayer(this.getLeaderId());

        User user = LP.getUserManager().getUser(UUID.fromString(guildLeader.getUniqueId()));

        trackFuture.thenAcceptAsync(track -> {
            gmGrpFuture.thenAcceptAsync(group -> {
                user.data().add(Node.builder("group." + group.getName()).build());
                LP.getUserManager().saveUser(user);
                group.data().add(Node.builder("emi.rp.guild.gm").build());
                LP.getGroupManager().saveGroup(group);
            });
            officerGrpFuture.thenAcceptAsync(group -> {
                group.data().add(Node.builder("emi.rp.guild.officer").build());
                LP.getGroupManager().saveGroup(group);
            });
            memberGrpFuture.thenAcceptAsync(track::appendGroup);
        });
    }

    public ActionResponse kickFromGuild(Player playerToKick)
    {
        ActionResponse response = new ActionResponse();
        EMIPlayer ep = PlayerUtils.getEMIPlayer(playerToKick.getUniqueId());
        // To get this far, the sender must be an officer. Do the guilds match?
        GuildMember playerGuildInfo = this.getGuildMember(playerToKick);
        LuckPerms LP = RP.getPermsApi();

        if(playerGuildInfo.getGuildId() == this.guildId) {
            try {
                DB.executeUpdate(
                        "UPDATE guild_members SET guild_id = 0 WHERE player_id = ?",
                        ep.getId()
                );
                Collection<Node> userNodes = LP.getUserManager().getUser(playerToKick.getUniqueId()).getNodes();
                String group = "group." + this.getFriendlyName();
                for(Node node : userNodes)
                {
                    if(node.getKey().equals(group))
                        LP.getUserManager().getUser(playerToKick.getUniqueId()).data().remove(node);
                }
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

    public static ActionResponse leaveGuild(UUID playerUuid) {
        DbRow result = new DbRow();
        ActionResponse response = new ActionResponse();
        LuckPerms LP = RP.getPermsApi();
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
            Collection<Node> userNodes = LP.getUserManager().getUser(playerUuid).getNodes();
            Guild guild = Guild.getGuildByMember(playerUuid);
            String group = "group." + guild.getFriendlyName();
            for(Node node : userNodes)
            {
                if(node.getKey().equals(group))
                    LP.getUserManager().getUser(playerUuid).data().remove(node);
            }
        } catch (SQLException e) {
            response.setMessage("Error while removing player from guild. Please contact staff.");
            response.setSuccessfulAction(false);
        }
        return response;
    }

    public ActionResponse inviteToGuild(Player invitedPlayer, Player guildOfficer, Guild guild)
    {
        ActionResponse response = new ActionResponse();
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

    public ActionResponse demoteMember(Player playerToDemote)
    {
        LuckPerms LP = RP.getPermsApi();
        ActionResponse response = new ActionResponse();
        EMIPlayer ep = PlayerUtils.getEMIPlayer(playerToDemote.getUniqueId());
        // To get this far, the sender must be an officer. Do the guilds match?
        GuildMember playerGuildInfo = this.getGuildMember(playerToDemote);

        if(playerGuildInfo.getGuildId() == this.guildId) {
            // call LP to do the thing
            Track track = LP.getTrackManager().getTrack(this.getFriendlyName());
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

    public ActionResponse promoteMember(Player playerToPromote)
    {
        LuckPerms LP = RP.getPermsApi();
        ActionResponse response = new ActionResponse();
        EMIPlayer ep = PlayerUtils.getEMIPlayer(playerToPromote.getUniqueId());
        // To get this far, the sender must be an officer. Do the guilds match?
        GuildMember playerGuildInfo = this.getGuildMember(playerToPromote);

        if(playerGuildInfo.getGuildId() == this.guildId) {
            Track track = LP.getTrackManager().getTrack(this.getFriendlyName());
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

    public ActionResponse joinGuild(int guildId, int playerId) {
        ActionResponse response = new ActionResponse();
        EMIPlayer invitedPlayer = PlayerUtils.getEMIPlayer(playerId);
        LuckPerms LP = RP.getPermsApi();

        User user = LP.getUserManager().getUser(invitedPlayer.getUniqueId());
        String group = "group." + this.getFriendlyName();
        user.data().add(Node.builder(group).build());

        try {
            DB.executeInsert(
                    "INSERT INTO guild_members (guild_id, player_id, guild_rank) VALUES (?,?,?)",
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

    private ActionResponse checksPass(Guild guild)
    {
        if(guild.getLeaderId() == 0)
            return new ActionResponse("Invalid player name. Are you sure the name is spelled correctly?", false);
        // If we made it this far, let's check and see if this player is guilded
        if(isGuilded(guild.getLeaderId()))
            return new ActionResponse("This player is still guilded... please have them /gquit and try again.", false);
        // Valid player, not guilded... lets check and see if there is a guild by the name we want
        if(guild.guildExists(guild.getName()))
            return new ActionResponse("A guild already exists by this name.", false);
        // We've passed all checks
        return new ActionResponse("Guild creation checks passed. If this message is returned, an error occurred during creation.", true);
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

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private int getLeaderPlayerId(String leader)
    {

        DbRow result = PlayerUtils.getPlayerRow(leader);
        if(result.isEmpty())
            return 0;
        else
            return result.getInt("player_id");
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
        return row != null;
    }

    public ActionResponse removeGuild()
    {
        LuckPerms LP = RP.getPermsApi();
        ActionResponse response = new ActionResponse();
        final String GUILD_MASTER = this.getPrefix() + "guildmaster";
        final String GUILD_OFFICER = this.getPrefix() + "officer";
        final String GUILD_MEMBER = this.getPrefix() + "member";
        //First remove all guild members from the guild
        try {
            DB.executeUpdate("DELETE FROM guild_members WHERE guild_id = ?", this.getGuildId());
            DB.executeUpdate("DELETE FROM guilds WHERE guild_id = ?", this.getGuildId());
            LP.getGroupManager().deleteGroup(LP.getGroupManager().getGroup(GUILD_MASTER));
            LP.getGroupManager().deleteGroup(LP.getGroupManager().getGroup(GUILD_OFFICER));
            LP.getGroupManager().deleteGroup(LP.getGroupManager().getGroup(GUILD_MEMBER));
            LP.getTrackManager().deleteTrack(LP.getTrackManager().getTrack(this.getFriendlyName()));
            response.setMessage("Guild removed. Any players left in the guild have also been unguilded.");
            response.setSuccessfulAction(true);
            return response;
        }
       catch (SQLException e) {
           response.setMessage("Error while removing the guild. Please contact staff.");
           response.setSuccessfulAction(false);
           return response;
       }
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
        return row != null;
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

    public static Guild getGuildByMember(UUID uuid)
    {
        EMIPlayer member = new EMIPlayer();
        for(EMIPlayer p : RP.getOnlinePlayers())
            if(p.getUniqueId().equals(uuid.toString()))
                member = p;

        CompletableFuture<DbRow> futureRow;
        DbRow row = new DbRow();
        futureRow = DB.getFirstRowAsync(
                "SELECT * FROM guilds g JOIN guild_members gm ON" +
                        "guilds.guild_id = guild_members.guild_id WHERE " +
                        "gm.guild_member_id = ?",
                member.getId()
        );
        try
        {
            row = futureRow.get();
        }
        catch (Exception e)
        {
            RP.getPlugin().getLogger().info(e.getMessage());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = row.get("guild_created_date");
        String formattedDate = date.format(formatter);

        return new Guild(
                row.getInt("guild_id"),
                row.getString("guild_name"),
                row.getInt("guild_leader_id"),
                row.getInt("guild_score"),
                row.getString("guild_primary_color"),
                row.getString("guild_secondary_color"),
                formattedDate,
                row.getInt("guild_tier")
        );
    }

    public static Guild getGuildByName(String name)
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = row.get("guild_created_date");
        String formattedDate = date.format(formatter);

        return new Guild(
                row.getInt("guild_id"),
                row.getString("guild_name"),
                row.getInt("guild_leader_id"),
                row.getInt("guild_score"),
                row.getString("guild_primary_color"),
                row.getString("guild_secondary_color"),
                formattedDate,
                row.getInt("guild_tier")
        );
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
                "SELECT * FROM guilds g JOIN guild_members gm ON " +
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = row.get("guild_created_date");
        String formattedDate = date.format(formatter);

        return new Guild(
                row.getInt("guild_id"),
                row.getString("guild_name"),
                row.getInt("guild_leader_id"),
                row.getInt("guild_score"),
                row.getString("guild_primary_color"),
                row.getString("guild_secondary_color"),
                formattedDate,
                row.getInt("guild_tier")
        );
    }
}
