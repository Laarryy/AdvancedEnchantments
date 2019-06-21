package me.egg82.ae.events.enchants.player.playerMove;

import java.util.function.Consumer;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveFreezingCancel implements Consumer<PlayerMoveEvent> {
    public PlayerMoveFreezingCancel() { }

    public void accept(PlayerMoveEvent event) {
        event.setCancelled(true);
    }
}
