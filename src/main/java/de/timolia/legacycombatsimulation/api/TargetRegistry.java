package de.timolia.legacycombatsimulation.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;

import java.util.*;

public class TargetRegistry {
    private static final TargetRegistry instance = new TargetRegistry();
    private static EnumSet<SimulationTarget> clientIndependentTargets = EnumSet.noneOf(SimulationTarget.class);
    static {
        for (SimulationTarget value : SimulationTarget.values()) {
            if (!value.isOnlyForLegacyClients())
                clientIndependentTargets.add(value);
        }
    }

    public static TargetRegistry instance() {
        return instance;
    }

    private final Multimap<Player, SimulationTarget> registry = HashMultimap.create();

    public boolean isEnabled(Player player, SimulationTarget target) {
        return registry.containsEntry(player, target);
    }

    public void enable(Player player, Collection<SimulationTarget> targets) {
        if (registry.putAll(player, targets)) {
            new SimulationTargetChangeEvent(player, EnumSet.copyOf(targets), true).callEvent();
        }
    }

    public void enableAll(Player player, boolean isLegacyClient) {
        EnumSet<SimulationTarget> values = isLegacyClient ? EnumSet.allOf(SimulationTarget.class) : clientIndependentTargets;
        if (registry.putAll(player, EnumSet.allOf(SimulationTarget.class))) {
            new SimulationTargetChangeEvent(player, values, true).callEvent();
        }
    }

    @Deprecated
    public void enable(Player player, Iterable<SimulationTarget> targets) {
        enable(player, (Collection<SimulationTarget>) targets);
    }

    public void disable(Player player, SimulationTarget target) {
        if (registry.remove(player, target)) {
            new SimulationTargetChangeEvent(player, target, false).callEvent();
        }
    }

    public void disableAll(Player player) {
        Collection<SimulationTarget> removed = registry.removeAll(player);
        if (!removed.isEmpty()) {
            new SimulationTargetChangeEvent(player, EnumSet.copyOf(removed), false).callEvent();
        }
    }

}
