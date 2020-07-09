package com.everneth.rp.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;

@CommandAlias("faction")
public class FactionCommand extends BaseCommand {
    @Subcommand("invite")
    @CommandPermission("faction.officer.invite")
    public void onInvite(CommandSender sender, String name)
    {

    }
    @Subcommand("promote")
    @CommandPermission("faction.officer.promote")
    public void onPromote(CommandSender sender, String name)
    {

    }
    @Subcommand("demote")
    @CommandPermission("faction.officer.demote")
    public void onDemote(CommandSender sender, String name)
    {

    }
    @Subcommand("remove")
    @CommandPermission("faction.officer.remove")
    public void onRemove(CommandSender sender, String name)
    {

    }
    @Subcommand("add enemy")
    @CommandPermission("faction.officer.add.enemy")
    public void onEnemyAdd(CommandSender sender, String factionName)
    {

    }
    @Subcommand("add ally")
    @CommandPermission("faction.officer.add.ally")
    public void onAllyAdd(CommandSender sender, String factionName)
    {

    }
    @Subcommand("rank")
    @CommandPermission("faction.leader.rank")
    public void onRank(CommandSender sender, int id, String name)
    {

    }
}
