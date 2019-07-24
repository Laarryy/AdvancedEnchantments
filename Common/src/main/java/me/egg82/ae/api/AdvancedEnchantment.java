package me.egg82.ae.api;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.egg82.ae.api.curses.*;
import me.egg82.ae.api.enchantments.*;
import me.egg82.ae.utils.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AdvancedEnchantment extends GenericEnchantment {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Set<AdvancedEnchantment> allEnchantments = new HashSet<>(); // Needs to be set BEFORE the enchants are defined, else NPE

    public static final AdvancedEnchantment AERIAL = new AerialEnchantment();
    public static final AdvancedEnchantment ANTIGRAVITY = new AntigravityEnchantment();
    public static final AdvancedEnchantment ARTISAN = new ArtisanEnchantment();
    public static final AdvancedEnchantment BEHEADING = new BeheadingEnchantment();
    public static final AdvancedEnchantment BLEEDING = new BleedingEnchantment();
    public static final AdvancedEnchantment BLINDING = new BlindingEnchantment();
    public static final AdvancedEnchantment BURST = new BurstEnchantment();
    public static final AdvancedEnchantment CHARGING = new ChargingEnchantment();
    public static final AdvancedEnchantment DISARMING = new DisarmingEnchantment();
    public static final AdvancedEnchantment ENSNARING = new EnsnaringEnchantment();
    public static final AdvancedEnchantment EXPLOSIVE = new ExplosiveEnchantment();
    public static final AdvancedEnchantment FIERY = new FieryEnchantment();
    public static final AdvancedEnchantment FREEZING = new FreezingEnchantment();
    public static final AdvancedEnchantment MAGNETIC = new MagneticEnchantment();
    public static final AdvancedEnchantment MARKING = new MarkingEnchantment();
    public static final AdvancedEnchantment MIRAGE = new MirageEnchantment();
    public static final AdvancedEnchantment MULTISHOT = new MultishotEnchantment();
    public static final AdvancedEnchantment POISONOUS = new PoisonousEnchantment();
    public static final AdvancedEnchantment PROFICIENCY = new ProficiencyEnchantment();
    public static final AdvancedEnchantment RAMPAGE = new RampageEnchantment();
    public static final AdvancedEnchantment REAPING = new ReapingEnchantment();
    public static final AdvancedEnchantment REPAIRING = new RepairingEnchantment();
    public static final AdvancedEnchantment SMELTING = new SmeltingEnchantment();
    public static final AdvancedEnchantment STILLNESS = new StillnessEnchantment();
    public static final AdvancedEnchantment THUNDEROUS = new ThunderousEnchantment();
    public static final AdvancedEnchantment TORNADO = new TornadoEnchantment();
    public static final AdvancedEnchantment VAMPIRIC = new VampiricEnchantment();
    public static final AdvancedEnchantment VORPAL = new VorpalEnchantment();

    public static final AdvancedEnchantment ADHERENCE_CURSE = new AdherenceCurse();
    public static final AdvancedEnchantment CALLING_CURSE = new CallingCurse();
    public static final AdvancedEnchantment DECAY_CURSE = new DecayCurse();
    public static final AdvancedEnchantment ENDER_CURSE = new EnderCurse();
    public static final AdvancedEnchantment LEECHING_CURSE = new LeechingCurse();
    public static final AdvancedEnchantment PACIFISM_CURSE = new PacifismCurse();
    public static final AdvancedEnchantment STICKINESS_CURSE = new StickinessCurse();
    public static final AdvancedEnchantment TREASON_CURSE = new TreasonCurse();
    public static final AdvancedEnchantment WITHER_CURSE = new WitherCurse();

    public static Set<AdvancedEnchantment> values() { return ImmutableSet.copyOf(allEnchantments); }

    public static Optional<AdvancedEnchantment> getByName(String name) {
        for (AdvancedEnchantment enchantment : allEnchantments) {
            if (enchantment.name.equalsIgnoreCase(name) || enchantment.friendlyName.equalsIgnoreCase(name)) {
                return Optional.of(enchantment);
            }
        }
        return Optional.empty();
    }

    public static Optional<AdvancedEnchantment> getByUuid(UUID uuid) {
        for (AdvancedEnchantment enchantment : allEnchantments) {
            if (enchantment.uuid.equals(uuid)) {
                return Optional.of(enchantment);
            }
        }
        return Optional.empty();
    }

    protected AdvancedEnchantment(UUID uuid, String name, String friendlyName, boolean isCurse, int minLevel, int maxLevel) {
        super(uuid, name, friendlyName, isCurse, minLevel, maxLevel, null);

        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Adding custom enchantment " + name);
        }

        allEnchantments.add(this);
    }
}
