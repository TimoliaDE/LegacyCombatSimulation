package de.timolia.legacycombatsimulation.projectile.rod;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import de.timolia.legacycombatsimulation.LegacyCombatSimulation;
import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import de.timolia.legacycombatsimulation.attack.DebugProvider;
import de.timolia.legacycombatsimulation.attack.nms.EntityHurt;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftProjectile;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class FishingRod implements Listener {

    private DebugProvider.DebugContext debugContext;

    public FishingRod(LegacyCombatSimulation plugin, DebugProvider.DebugContext debugContext) {
        this.debugContext = debugContext;
        interceptRod(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNonDamageHits(ProjectileHitEvent event) {
        if (event.isCancelled())
            return;
        if (event.getEntity().getType() == EntityType.FISHING_HOOK) {
            if (!(event.getHitEntity() instanceof Player player))
                return;
            Projectile projectile = ((CraftProjectile) event.getEntity()).getHandle();
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            Entity attacker = null;
            if (event.getEntity().getShooter() instanceof Player shooter)
                attacker = ((CraftPlayer) shooter).getHandle();
            DamageSource damageSource = ((CraftWorld) player.getWorld()).getHandle().damageSources().thrown(projectile, attacker);
            EntityHurt.hurtEntity(serverPlayer, damageSource, Float.MIN_VALUE, debugContext);
            if (((CraftProjectile) event.getEntity()).getHandle() instanceof RodEntity rodEntity)
                rodEntity.hitPlayer();

        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onListener(PlayerFishEvent event) {
        if (event.isCancelled())
            return;

        if (!TargetRegistry.instance().isEnabled(event.getPlayer(), SimulationTarget.FISHING_ROD))
            return;

        if (event.getState() == State.CAUGHT_ENTITY && event.getCaught() instanceof Player player) {
            event.setCancelled(true);
            event.getHook().remove();
            org.bukkit.inventory.ItemStack rodItem = event.getPlayer().getInventory().getItemInMainHand();
            if (rodItem.getType() != Material.FISHING_ROD)
                return;
            rodItem.damage(3, event.getPlayer()); // 1.8 damages rod by 3 when retracting an entity
        }


        if (event.getState() != State.FISHING)
            return;
        event.getHook().remove();
        ServerPlayer user = ((CraftPlayer) event.getPlayer()).getHandle();
        Level world = user.level();
        ItemStack itemStack = user.getItemInHand(CraftEquipmentSlot.getHand(event.getHand()));
        RodEntity rodEntity = new RodEntity(
            user,
            world,
            EnchantmentHelper.getFishingLuckBonus(itemStack),
            EnchantmentHelper.getFishingSpeedBonus(itemStack)
        );
        world.addFreshEntity(rodEntity);
        world.playSound(
                (net.minecraft.world.entity.player.Player) null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL,0.5F,
            0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        user.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        user.gameEvent(GameEvent.ITEM_INTERACT_START);
    }


    public void interceptRod(LegacyCombatSimulation plugin) {
        PacketType packetType = Server.REL_ENTITY_MOVE;
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, packetType,
                        Server.REL_ENTITY_MOVE_LOOK,
                        Server.ENTITY_TELEPORT,
                        Server.ENTITY_VELOCITY,
                        Server.SPAWN_ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {

                //if (!ClientVersion.isLegacyPlayer(event.getPlayer()))
                //   return;

                if (event.getPacket().getHandle() instanceof ClientboundMoveEntityPacket.Pos clientboundMoveEntityPacket) {
                    if (clientboundMoveEntityPacket.getEntity(((CraftWorld) event.getPlayer().getWorld()).getHandle()) instanceof RodEntity rodEntity) {
                        rodEntity.handleMovePacket(event);
                    }
                }
                if (event.getPacket().getHandle() instanceof ClientboundMoveEntityPacket.PosRot clientboundMoveEntityPacket) {
                    if (clientboundMoveEntityPacket.getEntity(((CraftWorld) event.getPlayer().getWorld()).getHandle()) instanceof RodEntity rodEntity) {
                        rodEntity.handleMovePacket(event);
                    }
                }
                if (event.getPacket().getHandle() instanceof ClientboundMoveEntityPacket.Rot clientboundMoveEntityPacket) {
                    if (clientboundMoveEntityPacket.getEntity(((CraftWorld) event.getPlayer().getWorld()).getHandle()) instanceof RodEntity rodEntity) {
                        rodEntity.handleMovePacket(event);
                    }
                }
            }
        });
    }
}
