package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class PacifismCurse extends AdvancedEnchantment {
    public PacifismCurse() {
        super(UUID.randomUUID(), "pacifism_curse", "Curse of Pacifism", true, 1, 1);
        targets.add(AdvancedEnchantmentTarget.ALL);
    }
}
