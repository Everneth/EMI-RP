package com.everneth.rp.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;

@CommandAlias("roleplay|rp")
public class RoleplayCommand {

    @CommandPermission("emi.rp.gm")
    @Subcommand("season create")
    public void onRpSeasonCreate(CommandSender sender)
    {

    }
    @CommandPermission("emi.rp.gm")
    @Subcommand("season remove")
    public void onRpSeasonRemove(CommandSender sender)
    {

    }
    @CommandPermission("emi.rp.gm")
    @Subcommand("season edit")
    public void onRpSeasonEdit(CommandSender sender)
    {

    }
    @CommandPermission("emi.rp.gm")
    @Subcommand("season start")
    public void onRpSeasonStart(CommandSender sender)
    {

    }
    @CommandPermission("emi.rp.gm")
    @Subcommand("season end")
    public void onRpSeasonEnd(CommandSender sender)
    {

    }

    @Subcommand("season list")
    public void onRpSeasonList(CommandSender sender)
    {

    }

    @Subcommand("season join")
    public void onRpSeasonJoin(CommandSender sender)
    {

    }
}
