package de.timolia.legacycombatsimulation.attack;

import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import de.timolia.legacycombatsimulation.attack.debug.DebugProvider.DebugContext;
import de.timolia.legacycombatsimulation.attack.nms.EnchantmentManager;
import de.timolia.legacycombatsimulation.attack.nms.EntityHurt;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

public class AttackHandler {

    // Update on version change: net.minecraft.world.item
    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");


    public boolean handleAttack(ServerPlayer player, Entity damaged, DebugContext debugContext) {
        PacketHandler.process(player, damaged, debugContext, () -> {
            serverPlayerAttack(player, damaged, debugContext);
        });
        return false;
    }

    private void serverPlayerAttack(ServerPlayer player, Entity target, DebugContext debugContext) {
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            player.setCamera(target);
            debugContext.fail("Spectator");
        } else {
            playerAttack(player, target, debugContext);
        }
    }

    private void playerAttack(ServerPlayer player, Entity entity, DebugContext debugContext) {
        /*if (entity.aD()) { irrelevant */
        if (!entity.skipAttackInteraction(player)) { /* !entity.l(this) */
            boolean oldItemDamageValues = TargetRegistry.instance().isEnabled(player.getBukkitEntity(), SimulationTarget.ITEM_DAMAGE_VALUES);
            //float f = (float) player.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
            float f;
            if (oldItemDamageValues)
                f = calculateAttributeValue(player.getAttribute(Attributes.ATTACK_DAMAGE), player.getMainHandItem());
            else
                f = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);


            //byte b0 = 0; never assigned - might be decopiler leftover
            float f1 = 0.0F;

            /*if (entity instanceof EntityLiving) {
                f1 = EnchantmentManager.a(this.bA(), ((EntityLiving) entity).getMonsterType());
            } else {
                f1 = EnchantmentManager.a(this.bA(), EnumMonsterType.UNDEFINED);
            }*/
            if (oldItemDamageValues && entity instanceof net.minecraft.world.entity.player.Player)
                f1 = EnchantmentManager.getDamageBonus(player.getMainHandItem(), ((LivingEntity) entity).getMobType());
            else if (entity instanceof LivingEntity) {
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
                //boolean flag2 = entity.hurt(player.damageSources().playerAttack(player).critical(flag), f); // Paper - add critical damage API
                boolean flag2 = EntityHurt.hurtEntity(entity, player.damageSources().playerAttack(player).critical(flag), f, debugContext); // Paper - add critical damage API

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
            } else {
                debugContext.fail("No damage f=%s f1=$s", f, f1);
            }
        } else {
            debugContext.fail("skipAttackInteraction()");
        }
        /*}*/
    }

    private static float calculateAttributeValue(AttributeInstance attributeInstance, ItemStack itemInHand) {
        if (attributeInstance == null)
            return 0;
        double d = attributeInstance.getBaseValue();
        for (AttributeModifier attributeModifier : attributeInstance.getModifiers(AttributeModifier.Operation.ADDITION)) {
            if (attributeModifier.getId().equals(BASE_ATTACK_DAMAGE_UUID)) { // ItemStack Attack Modifiers
                d += oldDamageByItem(itemInHand);
            } else
                d += attributeModifier.getAmount();
        }

        double e = d;

        for (AttributeModifier attributeModifier2 : attributeInstance.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
            e += d * attributeModifier2.getAmount();
        }

        for (AttributeModifier attributeModifier3 : attributeInstance.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            e *= 1.0D + attributeModifier3.getAmount();
        }

        return (float) attributeInstance.getAttribute().sanitizeValue(e);
    }

    private static float oldDamageByItem(ItemStack itemStack) {
        if (itemStack == null)
            return 1;
        Item item = itemStack.getItem();
        if (item instanceof TridentItem)
            return 7; // via backwards displays it as diamond sword
        float material = 0;
        float tool = 0;
        if (item instanceof SwordItem swordItem) {
            Tier tier = swordItem.getTier();
            tool = 4;
            if (tier instanceof Tiers enumTier) { // Tier has only one implementation
                switch (enumTier) {
                    case GOLD, WOOD -> material = 0;
                    case STONE -> material = 1;
                    case IRON -> material = 2;
                    case DIAMOND, NETHERITE -> material = 3;
                }
            }
        }
        if (item instanceof DiggerItem diggerItem) {
            Tier tier = diggerItem.getTier();
            if (tier instanceof Tiers enumTier) { // Tier has only one implementation
                switch (enumTier) {
                    case GOLD, WOOD -> material = 0;
                    case STONE -> material = 1;
                    case IRON -> material = 2;
                    case DIAMOND, NETHERITE -> material = 3;
                }
            }
            if (diggerItem instanceof ShovelItem)
                tool = 1;
            else if (diggerItem instanceof PickaxeItem)
                tool = 2;
            else if (diggerItem instanceof AxeItem)
                tool = 3;
        }
        return tool + material;
    }
}
