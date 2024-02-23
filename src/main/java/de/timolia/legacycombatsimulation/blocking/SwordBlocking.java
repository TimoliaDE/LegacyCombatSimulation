package de.timolia.legacycombatsimulation.blocking;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.SwordItem;
import org.bukkit.Tag;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SwordBlocking implements Listener {

    public SwordBlocking() {
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || event.getHand() != EquipmentSlot.HAND || event.getItem() == null)
            return;

        if (!Tag.ITEMS_SWORDS.isTagged(event.getItem().getType()))
            return;

        if (!TargetRegistry.instance().isEnabled(event.getPlayer(), SimulationTarget.BLOCKING_SWORDS))
            return;

        ServerPlayer serverPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        serverPlayer.startUsingItem(InteractionHand.MAIN_HAND);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (!TargetRegistry.instance().isEnabled(player, SimulationTarget.BLOCKING_SWORDS))
            return;
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!Tag.ITEMS_SWORDS.isTagged(inHand.getType()))
            return;
        if (player.getActiveItem().isEmpty())
            return;
        double baseDamage = event.getDamage(EntityDamageEvent.DamageModifier.BASE);
        // We cannot use DamageModifier.BLOCKING, because Paper assumes this to be a shield
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, baseDamage / 2);
    }
}
