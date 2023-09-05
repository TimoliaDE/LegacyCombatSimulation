package de.timolia.legacycombatsimulation.movement;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;

public class PlayerVelocity {
    public static void sendCurrentAndSet(ServerPlayer target, double x, double y, double z) {
        target.connection.send(new ClientboundSetEntityMotionPacket(target));
        target.hurtMarked = false;
        target.setDeltaMovement(x, y, z);
    }

    public static void addVelocity() {

    }

    public static boolean velocityChanged(ServerPlayer target) {
        return target.hurtMarked;
    }
}
