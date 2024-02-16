package de.timolia.legacycombatsimulation;

import org.bukkit.configuration.ConfigurationSection;

public class Configuration {

    private boolean disableSprintInterruptionOnAttack;

    public Configuration(ConfigurationSection section) {
        disableSprintInterruptionOnAttack = section.getBoolean("disableSprintInterruptionOnAttack", false);
    }

    public boolean isDisableSprintInterruptionOnAttack() {
        return disableSprintInterruptionOnAttack;
    }
}
