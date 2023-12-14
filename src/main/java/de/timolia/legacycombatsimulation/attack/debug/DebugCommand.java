package de.timolia.legacycombatsimulation.attack.debug;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DebugCommand implements CommandExecutor {
    private static final Set<UUID> debugEnabled = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static boolean isDebugEnabledFor(UUID uniqueId) {
        return debugEnabled.contains(uniqueId);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (debugEnabled.add(player.getUniqueId())) {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        } else {
            debugEnabled.remove(player.getUniqueId());
            player.playSound(player, Sound.BLOCK_ANVIL_LAND, 0.3f, 1);
        }
        return true;
    }
}
