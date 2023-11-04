package de.timolia.legacycombatsimulation.attack;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.SimulationTargetChangeEvent;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import java.util.UUID;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClientAttackSpeedIndicator implements Listener {
    private static final UUID MODIFIER_UNIQUE_ID = java.util.UUID.fromString("638f8a69-373e-4a14-94af-2b04d3eb54ce");
    private static final AttributeModifier MODIFIER = new AttributeModifier(
        MODIFIER_UNIQUE_ID,
        "LegacyCombatSimulation",
        1000,
        Operation.ADD_NUMBER
    );

    @EventHandler
    public void onSimulationTargetChange(SimulationTargetChangeEvent event) {
        Player player = event.getPlayer();
        boolean enabled = TargetRegistry.instance().isEnabled(player, SimulationTarget.ATTACK);
        AttributeInstance attribute = requireAttribute(player, Attribute.GENERIC_ATTACK_SPEED);
        if (enabled) {
            attribute.addTransientModifier(MODIFIER);
        } else {
            attribute.removeModifier(MODIFIER);
        }
    }

    private static AttributeInstance requireAttribute(Player player, Attribute attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            throw new IllegalStateException("Attribute " + attribute + " is not registered");
        }
        return instance;
    }
}
