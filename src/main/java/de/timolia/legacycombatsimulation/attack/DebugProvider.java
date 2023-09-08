package de.timolia.legacycombatsimulation.attack;

import de.timolia.legacycombatsimulation.LegacyCombatSimulation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DebugProvider {
    private static final LinkedList<ChatColor> colorsLeft = new LinkedList<>(Arrays.asList(ChatColor.values()));
    static {
        Collections.shuffle(colorsLeft);
    }
    private static final Map<Player, ChatColor> colors = new HashMap<>();
    private static final DebugContext dummy = new DebugContextDummy();

    public static ChatColor color(Player player) {
        return colors.computeIfAbsent(player, player1 -> colorsLeft.pop());
    }

    public static DebugContext start(Player player) {
        DebugContextImpl context = new DebugContextImpl();
        context.player = player;
        Bukkit.getScheduler().runTaskLater(
            LegacyCombatSimulation.plugin,
            () -> {
                if (!context.handheld) {
                    context.fail("1.20 PlayerHandler");
                }
            },
            2
        );
        return context;
    }

    public static DebugContext dummy() {
        return dummy;
    }

    public static class DebugContextImpl implements DebugContext {
        StringBuilder info = new StringBuilder();
        Set<String> tags = new HashSet<>();
        Player player;
        boolean handheld;
        boolean finished;

        public void fail(String message, Object... args) {
            if (args.length != 0) {
                message = String.format(message, args);
            }
            send(ChatColor.RED + "Rej: " + message);
            finished = true;
        }

        @Override
        public void info(String message, Object... args) {
            info.append(String.format(message, args)).append(' ');
        }

        @Override
        public void tag(String tag) {
            tags.add(tag);
        }

        @Override
        public void finish() {
            if (finished) {
                return;
            }
            send(info + " tags=" + tags);
        }

        private void send(String message) {
            if (finished || !handheld) {
                message += " (invalid state)";
            }
            String name = String.format("%1$5s", player.getName());
            if (name.length() > 5) {
                name = name.substring(0, 5);
            }
            String fullMessage = color(player) + name + " " + message;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("timolia.team")) {
                    onlinePlayer.sendMessage(fullMessage);
                }
            }
        }

        @Override
        public void markAsArrivedOnMainThread() {
            handheld = true;
        }
    }

    public static class DebugContextDummy implements DebugContext {
        @Override
        public void markAsArrivedOnMainThread() {

        }

        @Override
        public void fail(String message, Object... args) {

        }

        @Override
        public void info(String message, Object... args) {

        }

        @Override
        public void tag(String tag) {

        }

        @Override
        public void finish() {

        }
    }

    public interface DebugContext {
        void markAsArrivedOnMainThread();

        void fail(String message, Object... args);

        void info(String message, Object... args);

        void tag(String tag);

        void finish();
    }
}
