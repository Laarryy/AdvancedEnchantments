package me.egg82.ae.events.enchants.block.blockPlace;

import java.util.function.Consumer;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceFreezingCancel implements Consumer<BlockPlaceEvent> {
    public BlockPlaceFreezingCancel() { }

    public void accept(BlockPlaceEvent event) {
        event.setCancelled(true);
    }
}
