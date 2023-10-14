package de.timolia.legacycombatsimulation.projectile.rod;

import de.timolia.legacycombatsimulation.projectile.ProjectileMath;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;

public class RodEntity extends FishingHook {
    public RodEntity(Player thrower, Level world, int luckOfTheSeaLevel, int lureLevel) {
        super(thrower, world, luckOfTheSeaLevel, lureLevel);
        ProjectileMath.applyBaseVelocity(this, thrower, 1);
    }
}
