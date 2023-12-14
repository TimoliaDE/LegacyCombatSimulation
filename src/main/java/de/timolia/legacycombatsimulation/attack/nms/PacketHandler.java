package de.timolia.legacycombatsimulation.attack.nms;

import com.mojang.logging.LogUtils;
import de.timolia.legacycombatsimulation.attack.debug.DebugProvider.DebugContext;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

/* PlayerConnection / ServerGamePacketListenerImpl */
public class PacketHandler {
    static final Logger LOGGER = LogUtils.getLogger();

    public static void process(ServerPlayer player, Entity entity, DebugContext debugContext,
        Runnable attack) {
        if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb) && !(entity instanceof AbstractArrow) && (entity != player || player.isSpectator())) {
            ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (itemstack.isItemEnabled(player.serverLevel().enabledFeatures())) {
                attack.run();
                // CraftBukkit start
                if (!itemstack.isEmpty() && itemstack.getCount() <= -1) {
                    player.containerMenu.sendAllDataToRemote();
                }
                // CraftBukkit end
            } else {
                debugContext.fail("Item enable logic");
            }
        } else {
            debugContext.fail("Entity type");
            player.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"),  org.bukkit.event.player.PlayerKickEvent.Cause.INVALID_ENTITY_ATTACKED); // Paper - add cause
            LOGGER.warn("Player {} tried to attack an invalid entity", player.getName().getString());
        }
    }
}
