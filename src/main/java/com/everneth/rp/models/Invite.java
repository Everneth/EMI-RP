package com.everneth.rp.models;

import com.everneth.rp.InviteManager;
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
    Player officer;
    private TextComponent accept;
    private TextComponent decline;

    public Invite(int guildId, int playerId, Player player, Player officer)
    {
        this.guildId = guildId;
        this.playerId = playerId;
        this.player = player;
        this.officer = officer;
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
        this.officer.sendMessage(ChatColor.GREEN + this.player.getName() + " has accepted your invite!");
        InviteManager.getInviteManager().removeInvite(this);
        timer.cancel();
    }
    public void decline()
    {
        this.officer.sendMessage(ChatColor.RED + this.player.getName() + " has declined your invite or it has timed out!");
        InviteManager.getInviteManager().removeInvite(this);
        timer.cancel();
    }

    public int getGuildId() {
        return guildId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getOfficer() {
        return officer;
    }
}
