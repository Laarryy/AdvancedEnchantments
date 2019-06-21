package me.egg82.ae.utils;

import org.bukkit.block.BlockFace;

public class LocationUtil {
    private LocationUtil() { }

    public static BlockFace getFacingDirection(double yaw, boolean cardinal) {
        yaw += 180.0d;

        while (yaw < 0.0d) {
            yaw += 360.0d;
        }
        while (yaw > 360.0d) {
            yaw -= 360.0d;
        }

        if (cardinal) {
            if (yaw >= 315.0d || yaw < 45.0d) {
                return BlockFace.NORTH;
            } else if (yaw >= 45.0d && yaw < 135.0d) {
                return BlockFace.EAST;
            } else if (yaw >= 135.0d && yaw < 225.0d) {
                return BlockFace.SOUTH;
            }

            return BlockFace.WEST;
        }

        if (yaw >= 348.75d || yaw < 11.25d) {
            return BlockFace.NORTH;
        } else if (yaw >= 11.25d && yaw < 33.75d) {
            return BlockFace.NORTH_NORTH_EAST;
        } else if (yaw >= 33.75d && yaw < 56.25d) {
            return BlockFace.NORTH_EAST;
        } else if (yaw >= 56.25d && yaw < 78.75d) {
            return BlockFace.EAST_NORTH_EAST;
        } else if (yaw >= 78.75d && yaw < 101.25d) {
            return BlockFace.EAST;
        } else if (yaw >= 101.25d && yaw < 123.75d) {
            return BlockFace.EAST_SOUTH_EAST;
        } else if (yaw >= 123.75d && yaw < 146.25d) {
            return BlockFace.SOUTH_EAST;
        } else if (yaw >= 146.25d && yaw < 168.75d) {
            return BlockFace.SOUTH_SOUTH_EAST;
        } else if (yaw >= 168.75d && yaw < 191.25d) {
            return BlockFace.SOUTH;
        } else if (yaw >= 191.25d && yaw < 213.75d) {
            return BlockFace.SOUTH_SOUTH_WEST;
        } else if (yaw >= 213.75d && yaw < 236.25d) {
            return BlockFace.SOUTH_WEST;
        } else if (yaw >= 236.25d && yaw < 258.75d) {
            return BlockFace.WEST_SOUTH_WEST;
        } else if (yaw >= 258.75d && yaw < 281.25d) {
            return BlockFace.WEST;
        } else if (yaw >= 281.25d && yaw < 303.75d) {
            return BlockFace.WEST_NORTH_WEST;
        } else if (yaw >= 303.75d && yaw < 326.25d) {
            return BlockFace.NORTH_WEST;
        }

        return BlockFace.NORTH_NORTH_WEST;
    }
}
