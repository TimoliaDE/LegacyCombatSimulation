package de.timolia.legacycombatsimulation.inventory;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class OffHand implements Listener {
    private static final int OFFHAND_SLOT = 40;

    private boolean isDisabled(Entity entity) {
        if (!(entity instanceof Player player)) {
            return true;
        }
        return !TargetRegistry.instance().isEnabled(player, SimulationTarget.OFF_HAND);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event){
        if (isDisabled(event.getPlayer())) {
            return;
        }
        if (isNotEmpty(event.getOffHandItem())){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event){
        if (isDisabled(event.getWhoClicked())) {
            return;
        }
        if (event.getClick() == ClickType.SWAP_OFFHAND){
            event.setResult(Event.Result.DENY);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event){
        if (
            isDisabled(event.getWhoClicked())
            || event.getInventory().getType() != InventoryType.CRAFTING
            || !event.getInventorySlots().contains(OFFHAND_SLOT)) {
            return;
        }
        if (isNotEmpty(event.getOldCursor())){
            event.setResult(Event.Result.DENY);
            event.setCancelled(true);
        }
    }

    private boolean isNotEmpty(ItemStack item){
        return item != null && item.getType() != Material.AIR;
    }
}
