package de.timolia.legacycombatsimulation.projectile;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class BowEntity extends Arrow {
    public BowEntity(Level world, LivingEntity owner, float f) {
        super(world, owner);
        setRot(owner.getYRot(), owner.getXRot());
        setPos(
            owner.getX() - (double) (Mth.cos(getYRot() / 180.0F * 3.1415927F) * 0.16F),
            owner.getEyeY() - 0.10000000149011612D,
            owner.getZ() - (double) (Mth.sin(getYRot() / 180.0F * 3.1415927F) * 0.16F)
        );
        double motX = -Mth.sin(getYRot() / 180.0F * 3.1415927F) * Mth.cos(getXRot() / 180.0F * 3.1415927F);
        double motZ = Mth.cos(getYRot()/ 180.0F * 3.1415927F) * Mth.cos(getXRot() / 180.0F * 3.1415927F);
        double motY = -Mth.sin(getXRot() / 180.0F * 3.1415927F);
        this.shoot(motX, motY, motZ, f * 1.5F, 1.0F);
    }

    public void shoot(double d0, double d1, double d2, float f, float f1) {
        float f2 = (float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        d0 += this.random.nextGaussian() * (double) (this.random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d1 += this.random.nextGaussian() * (double) (this.random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d2 += this.random.nextGaussian() * (double) (this.random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d0 *= (double) f;
        d1 *= (double) f;
        d2 *= (double) f;
        setDeltaMovement(d0, d1, d2);
        float f3 = (float) Math.sqrt(d0 * d0 + d2 * d2);
        //this.lastYaw = this.yaw = (float) ();
        //this.lastPitch = this.pitch = (float) ();
        //this.ar = 0;
        setRot(
            (float) (BullShitMath.bullShitAtan2(d0, d2) * 180.0D / 3.1415927410125732D),
            (float) (BullShitMath.bullShitAtan2(d1, (double) f3) * 180.0D / 3.1415927410125732D)
        );
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
}
