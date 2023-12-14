package de.timolia.legacycombatsimulation.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.EnumSet;

public class SimulationTargetChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private EnumSet<SimulationTarget> simulationTargets;
    private final boolean gotEnabled;

    public SimulationTargetChangeEvent(@NotNull Player who, SimulationTarget simulationTarget, boolean gotEnabled) {
        super(who, !Bukkit.isPrimaryThread());
        this.gotEnabled = gotEnabled;
        this.simulationTargets = EnumSet.of(simulationTarget);
    }

    public SimulationTargetChangeEvent(@NotNull Player who, EnumSet<SimulationTarget> simulationTargets, boolean gotEnabled) {
        super(who, !Bukkit.isPrimaryThread());
        this.gotEnabled = gotEnabled;
        this.simulationTargets = simulationTargets;
    }

    public boolean doesTargetApply(SimulationTarget target) {
        return this.simulationTargets.contains(target);
    }

    public boolean gotEnabled() {
        return this.gotEnabled;
    }

    public boolean gotDisabled() {
        return !this.gotEnabled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
