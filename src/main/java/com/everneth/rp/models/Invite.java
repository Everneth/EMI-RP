package com.everneth.rp.models;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Timer;

public class Invite {
    int guildId;
    int playerId;
    Timer timer;
    Player player;
    private TextComponent accept;
    private TextComponent decline;

    public Invite(int guildId, int playerId, Player player)
    {
        this.guildId = guildId;
        this.playerId = playerId;
        this.player = player;
        this.timer = new Timer();
        this.accept = new TextComponent();
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept"));
        accept.setText("ACCEPT");
        accept.setColor(ChatColor.GREEN);
        this.decline = new TextComponent();
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild decline"));
        accept.setText("DECLINE");
        accept.setColor(ChatColor.RED);
    }
    public void send()
    {
        player.sendMessage("You have been invited to join " + guildId + " by " + playerId);
        player.sendMessage( accept.getText() + " | " + decline.getText());
        //this.timer.schedule();
    }
    public void accept()
    {

    }
    public void decline()
    {

    }

}
