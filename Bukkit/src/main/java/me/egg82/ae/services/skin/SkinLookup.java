package me.egg82.ae.services.skin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.mineskin.data.Skin;

public class SkinLookup {
    private SkinLookup() { }

    private static boolean isPaper = true;

    static {
        try {
            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
        } catch (ClassNotFoundException ignored) {
            isPaper = false;
        }
    }

    public static SkinInfo get(UUID uuid, File cacheDir) throws IOException {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null.");
        }

        return (isPaper) ? new PaperSkinInfo(uuid, cacheDir) : new BukkitSkinInfo(uuid, cacheDir);
    }

    public static SkinInfo get(UUID uuid, String name, Skin skin) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null.");
        }

        return (isPaper) ? new PaperSkinInfo(uuid, name, skin) : new BukkitSkinInfo(uuid, name, skin);
    }
}
