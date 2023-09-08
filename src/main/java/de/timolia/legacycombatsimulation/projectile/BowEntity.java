package de.timolia.legacycombatsimulation.projectile;

import de.timolia.legacycombatsimulation.attack.DebugProvider;
import de.timolia.legacycombatsimulation.attack.nms.EntityHurt;
import de.timolia.legacycombatsimulation.movement.PlayerVelocity;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

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

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        float f2 = (float) this.getDeltaMovement().length()/*BullShitMath.sqrt(this.motX * this.motX + this.motY * this.motY + this.motZ * this.motZ)*/;
        int k = BullShitMath.f((double) f2 * baseDamage/*this.damage*/);

        if (isCritArrow()/*this.isCritical()*/) {
            k += this.random.nextInt(k / 2 + 2);
        }

        DamageSource damagesource;

        /*if (this.shooter == null) {
            damagesource = DamageSource.arrow(this, this);
        } else {
            damagesource = DamageSource.arrow(this, this.shooter);
        }*/
        if (getOwner() == null) {
            damagesource = this.damageSources().arrow(this, this);
        } else {
            damagesource = this.damageSources().arrow(this, getOwner());
        }

        Entity entity = entityHitResult.getEntity();
        // CraftBukkit start - Moved damage call
        if (EntityHurt.hurtEntity(entity, damagesource, k, DebugProvider.dummy())/*movingobjectposition.entity.damageEntity(damagesource, (float) k)*/) {
            if (
                isOnFire()/*this.isBurning()*/
                && entity.getType() != EntityType.ENDERMAN/*!(movingobjectposition.entity instanceof EntityEnderman)*/
                && (
                    !(entity instanceof Player)/*!(movingobjectposition.entity instanceof EntityPlayer)*/
                    || !(getOwner() instanceof Player)/*!(this.shooter instanceof EntityPlayer)*/
                    || level().pvpMode/*this.world.pvpMode*/
                )
            ) { // CraftBukkit - abide by pvp setting if destination is a player
                EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), 5);
                org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);
                if (!combustEvent.isCancelled()) {
                    //movingobjectposition.entity.setOnFire(combustEvent.getDuration());
                    entity.setSecondsOnFire(combustEvent.getDuration());
                }
                // CraftBukkit end
            }

            // if (movingobjectposition.entity.damageEntity(damagesource, (float) k)) { // CraftBukkit - moved up
            if (entity instanceof LivingEntity entityliving/*movingobjectposition.entity instanceof EntityLiving*/) {
                //EntityLiving entityliving = (EntityLiving) movingobjectposition.entity;

                //if (!this.world.isClientSide) {
                    //entityliving.o(entityliving.bv() + 1);
                    entityliving.setArrowCount(entityliving.getArrowCount() + 1);
                //}

                if (knockback/*this.knockbackStrength*/ > 0) {
                    Vec3 mot = getDeltaMovement();
                    //f3 = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ);
                    double f3 = mot.horizontalDistance();
                    if (f3 > 0.0F) {
                        /*movingobjectposition.entity.g(
                            this.motX * (double) this.knockbackStrength * 0.6000000238418579D / (double) f3, 0.1D,
                            this.motZ * (double) this.knockbackStrength * 0.6000000238418579D / (double) f3
                        );*/
                        PlayerVelocity.addVelocity(
                            entity,
                            mot.x * knockback * 0.6000000238418579D / f3,
                            0.1D,
                            mot.z * knockback * 0.6000000238418579D / f3
                        );
                    }
                }

                if (getOwner() instanceof LivingEntity/*this.shooter instanceof EntityLiving*/) {
                    //EnchantmentManager.a(entityliving, this.shooter);
                    //EnchantmentManager.b((EntityLiving) this.shooter, entityliving);
                    EnchantmentHelper.doPostHurtEffects(entityliving, getOwner());
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) getOwner(), entityliving);
                }

                if (
                    getOwner()/*this.shooter */ != null
                    && entity/*movingobjectposition.entity*/ != getOwner()/*this.shooter */
                    && entity instanceof Player/*movingobjectposition.entity instanceof EntityHuman*/
                    && getOwner() instanceof ServerPlayer/*this.shooter instanceof EntityPlayer*/
                ) {
                    //((EntityPlayer) this.shooter).playerConnection.sendPacket(new PacketPlayOutGameStateChange(6, 0.0F));
                    ((ServerPlayer) getOwner()).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }
            }

            //this.makeSound("random.bowhit", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (entity.getType() != EntityType.ENDERMAN/*!(movingobjectposition.entity instanceof EntityEnderman)*/) {
                //this.die();
                discard();
            }
        } else {
            //this.motX *= -0.10000000149011612D;
            //this.motY *= -0.10000000149011612D;
            //this.motZ *= -0.10000000149011612D;
            setDeltaMovement(getDeltaMovement().scale(-0.10000000149011612D));
            //this.yaw += 180.0F;
            this.setYRot(this.getYRot() + 180.0F);
            //this.lastYaw += 180.0F;
            this.yRotO += 180.0F;
            //this.as = 0;
        }
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
