package de.timolia.legacycombatsimulation.movement;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PlayerVelocity {
    public static void sendCurrentAndSet(ServerPlayer target, double x, double y, double z) {
        target.connection.send(new ClientboundSetEntityMotionPacket(target));
        target.hurtMarked = false;
        target.setDeltaMovement(x, y, z);
    }

    public static void addVelocity(Entity entity, double x, double y, double z) {
        entity.setDeltaMovement(entity.getDeltaMovement().add(x, y, z));
        entity.hurtMarked = true;
    }

    public static boolean velocityChanged(ServerPlayer target) {
        return target.hurtMarked;
    }
}
