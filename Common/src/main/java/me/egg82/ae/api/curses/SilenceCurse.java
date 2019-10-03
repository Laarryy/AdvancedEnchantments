package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class SilenceCurse extends AdvancedEnchantment {
    public SilenceCurse() {
        super(UUID.randomUUID(), "silence_curse", "Curse of Silence", true, 1, 1);
        targets.add(AdvancedEnchantmentTarget.ARMOR_HEAD);
    }
}
