package de.timolia.legacycombatsimulation.environement;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import de.timolia.legacycombatsimulation.attack.nms.Damage;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class TnT implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onTnTDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
            return;
        if (!(event.getEntity() instanceof Player player))
            return;

        for (EntityDamageEvent.DamageModifier damageModifier : EntityDamageEvent.DamageModifier.values()) {
            System.out.println(damageModifier + ": " + event.getDamage(damageModifier));
        }

        double oldArmorReduction = event.getDamage(EntityDamageEvent.DamageModifier.ARMOR);
        double base = event.getDamage(EntityDamageEvent.DamageModifier.BASE);
        double armor = Damage.getDamageAfterArmorAbsorb(
                ((CraftPlayer) player).getHandle(),
                oldArmorReduction == 0,
                (float) base,
                SimulationTarget.OLD_TNT);
        armor = -(base - armor);
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, armor);
    }
}
