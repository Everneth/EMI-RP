package com.everneth.rp.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import com.everneth.rp.RP;
import com.everneth.rp.models.ActionResponse;
import com.everneth.rp.models.RPSeason;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("roleplay|rp")
public class RoleplayCommand extends BaseCommand {

    @CommandPermission("emi.rp.gm")
    @Subcommand("season create")
    public void onRpSeasonCreate(CommandSender sender, String name, int typeId, int themeId, @Optional boolean allowGuildsBool)
    {
        int allowGuilds = allowGuildsBool ? 1 : 0;
        RPSeason season = new RPSeason(name, typeId, themeId, allowGuilds);
        ActionResponse response = season.createSeason();
        sender.sendMessage(response.getMessage());
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
    public void onRpSeasonStart(CommandSender sender, String name)
    {
        RPSeason season = RPSeason.getSeason(name);
        ActionResponse response = season.startSeason();
        if(response.isSuccessfulAction())
            Bukkit.broadcastMessage(response.getMessage());
        else
            sender.sendMessage(response.getMessage());
    }
    @CommandPermission("emi.rp.gm")
    @Subcommand("season end")
    public void onRpSeasonEnd(CommandSender sender)
    {

    }

    @CommandPermission("emi.rp.gm")
    @Subcommand("season pause")
    public void onRpSeasonPause(CommandSender sender)
    {

    }

    @Subcommand("season list")
    public void onRpSeasonList(CommandSender sender)
    {

    }

    @Subcommand("season kick")
    public void onRpSeasonKick(CommandSender sender)
    {

    }

    @Subcommand("season join")
    public void onRpSeasonJoin(CommandSender sender)
    {

    }
}
