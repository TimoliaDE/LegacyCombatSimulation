package de.timolia.legacycombatsimulation.api;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.timolia.legacycombatsimulation.LegacyCombatSimulation;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientVersion implements Listener {

    private static final Map<UUID, Integer> playerVersions = new HashMap<>();

    public ClientVersion(LegacyCombatSimulation plugin) {
        register(plugin);
    }

    public void register(LegacyCombatSimulation plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.NORMAL,
                PacketType.Handshake.Client.SET_PROTOCOL, PacketType.Login.Server.DISCONNECT) {

            @Override
            public void onPacketReceiving(final PacketEvent event) {
                // not feasible because of via
                /*final PacketContainer packet = event.getPacket();
                if (packet.getHandle() instanceof ClientIntentionPacket clientIntentionPacket) {
                    playerVersions.put(event.getPlayer().getUniqueId(), clientIntentionPacket.getProtocolVersion());
                } else if (packet.getHandle() instanceof ClientboundLoginDisconnectPacket) {
                    playerVersions.remove(event.getPlayer().getUniqueId());
                }

                 */
            }
        });
    }

    public static int getVersion(Player player) {
        return playerVersions.get(player.getUniqueId());
    }

    public static boolean isLegacyPlayer(Player player) {
        return playerVersions.getOrDefault(player.getUniqueId(), Integer.MAX_VALUE) <= 47;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerVersions.remove(event.getPlayer().getUniqueId());
    }
}
