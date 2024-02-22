package de.timolia.legacycombatsimulation.movement;

import de.timolia.legacycombatsimulation.LegacyCombatSimulation;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class MovementCommand implements CommandExecutor {

    private static DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (args.length < 2) {
            sendDebug(player);
            return false;
        }

        double vkb;
        double hkb;

        try {
            vkb = Double.parseDouble(args[0]);
            hkb = Double.parseDouble(args[1]);
        } catch (Exception e) {
            sendDebug(player);
            return false;
        }

        LegacyCombatSimulation.configuration.setHorizontalKnockbackMultiplier(hkb);
        LegacyCombatSimulation.configuration.setVerticalKnockbackMultiplier(vkb);

        player.sendMessage("§aUpdated values");

        return true;
    }

    private void sendDebug(Player player) {
        player.sendMessage("Current: <vertical> - " + decimalFormat.format(LegacyCombatSimulation.configuration.getVerticalKnockbackMultiplier()) + " // " +
                        "<horizontal> - " + decimalFormat.format(LegacyCombatSimulation.configuration.getHorizontalKnockbackMultiplier()));
        player.sendMessage("§7Usage: /modifykb <vertical> <horizontal>. Example: /modifykb 1.2 0.8");
    }
}
