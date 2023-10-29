package de.timolia.legacycombatsimulation.inventory;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;

public class CreativeGiveItems implements Listener {
    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (
            event.getWhoClicked() instanceof Player player
            && Bukkit.getUnsafe().toLegacy(event.getCursor().getType()) == null
            && TargetRegistry.instance().isEnabled(player, SimulationTarget.CREATIVE_GIVE_ITEMS)
        ) {
            event.setCancelled(true);
        }
    }
}
