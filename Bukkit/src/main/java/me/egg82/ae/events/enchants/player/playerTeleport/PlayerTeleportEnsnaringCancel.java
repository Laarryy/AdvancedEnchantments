package me.egg82.ae.events.enchants.player.playerTeleport;

import java.util.function.Consumer;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportEnsnaringCancel implements Consumer<PlayerTeleportEvent> {
    public PlayerTeleportEnsnaringCancel() { }

    public void accept(PlayerTeleportEvent event) {
        event.setCancelled(true);
    }
}
