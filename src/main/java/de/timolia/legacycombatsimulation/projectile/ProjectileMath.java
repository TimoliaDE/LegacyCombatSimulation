package de.timolia.legacycombatsimulation.projectile;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ProjectileMath {
    private static final Random random = new Random();

    public static void applyBaseVelocity(Entity entity, Entity owner, float f) {
        entity.setRot(owner.getYRot(), owner.getXRot());
        entity.setPos(
            owner.getX() - (double) (Mth.cos(entity.getYRot() / 180.0F * 3.1415927F) * 0.16F),
            owner.getEyeY() - 0.10000000149011612D,
            owner.getZ() - (double) (Mth.sin(entity.getYRot() / 180.0F * 3.1415927F) * 0.16F)
        );
        double motX = -Mth.sin(entity.getYRot() / 180.0F * 3.1415927F) * Mth.cos(entity.getXRot() / 180.0F * 3.1415927F);
        double motZ = Mth.cos(entity.getYRot()/ 180.0F * 3.1415927F) * Mth.cos(entity.getXRot() / 180.0F * 3.1415927F);
        double motY = -Mth.sin(entity.getXRot() / 180.0F * 3.1415927F);
        shoot(entity, motX, motY, motZ, f * 1.5F, 1.0F);
    }

    public static void shoot(Entity entity, double d0, double d1, double d2, float f, float f1) {
        float f2 = (float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        d0 += random.nextGaussian() * (double) (random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d1 += random.nextGaussian() * (double) (random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d2 += random.nextGaussian() * (double) (random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
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
}
