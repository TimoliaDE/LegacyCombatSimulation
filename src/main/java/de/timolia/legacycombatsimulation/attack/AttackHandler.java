package de.timolia.legacycombatsimulation.attack;

import de.timolia.legacycombatsimulation.attack.nms.EnchantmentManager;
import de.timolia.legacycombatsimulation.attack.nms.ItemStackHurt;
import de.timolia.legacycombatsimulation.attack.nms.PacketHandler;
import de.timolia.legacycombatsimulation.movement.PlayerVelocity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

public class AttackHandler {
    public boolean handleAttack(ServerPlayer player, Entity damaged) {
        PacketHandler.process(player, damaged, () -> {
            serverPlayerAttack(player, damaged);
        });
        return false;
    }

    private void serverPlayerAttack(ServerPlayer player, Entity target) {
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            player.setCamera(target);
        } else {
            playerAttack(player, target);
        }
    }

    private void playerAttack(ServerPlayer player, Entity entity) {
        /*if (entity.aD()) { irrelevant */
        if (!entity.skipAttackInteraction(player)) { /* !entity.l(this) */
            //float f = (float) player.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
            float f = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            //byte b0 = 0; never assigned - might be decopiler leftover
            float f1 = 0.0F;

            /*if (entity instanceof EntityLiving) {
                f1 = EnchantmentManager.a(this.bA(), ((EntityLiving) entity).getMonsterType());
            } else {
                f1 = EnchantmentManager.a(this.bA(), EnumMonsterType.UNDEFINED);
            }*/
            if (entity instanceof LivingEntity) {
                f1 = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), ((LivingEntity) entity).getMobType());
            } else {
                f1 = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED);
            }

            //int i = b0 + EnchantmentManager.a((EntityLiving) this);
            int i = EnchantmentHelper.getKnockbackBonus(player);

            if (player.isSprinting()) {
                ++i;
            }

            if (f > 0.0F || f1 > 0.0F) {
                boolean flag = player.fallDistance > 0.0F
                    && !player.onGround
                    && !player.onClimbable() /* !this.k_() */
                    && !player.isInWater() /* !this.V() */
                    && !player.hasEffect(MobEffects.BLINDNESS) /* !this.hasEffect(MobEffectList.BLINDNESS) */
                    && !player.isPassenger() /* this.vehicle == null */
                    && entity instanceof LivingEntity /* entity instanceof EntityLiving */;

                if (flag && f > 0.0F) {
                    f *= 1.5F;
                }

                f += f1;
                boolean flag1 = false;
                //int j = EnchantmentManager.getFireAspectEnchantmentLevel(this);
                int j = EnchantmentHelper.getFireAspect(player);

                /*if (entity instanceof EntityLiving && j > 0 && !entity.isBurning()) { */
                if (entity instanceof LivingEntity && j > 0 && !player.isOnFire()) {
                    // CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
                    EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(player.getBukkitEntity(), entity.getBukkitEntity(), 1);
                    org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

                    if (!combustEvent.isCancelled()) {
                        flag1 = true;
                        //entity.setOnFire(combustEvent.getDuration());
                        entity.setSecondsOnFire(combustEvent.getDuration(), false);
                    }
                    // CraftBukkit end
                }

                double d0 = entity.getDeltaMovement().x;
                double d1 = entity.getDeltaMovement().y;
                double d2 = entity.getDeltaMovement().z;
                //boolean flag2 = entity.damageEntity(DamageSource.playerAttack(this), f); TODO we might need to revisit
                boolean flag2 = entity.hurt(player.damageSources().playerAttack(player).critical(flag), f); // Paper - add critical damage API

                if (flag2) {
                    if (i > 0) {
                        //entity.g((double) (-MathHelper.sin(this.yaw * 3.1415927F / 180.0F) * (float) i * 0.5F), 0.1D, (double) (MathHelper.cos(this.yaw * 3.1415927F / 180.0F) * (float) i * 0.5F));
                        //this.motX *= 0.6D;
                        //this.motZ *= 0.6D;
                        float yaw = player.getYRot() * 0.017453292F; /* Math.DEGREES_TO_RADIANS */
                        entity.addDeltaMovement(new Vec3(
                            -Mth.sin(yaw) * (float) i * 0.5F,
                            0.1D,
                            Mth.cos(yaw) * (float) i * 0.5F
                        ));
                        player.setDeltaMovement(player.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                        player.setSprinting(false);
                    }

                    if (entity instanceof ServerPlayer entityPlayer && PlayerVelocity.velocityChanged(entityPlayer)) {
                        // CraftBukkit start - Add Velocity Event
                        boolean cancelled = false;
                        Player bPlayer = (Player) entity.getBukkitEntity();
                        org.bukkit.util.Vector velocity = new Vector( d0, d1, d2 );

                        PlayerVelocityEvent event = new PlayerVelocityEvent(bPlayer, velocity.clone());
                        //world.getServer().getPluginManager().callEvent(event);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            cancelled = true;
                        } else if (!velocity.equals(event.getVelocity())) {
                            bPlayer.setVelocity(velocity);
                        }

                        if (!cancelled) {
                            PlayerVelocity.sendCurrentAndSet(entityPlayer, d0, d1, d2);
                        }
                        // CraftBukkit end
                    }

                    if (flag) {
                        //this.b(entity);
                        player.crit(entity);
                    }

                    /* most likely magic crit? */
                    if (f1 > 0.0F) {
                        //this.c(entity);
                        player.magicCrit(entity);
                    }

                    if (f >= 18.0F) {
                        //this.b((Statistic) AchievementList.F); ignore overkill achievement
                    }

                    //this.p(entity);
                    player.setLastHurtMob(entity);
                    //if (entity instanceof EntityLiving) {
                    if (entity instanceof LivingEntity) {
                        EnchantmentManager.doPostHurtEffects((LivingEntity) entity, player);
                    }

                    EnchantmentManager.doPostDamageEffects(player, entity);
                    //ItemStack itemstack = this.bZ();
                    ItemStack itemstack = player.getMainHandItem();
                    Object object = entity;

                    /*if (entity instanceof EntityComplexPart) {
                        IComplex icomplex = ((EntityComplexPart) entity).owner;

                        if (icomplex instanceof EntityLiving) {
                            object = (EntityLiving) icomplex;
                        }
                    }*/
                    if (entity instanceof EnderDragonPart) {
                        object = ((EnderDragonPart) entity).parentMob;
                    }

                    //if (itemstack != null && object instanceof EntityLiving) {
                    if (!itemstack.isEmpty() && object instanceof LivingEntity) {
                        //itemstack.a((EntityLiving) object, this);
                        ItemStackHurt.hurtEnemy(itemstack, (LivingEntity) object, player);
                        // CraftBukkit - bypass infinite items; <= 0 -> == 0
                        /*if (itemstack.count == 0) {
                            this.ca();
                        }*/
                        if (itemstack.isEmpty()) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        }
                    }

                    //if (entity instanceof EntityLiving) {
                    if (entity instanceof LivingEntity) {
                        //this.a(StatisticList.w, Math.round(f * 10.0F));
                        player.awardStat(Stats.DAMAGE_DEALT, Math.round(f * 10.0F));
                        if (j > 0) {
                            // CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
                            EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(player.getBukkitEntity(), entity.getBukkitEntity(), j * 4);
                            org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

                            if (!combustEvent.isCancelled()) {
                                //entity.setOnFire(combustEvent.getDuration());
                                entity.setSecondsOnFire(combustEvent.getDuration(), false);
                            }
                            // CraftBukkit end
                        }
                    }

                    //this.applyExhaustion(world.spigotConfig.combatExhaustion); // Spigot - Change to use configurable value
                    player.causeFoodExhaustion(player.level().spigotConfig.combatExhaustion, EntityExhaustionEvent.ExhaustionReason.ATTACK); // CraftBukkit - EntityExhaustionEvent // Spigot - Change to use configurable value
                } else if (flag1) {
                    //entity.extinguish();
                    entity.clearFire();
                }
            }
        }
        /*}*/
    }
}
