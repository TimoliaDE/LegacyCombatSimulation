package de.timolia.legacycombatsimulation.attack.nms;

import de.timolia.legacycombatsimulation.attack.DebugProvider.DebugContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class EntityHurt {
    public static boolean hurtEntity(Entity entity, DamageSource damagesource, float amount,
        DebugContext debugContext) {
        if (entity instanceof ServerPlayer player) {
            return hurt(player, damagesource, amount, debugContext);
        }
        if (entity instanceof Player) {
            throw new IllegalArgumentException("Unexpected: " + entity.getClass().getSimpleName());
        }
        if (entity instanceof LivingEntity living) {
            return hurtLiving(living, damagesource, amount, debugContext);
        }
        throw new IllegalArgumentException("Unexpected: " + entity.getClass().getSimpleName());
    }

    /* EntityPlayer or ServerPlayer  */
    public static boolean hurt(ServerPlayer player, DamageSource damagesource, float amount,
        DebugContext debugContext) {
        if (player.isInvulnerableTo(damagesource)) {
            debugContext.fail("invulnerable to source %s", damagesource.toString());
            return false;
        } else {
            boolean flag = false; /*this.server.ae() && this.cr() && "fall".equals(damagesource.translationIndex);*/

            if (!flag && player.spawnInvulnerableTime > 0/* this.invulnerableTicks > 0 */
                && !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)/*&& damagesource != DamageSource.OUT_OF_WORLD*/) {
                debugContext.fail("spawnInvulnerableTime");
                return false;
            } else {
                //if (damagesource instanceof EntityDamageSource) { DamageSource now has a nullable entity
                    Entity entity = damagesource.getEntity();

                    if (
                        entity instanceof Player entityPlayer /* entity instanceof EntityHuman */
                        && !player.canHarmPlayer(entityPlayer) /* && !this.a((EntityHuman) entity) */) {
                        debugContext.fail("canHarmPlayer (direct)");
                        return false;
                    }

                    /*if (entity instanceof EntityArrow) {
                        EntityArrow entityarrow = (EntityArrow) entity;

                        if (entityarrow.shooter instanceof EntityHuman && !this.a((EntityHuman) entityarrow.shooter)) {
                            return false;
                        }
                    }*/
                    if (
                        entity instanceof AbstractArrow arrow
                        && arrow.getOwner() instanceof Player entityPlayer
                        && !player.canHarmPlayer(entityPlayer)
                    ) {
                        debugContext.fail("canHarmPlayer (arrow)");
                        return false;
                    }
                }

                // Paper start - cancellable death events
                //return super.hurt(source, amount); paper code is from 1.20
                player.queueHealthUpdatePacket = true;
                boolean damaged = hurtPlayer(player, damagesource, amount, debugContext);
                player.queueHealthUpdatePacket = false;
                if (player.queuedHealthUpdatePacket != null) {
                    player.connection.send(player.queuedHealthUpdatePacket);
                    player.queuedHealthUpdatePacket = null;
                }
                return damaged;
                // Paper end
            //}
        }
    }

    /* EntityHuman or Player */
    public static boolean hurtPlayer(ServerPlayer player, DamageSource damagesource, float amount,
        DebugContext debugContext) {
        /*if (this.isInvulnerable(damagesource)) { checked by parent
            return false;
        } else */ if (player.getAbilities().invulnerable /*this.abilities.isInvulnerable*/
            && !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) /*!damagesource.ignoresInvulnerability() */) {
            debugContext.fail("invulnerable abilities");
            return false;
        } else {
            //this.ticksFarFromPlayer = 0;
            player.setNoActionTime(0);
            if (player.isDeadOrDying()/*this.getHealth() <= 0.0F*/) {
                debugContext.fail("isDeadOrDying");
                return false;
            } else {
                /*if (this.isSleeping() && !this.world.isClientSide) { 1.20 moved this down to living
                    this.a(true, true, false);
                }*/

                if (damagesource.scalesWithDifficulty()/* damagesource.r() */) {
                    if (player.level().getDifficulty() == Difficulty.PEACEFUL) {
                        return false; // CraftBukkit - f = 0.0f -> return false
                    }

                    if (player.level().getDifficulty() == Difficulty.EASY) {
                        amount = amount / 2.0F + 1.0F;
                    }

                    if (player.level().getDifficulty() == Difficulty.HARD) {
                        amount = amount * 3.0F / 2.0F;
                    }
                }

                if (false /*&& f == 0.0F*/) { // CraftBukkit - Don't filter out 0 damage
                    return false;
                } else {
                    /*Entity entity = damagesource.getEntity();irelevant?

                    if (entity instanceof EntityArrow && ((EntityArrow) entity).shooter != null) {
                        entity = ((EntityArrow) entity).shooter;
                    }*/
                    //TODO removeEntitiesOnShoulder?
                    return hurtLiving(player, damagesource, amount, debugContext);
                }
            }
        }
    }

    /* EnittyLiving or LivingEntity */
    public static boolean hurtLiving(LivingEntity player, DamageSource damagesource, float amount,
        DebugContext debugContext) {
        /*if (this.isInvulnerable(damagesource)) { checked by parent
            return false;
        } else if (this.world.isClientSide) { always server side
            return false;
        } else {
            this.ticksFarFromPlayer = 0; set by parent
         */
            /* 1.8: this.getHealth() <= 0.0F
             * 1.20: this.isRemoved() || this.dead || this.getHealth() <= 0.0F
             * above's 1.20 player.isDeadOrDying() is equivalent to this.getHealth() <= 0.0F
             */
            if (player.isRemoved()) {
                debugContext.fail("removed");
                return false;
            } else if (damagesource.is(DamageTypeTags.IS_FIRE)/* damagesource.o() */
                && player.hasEffect(MobEffects.FIRE_RESISTANCE)/* && this.hasEffect(MobEffectList.FIRE_RESISTANCE)*/) {
                debugContext.fail("FIRE_RESISTANCE");
                return false;
            } else {
                /* moved down as in 1.20 */
                if (player.isSleeping() /*&& !this.level.isClientSide*/) {
                    player.stopSleeping();
                }
                // CraftBukkit - Moved into d(DamageSource, float) - commented out by timolia
                /*if (false && (damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && this.getEquipment(4) != null) {
                    this.getEquipment(4).damage((int) (f * 4.0F + this.random.nextFloat() * f * 2.0F), this);
                    f *= 0.75F;
                }*/

                //this is likely translated to this.walkAnimation.setSpeed(1.5F); but has no effect for serverside
                //this.aB = 1.5F;
                boolean flag = true;

                /* mojang removed this in 1.20 but craftbukkit readded it */
                if ((float) player.invulnerableTime > (float) player.invulnerableDuration / 2.0F/*(float) this.noDamageTicks > (float) this.maxNoDamageTicks / 2.0F*/) {
                    if (amount <= player.lastHurt/*this.lastDamage*/) {
                        debugContext.fail("hurt time left=%s", (float) player.invulnerableTime - (float) player.invulnerableDuration / 2.0F);
                        return false;
                    }

                    // CraftBukkit start
                    if (!Damage.damageEntity0(player, damagesource, amount - player.lastHurt/*f - this.lastDamage*/, debugContext)) {
                        return false;
                    }
                    // CraftBukkit end
                    /*this.lastDamage = f;*/
                    player.lastHurt = amount;
                    flag = false;
                } else {
                    // CraftBukkit start
                    //float previousHealth = this.getHealth();
                    if (!Damage.damageEntity0(player, damagesource, amount, debugContext)) {
                        return false;
                    }

                    //this.lastDamage = f;
                    player.lastHurt = amount;
                    //this.noDamageTicks = this.maxNoDamageTicks;
                    player.invulnerableTime = player.invulnerableDuration;
                    // CraftBukkit end
                    //this.hurtTicks = this.av = 10;
                    player.hurtTime = player.hurtDuration = 10;
                }

                // CraftBukkit start
                /*if(player instanceof EntityAnimal){ we only handle players
                    ((EntityAnimal)this).cq();
                    if(this instanceof EntityTameableAnimal){
                        ((EntityTameableAnimal)this).getGoalSit().setSitting(false);
                    }
                }*/
                // CraftBukkit end

                //this.aw = 0.0F; kontrolliert velocity beim tod? idk tbh
                Entity entity = damagesource.getEntity();

                if (entity != null) {
                    if (entity instanceof LivingEntity living/* EntityLiving */) {
                        player.setLastHurtByMob(living);
                        //this.b((EntityLiving) entity);
                    }

                    if (entity instanceof Player entityhuman/* EntityHuman */) {
                        //this.lastDamageByPlayerTime = 100;
                        //this.killer = (EntityHuman) entity;
                        player.lastHurtByPlayerTime = 100;
                        player.lastHurtByPlayer = entityhuman;
                    }/* else if (entity instanceof EntityWolf) { we only handle players
                        EntityWolf entitywolf = (EntityWolf) entity;

                        if (entitywolf.isTamed()) {
                            this.lastDamageByPlayerTime = 100;
                            this.killer = null;
                        }
                    }*/
                }

                if (flag) {
                    //this.world.broadcastEntityEffect(this, (byte) 2);
                    player.level().broadcastDamageEvent(player, damagesource);//1.20 version
                    player.level().broadcastEntityEvent(player, (byte) 2);//not sure if this even exists in 1.20 client anymore
                    if (!damagesource.is(DamageTypeTags.NO_IMPACT) /*damagesource != DamageSource.DROWN*/) {
                        //PlayerVelocity.velocityChanged(player);
                        player.hurtMarked = true;//TODO 1.8 knock resist
                    }

                    if (entity != null) {
                        double d0 = entity.getX() - player.getX();

                        double d1;

                        for (d1 = entity.getZ() - player.getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                            d0 = (Math.random() - Math.random()) * 0.01D;
                        }

                        //this.aw = (float) (MathHelper.b(d1, d0) * 180.0D / 3.1415927410125732D - (double) this.yaw); again after death velo
                        //this.a(entity, f, d0, d1);
                        applyVelocity(player, d0, d1, debugContext);
                        //method for 1.20 ClientboundHurtAnimationPacket
                        player.indicateDamage(d0, d1);
                    } else {
                        //this.aw = (float) ((int) (Math.random() * 2.0D) * 180);
                        debugContext.fail("keine ahnung bro");
                    }
                }

                String s;

                if (player.isDeadOrDying()/*this.getHealth() <= 0.0F*/) {
                    /*s = this.bp(); TODO sound
                    if (flag && s != null) {
                        this.makeSound(s, this.bB(), this.bC());
                    }*/

                    player.die(damagesource);
                } else {
                    /*s = this.bo();
                    if (flag && s != null) {
                        this.makeSound(s, this.bB(), this.bC());
                    }*/
                    SoundEvent sound = damagesource.type().effects().sound();
                    if (flag) {
                        player.playSound(sound, player.getSoundVolume(), player.getVoicePitch());
                    }
                }

                return true;
            //}
        }
    }

    private static void applyVelocity(LivingEntity player, double x, double z,
        DebugContext debugContext) {
        if (player.getRandom().nextDouble() >= player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) {
            //this.ai = true;
            player.hasImpulse = true;
            float f1 = (float) Math.sqrt(x * x + z * z);
            float f2 = 0.4F;
            double motX = player.getDeltaMovement().x;
            double motY = player.getDeltaMovement().y;
            double motZ = player.getDeltaMovement().z;
            motX /= 2.0D;
            motY /= 2.0D;
            motZ /= 2.0D;
            motX -= x / (double) f1 * (double) f2;
            motY += f2;
            motZ -= z / (double) f1 * (double) f2;
            if (motY > 0.4000000059604645D) {
                motY = 0.4000000059604645D;
            }
            player.setDeltaMovement(motX, motY, motZ);
            debugContext.info("velo (%.2f, %.2f, %.2f) %.2f %.2f", motX, motY, motZ, x, z);
        } else {
            debugContext.tag("KNOCK_RESI");
        }
    }
}
