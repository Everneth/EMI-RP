package com.everneth.rp.models;

import com.everneth.rp.InviteManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Timer;
import java.util.TimerTask;

public class Invite {
    private Guild guild;
    private int playerId;
    private Timer timer;
    private Player player;
    private Player officer;
    private TextComponent accept;
    private TextComponent decline;
    private TimerTask onTimeout;

    public Invite(Guild guild, int playerId, Player player, Player officer)
    {
        this.guild = guild;
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
        TimerTask onTimeout = new TimerTask() {
            @Override
            public void run() {
                Invite playerInvite = InviteManager.getInviteManager().findInvite(player);
                playerInvite.decline();
            }
        };
    }
    public void send()
    {
        player.sendMessage("You have been invited to join <" + this.guild.getName() + "> by " + this.player.getName());
        player.sendMessage( accept.getText() + " | " + decline.getText());
        this.timer.schedule(this.onTimeout, 600000L);
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

    public Guild getGuild() {
        return guild;
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
