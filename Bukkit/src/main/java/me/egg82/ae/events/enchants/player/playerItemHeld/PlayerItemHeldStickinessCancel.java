package me.egg82.ae.events.enchants.player.playerItemHeld;

import java.util.function.Consumer;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class PlayerItemHeldStickinessCancel implements Consumer<PlayerItemHeldEvent> {
    public PlayerItemHeldStickinessCancel() { }

    public void accept(PlayerItemHeldEvent event) {
        event.setCancelled(true);
    }
}
