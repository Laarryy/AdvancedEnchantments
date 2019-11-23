package me.egg82.ae.api.curses;

import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

import java.util.UUID;

public class NightCurse extends AdvancedEnchantment {
    public NightCurse() {
        super(UUID.randomUUID(), "night_curse", "Curse of Night", true, 1, 1);
        targets.add(AdvancedEnchantmentTarget.ARMOR_HEAD);
    }
}
