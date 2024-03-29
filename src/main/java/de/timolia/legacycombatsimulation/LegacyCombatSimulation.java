package de.timolia.legacycombatsimulation;

import de.timolia.legacycombatsimulation.api.ClientVersion;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import de.timolia.legacycombatsimulation.attack.AttackHandler;
import de.timolia.legacycombatsimulation.attack.AttackInterceptor;
import de.timolia.legacycombatsimulation.attack.ClientAttackSpeedIndicator;
import de.timolia.legacycombatsimulation.attack.debug.DebugCommand;
import de.timolia.legacycombatsimulation.attack.debug.DebugProvider;
import de.timolia.legacycombatsimulation.blocking.SwordBlocking;
import de.timolia.legacycombatsimulation.consume.GoldenApple;
import de.timolia.legacycombatsimulation.consume.HungerSystem;
import de.timolia.legacycombatsimulation.environement.FireBlock;
import de.timolia.legacycombatsimulation.environement.Sounds;
import de.timolia.legacycombatsimulation.environement.TnT;
import de.timolia.legacycombatsimulation.inventory.CreativeGiveItems;
import de.timolia.legacycombatsimulation.inventory.OffHand;
import de.timolia.legacycombatsimulation.movement.MovementCommand;
import de.timolia.legacycombatsimulation.movement.SwimmingPrevention;
import de.timolia.legacycombatsimulation.projectile.arrow.Bow;
import de.timolia.legacycombatsimulation.projectile.EnderPearl;

import de.timolia.legacycombatsimulation.projectile.rod.FishingRod;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class LegacyCombatSimulation extends JavaPlugin implements Listener {
    public static Plugin plugin;
    public static Configuration configuration;

    @Override
    public void onEnable() {
        plugin = this;
        getCommand("LegacyCombatSimulation").setExecutor(new DebugCommand());
        getCommand("modifykb").setExecutor(new MovementCommand());
        AttackHandler attackHandler = new AttackHandler();
        AttackInterceptor interceptor = new AttackInterceptor(attackHandler);
        interceptor.register(this);
        registerBukkitListeners(
            new OffHand(),
            new EnderPearl(),
            new Bow(),
            new GoldenApple(),
            new CreativeGiveItems(),
            new ClientAttackSpeedIndicator(),
            new FishingRod(this, new DebugProvider.DebugContextDummy()),
            new ClientVersion(this),
            new SwordBlocking(),
            new SwimmingPrevention(DebugProvider.dummy()),
            new FireBlock(),
            new HungerSystem(),
            new Sounds(this),
            new TnT(),
            this
        );

        configuration = new Configuration(getConfig());
    }

    private void registerBukkitListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TargetRegistry.instance().enableAll(event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        TargetRegistry.instance().disableAll(event.getPlayer());
    }

    @EventHandler
    public void onSpring(PlayerToggleSprintEvent event) {

    }
}
