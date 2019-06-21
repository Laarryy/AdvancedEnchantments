package me.egg82.ae.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class BlockUtil {
    private BlockUtil() { }

    public static List<Block> getBlocks(Location center, int xRadius, int yRadius, int zRadius) {
        if (center == null) {
            throw new IllegalArgumentException("center cannot be null.");
        }

        int minX = center.getBlockX() - xRadius;
        int maxX = center.getBlockX() + xRadius;
        int minY = center.getBlockY() - yRadius;
        int maxY = center.getBlockY() + yRadius;
        int minZ = center.getBlockZ() - zRadius;
        int maxZ = center.getBlockZ() + zRadius;

        Location currentLocation = new Location(center.getWorld(), 0.0d, 0.0d, 0.0d);
        List<Block> blocks = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            currentLocation.setX(x);
            for (int z = minZ; z <= maxZ; z++) {
                currentLocation.setZ(z);
                for (int y = minY; y <= maxY; y++) {
                    currentLocation.setY(y);
                    blocks.add(currentLocation.getBlock());
                }
            }
        }

        return blocks;
    }
}
