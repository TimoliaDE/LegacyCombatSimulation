package de.timolia.legacycombatsimulation;

import de.timolia.legacycombatsimulation.api.ClientVersion;
import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import de.timolia.legacycombatsimulation.attack.AttackHandler;
import de.timolia.legacycombatsimulation.attack.AttackInterceptor;
import de.timolia.legacycombatsimulation.attack.ClientAttackSpeedIndicator;
import de.timolia.legacycombatsimulation.attack.DebugProvider;
import de.timolia.legacycombatsimulation.blocking.SwordBlocking;
import de.timolia.legacycombatsimulation.consume.GoldenApple;
import de.timolia.legacycombatsimulation.environement.FireBlock;
import de.timolia.legacycombatsimulation.inventory.CreativeGiveItems;
import de.timolia.legacycombatsimulation.inventory.OffHand;
import de.timolia.legacycombatsimulation.movement.SwimmingPrevention;
import de.timolia.legacycombatsimulation.projectile.arrow.Bow;
import de.timolia.legacycombatsimulation.projectile.EnderPearl;
import java.util.Arrays;

import de.timolia.legacycombatsimulation.projectile.rod.FishingRod;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class LegacyCombatSimulation extends JavaPlugin implements Listener {
    public static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        AttackHandler attackHandler = new AttackHandler();
        AttackInterceptor interceptor = new AttackInterceptor(attackHandler);
        interceptor.register(this);
        registerBukkitListeners(
            new OffHand(),
            new EnderPearl(),
            new Bow(),
            new GoldenApple(),
            new FishingRod(this, new DebugProvider.DebugContextDummy()),
            new CreativeGiveItems(),
            new ClientAttackSpeedIndicator(),
            new ClientVersion(this),
            new SwordBlocking(),
            new SwimmingPrevention(DebugProvider.dummy()),
            new FireBlock(),
            this
        );
    }

    private void registerBukkitListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TargetRegistry.instance().enable(event.getPlayer(), Arrays.asList(SimulationTarget.values()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        TargetRegistry.instance().disableAll(event.getPlayer());
    }
}
