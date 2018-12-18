package com.everneth.rp.events;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.everneth.rp.RP;

import java.util.concurrent.CompletableFuture;

public class JoinEvent implements Listener {

    private CompletableFuture<DbRow> playerOjbectFuture;
    private DbRow playerRow;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        playerOjbectFuture = DB.getFirstRowAsync("SELECT * FROM players WHERE player_uuid = ?", player.getUniqueId().toString());
        try {
            //Try to get the row from the future and put it into a DbRow object
            playerRow = playerOjbectFuture.get();
        }
        catch (Exception e)
        {
            //Something went wrong, record the error. This should never happen.
            RP.getPlugin().getLogger().warning(e.getMessage());
        }
    }
}
