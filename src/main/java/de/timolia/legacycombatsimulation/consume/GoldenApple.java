package de.timolia.legacycombatsimulation.consume;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GoldenApple implements Listener {
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!TargetRegistry.instance().isEnabled(player, SimulationTarget.GOLDEN_APPLE)) {
            return;
        }
        Material type = event.getItem().getType();
        boolean enchanted = type == Material.ENCHANTED_GOLDEN_APPLE;
        if (!enchanted && type != Material.GOLDEN_APPLE) {
            return;
        }
        consumeOne(player.getInventory().getItem(event.getHand()));
        event.setCancelled(true);
        ((CraftPlayer) player).getHandle().getFoodData().eat(4, 1.2F);

        //if (!world.isClientSide) {
            //entityhuman.addEffect(new MobEffect(MobEffectList.ABSORBTION.id, 2400, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
        //}

        if (enchanted/* itemstack.getData() > 0 */) {
            //if (!world.isClientSide) {
                //entityhuman.addEffect(new MobEffect(MobEffectList.REGENERATION.id, 600, 4));
                //entityhuman.addEffect(new MobEffect(MobEffectList.RESISTANCE.id, 6000, 0));
                //entityhuman.addEffect(new MobEffect(MobEffectList.FIRE_RESISTANCE.id, 6000, 0));

                player.addPotionEffects(Arrays.asList(
                    new PotionEffect(PotionEffectType.REGENERATION, 600, 4),
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 0),
                    new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0)
                ));
            //}
        } else {
            //super.c(itemstack, world, entityhuman);
            //if (!world.isClientSide && this.l > 0 && world.random.nextFloat() < this.o) {
                //entityhuman.addEffect(new MobEffect(this.l, this.m * 20, this.n));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 1));
            //}
        }
    }

    private void consumeOne(ItemStack mirrored) {
        mirrored.setAmount(mirrored.getAmount() - 1);
        if (mirrored.isEmpty()) {
            mirrored.setType(Material.AIR);
        }
    }
}
