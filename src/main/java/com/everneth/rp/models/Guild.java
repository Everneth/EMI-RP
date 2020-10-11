package com.everneth.rp.models;

import co.aikar.idb.DB;
import com.everneth.rp.RP;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Guild {
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

    public Guild(String name, Player commandSender, String primaryColor, String secondaryColor, boolean isRp)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.name = name;
        this.leaderId = getLeaderPlayerId(commandSender);
        this.score = 0;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.bannerPath = null;
        this.isRp = isRp;
        this.createdDate = format.format(now);
        this.tier = 1;
    }

    private int createGuild(Guild guild)
    {
        try {
            DB.executeInsert(
                    "INSERT INTO guilds (guild_name, guild_leader_id, guild_score, guild_primary_color," +
                            " guild_secondary_color, guild_banner_path, guild_is_rp, guild_created_date, guild_tier) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    guild.getName(),
                    guild.getLeaderId(),
                    guild.getScore(),
                    guild.getPrimaryColor(),
                    guild.getSecondaryColor(),
                    null,
                    guild.isRp(),
                    guild.getCreatedDate(),
                    guild.getTier()
            );
        }
        catch (SQLException e)
        {
            RP.getPlugin().getLogger().warning(e.getMessage());
        }
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

    private int getLeaderPlayerId(Player leader)
    {
        return 0;
    }
}
