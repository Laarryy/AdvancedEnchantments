package me.egg82.ae.events.enchants.block.blockBreak;

import java.util.function.Consumer;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakFreezingCancel implements Consumer<BlockBreakEvent> {
    public BlockBreakFreezingCancel() { }

    public void accept(BlockBreakEvent event) {
        event.setCancelled(true);
    }
}
