package me.egg82.ae.api.curses;

import java.util.Arrays;
import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class MisfortuneCurse extends AdvancedEnchantment {
    public MisfortuneCurse() {
        super(UUID.randomUUID(), "misfortune_curse", "Curse of Misfortune", true, 1, 5);
        targets.add(AdvancedEnchantmentTarget.TOOL);
        conflicts.addAll(Arrays.asList(AdvancedEnchantment.STILLNESS, AdvancedEnchantment.SMELTING));
    }
}
