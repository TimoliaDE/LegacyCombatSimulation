package de.timolia.legacycombatsimulation.environement;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FireBlock implements Listener {

    @EventHandler
    public void onFireStep(EntityInsideBlockEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (event.getBlock().getType() != Material.FIRE)
            return;
        if (!TargetRegistry.instance().isEnabled(player, SimulationTarget.INSTANT_IGNITE))
            return;
        // getFireImmuneTicks on player returns 20 --> player has to stand 20 ticks in the fire before igniting.
        // This sets that value effectively to 1 tick
        player.setFireTicks(Math.max(player.getFireTicks(), -2));
    }
}
