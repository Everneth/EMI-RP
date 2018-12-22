package com.everneth.rp;

import com.everneth.rp.models.Invite;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class InviteManager {
    private static final InviteManager inviteManager = new InviteManager();
    private HashMap<UUID, Invite> inviteMap;
    private InviteManager()
    {
        inviteMap = new HashMap<UUID, Invite>();
    }
    public static InviteManager getInviteManager()
    {
        return inviteManager;
    }

    public void addInvite(Player player, Invite guildInvite)
    {
        inviteMap.put(player.getUniqueId(), guildInvite);
    }
    public void removeInvite(Player player, Invite guildInvite)
    {
        inviteMap.remove(player.getUniqueId(), guildInvite);
    }
    public Invite findInvite(Player player)
    {
        return inviteMap.get(player.getUniqueId());
    }
}
