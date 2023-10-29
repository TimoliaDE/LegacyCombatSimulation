package de.timolia.legacycombatsimulation.projectile.rod;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class FishingRod implements Listener {
    @EventHandler
    public void onListener(PlayerFishEvent event) {
        if (
            event.getState() != State.FISHING
            || !TargetRegistry.instance().isEnabled(event.getPlayer(), SimulationTarget.FISHING_ROD)
        ) {
            return;
        }
        ServerPlayer user = ((CraftPlayer) event.getPlayer()).getHandle();
        Level world = user.level();
        ItemStack itemStack = user.getItemInHand(CraftEquipmentSlot.getHand(event.getHand()));
        RodEntity rodEntity = new RodEntity(
            user,
            world,
            EnchantmentHelper.getFishingLuckBonus(itemStack),
            EnchantmentHelper.getFishingSpeedBonus(itemStack)
        );
        world.playSound(
            (Player) null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL,0.5F,
            0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        world.addFreshEntity(rodEntity);
        user.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        user.gameEvent(GameEvent.ITEM_INTERACT_START);
    }
}
