package de.timolia.legacycombatsimulation.projectile.rod;

import com.comphenix.protocol.events.PacketEvent;
import de.timolia.legacycombatsimulation.projectile.ProjectileMath;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;


public class RodEntity extends FishingHook {

    boolean suppressPackets = true;

    public RodEntity(Player thrower, Level world, int luckOfTheSeaLevel, int lureLevel) {
        super(thrower, world, luckOfTheSeaLevel, lureLevel);
        ProjectileMath.applyBaseVelocity(this, thrower, 1, 0.4F, false);
    }

    public void handleMovePacket(PacketEvent packetEvent) {
        packetEvent.setCancelled(true);
        if (suppressPackets)
            return;
        sendLocationUpdate(packetEvent.getPlayer());
    }

    private void sendLocationUpdate(org.bukkit.entity.Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(new ClientboundTeleportEntityPacket(this));
    }

    public void hitPlayer() {
        suppressPackets = false;
    }

}
