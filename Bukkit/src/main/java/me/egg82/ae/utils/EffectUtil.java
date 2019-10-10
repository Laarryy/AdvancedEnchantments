package me.egg82.ae.utils;

import de.slikey.effectlib.Effect;
import org.bukkit.entity.Entity;

public class EffectUtil {
    public static void start(Effect effect, Entity entity) {
        effect.setEntity(entity);
        effect.disappearWithOriginEntity = true;
        effect.start();
    }
}
