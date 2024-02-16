package de.timolia.legacycombatsimulation.environement;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.timolia.legacycombatsimulation.LegacyCombatSimulation;
import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R1.CraftSound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;

import java.util.EnumMap;

public class Sounds implements Listener {

    private EnumMap<Sound, Sound> soundReplacements = new EnumMap<>(Sound.class);
    public Sounds(LegacyCombatSimulation plugin) {
        soundReplacements.put(Sound.ENTITY_GENERIC_BURN, Sound.ENTITY_PLAYER_HURT);
        soundReplacements.put(Sound.ENTITY_PLAYER_HURT_ON_FIRE, Sound.ENTITY_PLAYER_HURT);
        soundReplacements.put(Sound.ENTITY_PLAYER_HURT_DROWN, Sound.ENTITY_PLAYER_HURT);

        for (Sound sound : soundReplacements.keySet()) {
            if (soundReplacements.containsValue(sound)) {
                System.err.println("Circular Sound connection found! Cannot replace sounds");
                return;
            }
        }
        register(plugin);
    }

    public void register(LegacyCombatSimulation plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.NAMED_SOUND_EFFECT) {

            @Override
            public void onPacketSending(final PacketEvent event) {
                final PacketContainer packet = event.getPacket();
                if (packet.getHandle() instanceof ClientboundSoundPacket clientboundSoundPacket) {
                    SoundEvent soundEvent = clientboundSoundPacket.getSound().value();
                    Sound bukkitSound = CraftSound.getBukkit(soundEvent);
                    Sound replacement = soundReplacements.get(bukkitSound);
                    if (replacement == null)
                        return;

                    if (!TargetRegistry.instance().isEnabled(event.getPlayer(), SimulationTarget.LEGACY_CLIENT_SOUNDS))
                        return;

                    event.setCancelled(true);
                    event.getPlayer().playSound(
                            new Location(event.getPlayer().getWorld(), clientboundSoundPacket.getX(), clientboundSoundPacket.getY(), clientboundSoundPacket.getZ()),
                            replacement,
                            clientboundSoundPacket.getVolume(),
                            clientboundSoundPacket.getPitch()
                    );
                }

            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        if (event.isCancelled())
            return;
        if (!TargetRegistry.instance().isEnabled(event.getPlayer(), SimulationTarget.LEGACY_CLIENT_SOUNDS))
            return;
        event.getPlayer().playSound(event.getArrow().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1);
    }

}
