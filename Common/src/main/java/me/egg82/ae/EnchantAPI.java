package me.egg82.ae;

import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.api.GenericEnchantment;
import me.egg82.ae.services.InternalAPI;

public class EnchantAPI {
    private static final EnchantAPI api = new EnchantAPI();
    private final InternalAPI internalApi = new InternalAPI();

    private EnchantAPI() { }

    public static EnchantAPI getInstance() { return api; }

    public int getMaxLevel(GenericEnchantment enchantment, GenericEnchantableItem... items) throws APIException {
        if (enchantment == null) {
            throw new APIException(false, "enchantment cannot be null.");
        }

        int max = -1;
        for (GenericEnchantableItem item : items) {
            if (item == null) {
                continue;
            }

            max = Math.max(max, item.getEnchantmentLevel(enchantment));
        }
        return max;
    }
}
