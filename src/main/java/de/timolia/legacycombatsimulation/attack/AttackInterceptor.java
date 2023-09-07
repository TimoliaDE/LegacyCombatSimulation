package de.timolia.legacycombatsimulation.attack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AttackInterceptor {
    private final AttackHandler attackHandler;

    public AttackInterceptor(AttackHandler attackHandler) {
        this.attackHandler = attackHandler;
    }

    public void register(Plugin plugin) {
        PacketType packetType = Client.USE_ENTITY;
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, packetType) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                ServerboundInteractPacket packet = (ServerboundInteractPacket) event.getPacket().getHandle();
                if (!packet.isAttack()) {
                    return;
                }
                Player player = event.getPlayer();
                if (!TargetRegistry.instance().isEnabled(player, SimulationTarget.ATTACK)) {
                    return;
                }
                ServerPlayer severPlayer = ((CraftPlayer) player).getHandle();
                Packet<?> replacement = createReplacementPacket(severPlayer, packet);
                event.setPacket(new PacketContainer(packetType, replacement));
            }
        });
    }

    private Packet<?> createReplacementPacket(ServerPlayer player, ServerboundInteractPacket packet) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            FriendlyByteBuf byteBuf = new FriendlyByteBuf(buffer);
            packet.write(byteBuf);
            return new InterceptPacket(byteBuf, player);
        } finally {
            buffer.release();
        }
    }

    class InterceptPacket extends ServerboundInteractPacket {
        private final ServerPlayer player;

        public InterceptPacket(FriendlyByteBuf buf, ServerPlayer serverPlayer) {
            super(buf);
            this.player = serverPlayer;
        }

        private void ensureAttack() {
            if (!isAttack()) {
                throw new IllegalStateException();
            }
        }

        private void ensureMainThread() {
            if (!Bukkit.isPrimaryThread()) {
                throw new IllegalStateException("Illegal thread");
            }
        }

        @Override
        public void dispatch(Handler handler) {
            ensureMainThread();
            ensureAttack();
            Entity entity = getTarget(player.serverLevel());
            /* support for papers UnknownEntityEvents */
            if (entity == null) {
                super.dispatch(handler);
                return;
            }
            if (attackHandler.handleAttack(player, entity)) {
                super.dispatch(handler);
            }
        }
    }
}
