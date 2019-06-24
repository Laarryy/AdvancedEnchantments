package me.egg82.ae.extended;

public class CachedConfigValues {
    private CachedConfigValues() {}

    private boolean debug = false;
    public boolean getDebug() { return debug; }

    private double enchantChance = 0.0d;
    public double getEnchantChance() { return enchantChance; }

    public static CachedConfigValues.Builder builder() { return new CachedConfigValues.Builder(); }

    public static class Builder {
        private final CachedConfigValues values = new CachedConfigValues();

        private Builder() {}

        public CachedConfigValues.Builder debug(boolean value) {
            values.debug = value;
            return this;
        }

        public CachedConfigValues.Builder enchantChance(double value) {
            values.enchantChance = value;
            return this;
        }

        public CachedConfigValues build() { return values; }
    }
}
