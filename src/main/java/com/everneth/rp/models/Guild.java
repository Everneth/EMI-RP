package com.everneth.rp.models;

import org.bukkit.entity.Player;

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


    private int getLeaderPlayerId(Player leader)
    {
        return 0;
    }
}
