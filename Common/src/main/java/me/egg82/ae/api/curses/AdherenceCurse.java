package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class AdherenceCurse extends AdvancedEnchantment {
    public AdherenceCurse() {
        super(UUID.randomUUID(), "adherence_curse", "Curse of Adherence", true, 1, 1);
        targets.add(AdvancedEnchantmentTarget.ALL);
    }
}
