package com.everneth.rp.events;

import com.everneth.rp.models.EMIPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.everneth.rp.RP;

public class JoinEvent implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        /* Right now all this event does is maintain our list of online
         * players as EMIPlayer objects. This will make player lookups faster
         * without querying the database as often.
         */
        Player player = event.getPlayer();
        EMIPlayer playerToAdd = EMIPlayer.getEmiPlayer(player.getUniqueId());

        RP.getOnlinePlayers().add(playerToAdd);
    }
}
