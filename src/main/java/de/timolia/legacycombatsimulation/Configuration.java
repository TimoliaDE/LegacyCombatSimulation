package de.timolia.legacycombatsimulation;

import org.bukkit.configuration.ConfigurationSection;

public class Configuration {

    private boolean disableSprintInterruptionOnAttack;
    private double verticalKnockbackMultiplier;
    private double horizontalKnockbackMultiplier;

    public Configuration(ConfigurationSection section) {
        disableSprintInterruptionOnAttack = section.getBoolean("disableSprintInterruptionOnAttack", false);
        verticalKnockbackMultiplier = section.getDouble("verticalKnockbackMultiplier", 1);
        horizontalKnockbackMultiplier = section.getDouble("horizontalKnockbackMultiplier", 1);
    }

    public boolean isDisableSprintInterruptionOnAttack() {
        return disableSprintInterruptionOnAttack;
    }

    public double getVerticalKnockbackMultiplier() {
        return verticalKnockbackMultiplier;
    }

    public void setVerticalKnockbackMultiplier(double verticalKnockbackMultiplier) {
        this.verticalKnockbackMultiplier = verticalKnockbackMultiplier;
    }

    public double getHorizontalKnockbackMultiplier() {
        return horizontalKnockbackMultiplier;
    }

    public void setHorizontalKnockbackMultiplier(double horizontalKnockbackMultiplier) {
        this.horizontalKnockbackMultiplier = horizontalKnockbackMultiplier;
    }
}
