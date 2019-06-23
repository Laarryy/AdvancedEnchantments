package me.egg82.ae.extended;

public class CachedConfigValues {
    private CachedConfigValues() {}

    private boolean debug = false;
    public boolean getDebug() { return debug; }

    private boolean addEnchants = false;
    public boolean getAddEnchants() { return addEnchants; }

    public static CachedConfigValues.Builder builder() { return new CachedConfigValues.Builder(); }

    public static class Builder {
        private final CachedConfigValues values = new CachedConfigValues();

        private Builder() {}

        public CachedConfigValues.Builder debug(boolean value) {
            values.debug = value;
            return this;
        }

        public CachedConfigValues.Builder addEnchants(boolean value) {
            values.addEnchants = value;
            return this;
        }

        public CachedConfigValues build() { return values; }
    }
}
