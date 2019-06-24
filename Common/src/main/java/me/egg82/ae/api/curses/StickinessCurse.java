package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class StickinessCurse extends AdvancedEnchantment {
    public StickinessCurse() {
        super(UUID.randomUUID(), "stickiness_curse", "Curse of Stickiness", true, 1, 3);
        targets.add(AdvancedEnchantmentTarget.ALL);
    }
}
