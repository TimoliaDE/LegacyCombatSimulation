package de.timolia.legacycombatsimulation.api;

public enum SimulationTarget {
    ATTACK,
    ENDER_PEARL,
    OFF_HAND,
    BOW,
    GOLDEN_APPLE,
    FISHING_ROD,
    BLOCKING_SWORDS,
    CREATIVE_GIVE_ITEMS,
    SWIMMING_PREVENTION,
    INSTANT_IGNITE,
    HUNGER_AND_REGENERATION,
    ITEM_DAMAGE_VALUES,
    OLD_TNT,

    LEGACY_CLIENT_SOUNDS(true),
    ;

    private boolean onlyForLegacyClients = false;

    SimulationTarget() {

    }

    SimulationTarget(boolean onlyForLegacyClients) {
        this.onlyForLegacyClients = onlyForLegacyClients;
    }

    public boolean isOnlyForLegacyClients() {
        return onlyForLegacyClients;
    }
}
