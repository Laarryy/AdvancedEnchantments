package me.egg82.ae.tasks;

import de.slikey.effectlib.EffectManager;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import me.egg82.ae.effects.ParticleSplashEffect;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityDamageHandler;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.EffectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class TaskBleeding implements Runnable {
    private final EffectManager effectManager;

    public TaskBleeding(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void run() {
        for (Iterator<Map.Entry<UUID, Double>> i = CollectionProvider.getBleeding().entrySet().iterator(); i.hasNext();) {
            Map.Entry<UUID, Double> kvp = i.next();
            Entity e = Bukkit.getEntity(kvp.getKey());
            if (e == null || e.isDead() || !(e instanceof Damageable)) {
                i.remove();
                continue;
            }

            if (ConfigUtil.getParticlesOrFalse()) {
                ParticleSplashEffect effect = new ParticleSplashEffect(effectManager, Particle.BLOCK_DUST);
                effect.material = Material.REDSTONE_BLOCK;
                effect.minCount = 5;
                effect.maxCount = 8;
                EffectUtil.start(effect, e);
            }

            EntityDamageHandler.damage((Damageable) e, kvp.getValue(), EntityDamageEvent.DamageCause.WITHER);
        }
    }
}
