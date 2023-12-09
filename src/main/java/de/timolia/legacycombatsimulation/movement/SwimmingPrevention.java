package de.timolia.legacycombatsimulation.movement;

import de.timolia.legacycombatsimulation.LegacyCombatSimulation;
import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import de.timolia.legacycombatsimulation.attack.DebugProvider;
import de.timolia.legacycombatsimulation.attack.nms.EntityHurt;
import de.timolia.legacycombatsimulation.projectile.ProjectileMath;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SwimmingPrevention implements Listener {

    private DebugProvider.DebugContext debugContext;

    public SwimmingPrevention(DebugProvider.DebugContext debugContext) {
        this.debugContext = debugContext;
    }

    @EventHandler
    public void onPoseChange(EntityPoseChangeEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (!TargetRegistry.instance().isEnabled(player, SimulationTarget.SWIMMING_PREVENTION))
            return;
        if (event.getPose() != Pose.SWIMMING)
            return;
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                if (player.getPose() != Pose.SWIMMING) {
                    this.cancel();
                    return;
                }
                if (!TargetRegistry.instance().isEnabled(player, SimulationTarget.SWIMMING_PREVENTION)) {
                    this.cancel();
                    return;
                }
                if (legacyInWall(serverPlayer))
                    EntityHurt.hurtEntity(serverPlayer, serverPlayer.damageSources().inWall(), 1.0F, debugContext);
            }
        }.runTaskTimer(LegacyCombatSimulation.plugin, 2, 1);
    }


    private boolean legacyInWall(net.minecraft.world.entity.player.Player player) {
        if (player.isSleeping())
            return false;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

        float width = net.minecraft.world.entity.player.Player.STANDING_DIMENSIONS.width;

        for (int i = 0; i < 8; ++i) {
            int j = Mth.floor(player.getY() + (double) (((float) ((i >> 0) % 2) - 0.5F) * 0.1F) + (double) ProjectileMath.getHeadHeight(player));
            int k = Mth.floor(player.getX() + (double) (((float) ((i >> 1) % 2) - 0.5F) * width * 0.8F));
            int l = Mth.floor(player.getZ() + (double) (((float) ((i >> 2) % 2) - 0.5F) * width * 0.8F));

            if (mutableBlockPos.getX() != k
                    || mutableBlockPos.getY() != j
                    || mutableBlockPos.getZ() != l) {
                mutableBlockPos.set(k, j, l);
                if (player.level().getBlockState(mutableBlockPos).isSuffocating(player.level(), mutableBlockPos)) {
                    return true;
                }
            }
        }

        return false;
    }

}
