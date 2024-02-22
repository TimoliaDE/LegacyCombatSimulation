package de.timolia.legacycombatsimulation.projectile.arrow;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class Bow implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShoot(EntityShootBowEvent event) {
        if (event.isCancelled())
            return;
        if (
            !(event.getEntity() instanceof Player player)
            || !TargetRegistry.instance().isEnabled(player, SimulationTarget.BOW)
        ) {
            return;
        }
        event.setCancelled(true);
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ItemStack bow = ((CraftItemStack) event.getBow()).handle;
        ItemStack arrowItem = ((CraftItemStack) event.getConsumable()).handle;
        Arrow arrow = shoot(serverPlayer, bow, event.getForce());
        boolean flag1 = !event.shouldConsumeItem();
        postShootHandle(serverPlayer, arrow, bow, flag1, arrowItem, event.getForce());
    }

    private Arrow shoot(ServerPlayer player, ItemStack stack, float f) {
        /*int j = this.d(itemstack) - i;
        float f = (float) j / 20.0F;

        f = (f * f + f * 2.0F) / 3.0F;
        if ((double) f < 0.1D) {
            return;
        }

        if (f > 1.0F) {
            f = 1.0F;
        }*/

        Arrow entityarrow = new BowEntity(player.level(), player, f * 2.0F);

        if (f == 1.0F) {
            //entityarrow.setCritical(true);
            entityarrow.setCritArrow(true);
        }

        //int k = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_DAMAGE.id, itemstack);
        int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);

        if (k > 0) {
            //entityarrow.b(entityarrow.j() + (double) k * 0.5D + 0.5D);
            entityarrow.setBaseDamage(entityarrow.getBaseDamage() + (double) k * 0.5D + 0.5D);
        }

        //int l = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK.id, itemstack);
        int l = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);

        if (l > 0) {
            //entityarrow.setKnockbackStrength(l);
            entityarrow.setKnockback(l);
        }

        /*if (EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_FIRE.id, itemstack) > 0) {
            // CraftBukkit start - call EntityCombustEvent
            EntityCombustEvent event = new EntityCombustEvent(entityarrow.getBukkitEntity(), 100);
            entityarrow.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                entityarrow.setOnFire(event.getDuration());
            }
            // CraftBukkit end
        }*/
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            entityarrow.setSecondsOnFire(100);
        }
        return entityarrow;
    }

    private void postShootHandle(
        ServerPlayer entityhuman,
        Arrow entityarrow,
        ItemStack stack,
        boolean flag1,
        ItemStack itemstack1,
        float f
    ) {
        Level world = entityhuman.serverLevel();
        stack.hurtAndBreak(1, entityhuman, (entityhuman1) -> {
            entityhuman1.broadcastBreakEvent(entityhuman.getUsedItemHand());
        });
        if (flag1 || entityhuman.getAbilities().instabuild && (itemstack1.is(Items.SPECTRAL_ARROW) || itemstack1.is(Items.TIPPED_ARROW))) {
            entityarrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }

        // CraftBukkit start
        //if (event.getProjectile() == entityarrow.getBukkitEntity()) {
            if (!world.addFreshEntity(entityarrow)) {
                if (entityhuman instanceof net.minecraft.server.level.ServerPlayer) {
                    ((net.minecraft.server.level.ServerPlayer) entityhuman).getBukkitEntity().updateInventory();
                }
                return;
            }
        //}
        world.playSound((net.minecraft.world.entity.player.Player) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
        if (!flag1 && !entityhuman.getAbilities().instabuild) {
            itemstack1.shrink(1);
            if (itemstack1.isEmpty()) {
                entityhuman.getInventory().removeItem(itemstack1);
            }
        }
    }
}
