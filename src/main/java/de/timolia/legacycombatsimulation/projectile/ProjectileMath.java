package de.timolia.legacycombatsimulation.projectile;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.entity.HumanEntity;

public class ProjectileMath {
    private static final Random random = new Random();

    public static void applyBaseVelocity(Entity entity, Entity owner, float f, float f1, boolean rand) {
        entity.setPos(
            owner.getX() - (double) (Mth.cos(entity.getYRot() / 180.0F * 3.1415927F) * 0.16F),
            getHeadHeight(entity) - 0.10000000149011612D,
            owner.getZ() - (double) (Mth.sin(entity.getYRot() / 180.0F * 3.1415927F) * 0.16F)
        );
        entity.setRot(owner.getYRot(), owner.getXRot());
        double motX = f1 * -Mth.sin(entity.getYRot() / 180.0F * 3.1415927F) * Mth.cos(entity.getXRot() / 180.0F * 3.1415927F);
        double motZ = f1 * Mth.cos(entity.getYRot()/ 180.0F * 3.1415927F) * Mth.cos(entity.getXRot() / 180.0F * 3.1415927F);
        double motY = f1 * -Mth.sin(entity.getXRot() / 180.0F * 3.1415927F);
        shoot(entity, motX, motY, motZ, f * 1.5F, 1.0F, rand);
    }

    public static void shoot(Entity entity, double d0, double d1, double d2, float f, float f1, boolean rand) {
        float f2 = (float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        d0 += random.nextGaussian() * (double) (!rand && random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d1 += random.nextGaussian() * (double) (!rand && random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d2 += random.nextGaussian() * (double) (!rand && random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d0 *= (double) f;
        d1 *= (double) f;
        d2 *= (double) f;
        entity.setDeltaMovement(d0, d1, d2);
        float f3 = (float) Math.sqrt(d0 * d0 + d2 * d2);
        //this.lastYaw = this.yaw = (float) ();
        //this.lastPitch = this.pitch = (float) ();
        //this.ar = 0;
        entity.setRot(
            (float) (BullShitMath.bullShitAtan2(d0, d2) * 180.0D / 3.1415927410125732D),
            (float) (BullShitMath.bullShitAtan2(d1, (double) f3) * 180.0D / 3.1415927410125732D)
        );
        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();
    }

    public static double getHeadHeight(Entity entity) {
        if (!(entity instanceof Player player))
            return entity.getEyeY();

        float f = 1.62F;

        if (player.isSleeping()) {
            f = 0.2F;
        }

        if (player.isShiftKeyDown()) {
            f -= 0.08F;
        }

        return f;
    }
}
