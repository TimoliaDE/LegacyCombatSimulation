package de.timolia.legacycombatsimulation.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class SimulationTargetChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public SimulationTargetChangeEvent(@NotNull Player who) {
        super(who, !Bukkit.isPrimaryThread());
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
