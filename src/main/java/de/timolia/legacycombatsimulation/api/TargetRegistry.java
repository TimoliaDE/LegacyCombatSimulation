package de.timolia.legacycombatsimulation.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;

public class TargetRegistry {
    private static final TargetRegistry instance = new TargetRegistry();

    public static TargetRegistry instance() {
        return instance;
    }

    private final Multimap<Player, SimulationTarget> registry = HashMultimap.create();

    public boolean isEnabled(Player player, SimulationTarget target) {
        return registry.containsEntry(player, target);
    }

    public void enable(Player player, Iterable<SimulationTarget> targets) {
        registry.putAll(player, targets);
    }

    public void disable(Player player, SimulationTarget target) {
        registry.remove(player, target);
    }

    public void disableAll(Player player) {
        registry.removeAll(player);
    }
}
