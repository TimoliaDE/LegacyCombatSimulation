package de.timolia.legacycombatsimulation;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import de.timolia.legacycombatsimulation.attack.AttackHandler;
import de.timolia.legacycombatsimulation.attack.AttackInterceptor;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LegacyCombatSimulation extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        AttackHandler attackHandler = new AttackHandler();
        AttackInterceptor interceptor = new AttackInterceptor(attackHandler);
        interceptor.register(this);
        Bukkit.getPluginManager().registerEvents(this, this);
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
