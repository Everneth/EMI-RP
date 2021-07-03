package com.everneth.rp.events;

import com.everneth.rp.models.EMIPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import com.everneth.rp.RP;

public class LeaveEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        /* Right now all this event does is maintain our list of online
         * players as EMIPlayer objects. This will make player lookups faster
         * without querying the database as often.
         */
        Player player = event.getPlayer();
        EMIPlayer playerToRemove = new EMIPlayer();
        for (EMIPlayer ep : RP.getOnlinePlayers())
        {
            if(ep.getUniqueId().equals(player.getUniqueId().toString()))
                playerToRemove = ep;
        }
        RP.getOnlinePlayers().remove(playerToRemove);
    }
}
