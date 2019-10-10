package me.egg82.ae.effects;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ParticleSplashEffect extends Effect {
    public Particle particle;
    public int minCount = 3;
    public int maxCount = 5;

    public float maxHeight = 0.3f;
    public float minHeight = 0.1f;

    public double maxWidth = 0.8d;
    public double minWidth = 0.2d;

    public ParticleSplashEffect(EffectManager effectManager, Particle particle) {
        super(effectManager);
        this.particle = particle;
        type = EffectType.INSTANT;
        period = 1;
        iterations = 1;
    }

    @Override
    public void onRun() {
        Location location = getLocation();

        int count = RandomUtils.random.nextInt(maxCount - minCount) + minCount;
        for (int i = 0; i < count; i++) {
            Vector v = RandomUtils.getRandomCircleVector().multiply(RandomUtils.random.nextDouble() * (maxWidth - minWidth) + minWidth);
            v.setY(RandomUtils.random.nextFloat() * (maxHeight - minHeight) + minHeight);
            location.add(v);
            display(particle, location, color);
            location.subtract(v);
        }
    }
}
