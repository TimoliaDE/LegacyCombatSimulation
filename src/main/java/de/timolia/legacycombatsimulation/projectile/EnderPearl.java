package de.timolia.legacycombatsimulation.projectile;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import io.papermc.paper.event.player.PlayerItemCooldownEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EnderPearl implements Listener {
    @EventHandler
    public void onLaunch(PlayerItemCooldownEvent event) {
        if (
            TargetRegistry.instance().isEnabled(event.getPlayer(), SimulationTarget.ENDER_PEARL)
            && event.getType() == Material.ENDER_PEARL
        ) {
            event.setCancelled(true);
        }
    }
}
