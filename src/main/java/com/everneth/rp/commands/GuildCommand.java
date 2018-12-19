package com.everneth.rp.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("guild")
public class GuildCommand extends BaseCommand {
    @Subcommand("guild create")
    @CommandPermission("emi.rp.guild")
    public void onGuildPlayerCreate(CommandSender sender, String name, Player p2, Player p3)
    {
        if(!p2.isOnline() || !p3.isOnline())
        {
            sender.sendMessage("One or more players offline! If you need assistance, please contact a GM.");
        }
        else
        {
            createGuild(name, p2, p3);
        }
    }

    protected void createGuild(String name, Player p1, Player p2, Player p3 = null)
    {

    }

}
