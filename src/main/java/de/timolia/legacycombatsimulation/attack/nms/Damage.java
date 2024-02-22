package de.timolia.legacycombatsimulation.attack.nms;

import com.google.common.base.Function;
import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import de.timolia.legacycombatsimulation.attack.debug.DebugProvider.DebugContext;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Tag;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

public class Damage {
    /* 1.8 signature EntityHuman#d(final DamageSource damagesource, float f) */
    public static boolean damageEntity0(LivingEntity entity, DamageSource damagesource, float f,
        DebugContext debugContext) {
        if (!entity.isInvulnerableTo(damagesource)) {
            final boolean human = entity instanceof Player;
            float originalDamage = f;
            Function<Double, Double> hardHat = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if (damagesource.is(DamageTypeTags.DAMAGES_HELMET) && !entity.getItemBySlot(
                        EquipmentSlot.HEAD).isEmpty()) {
                        return -(f - (f * 0.75F));

                    }
                    return -0.0;
                }
            };
            float hardHatModifier = hardHat.apply((double) f).floatValue();
            f += hardHatModifier;

            Function<Double, Double> blocking = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if (human) {
                        if (!damagesource.is(DamageTypeTags.BYPASSES_ARMOR) && /*entity.isBlocking()*/ isEntityLegacyBlocking(entity) && f > 0.0F) {
                            return -(f - ((1.0F + f) * 0.5F));
                        }
                    }
                    return -0.0;
                }
            };
            float blockingModifier = blocking.apply((double) f).floatValue();
            f += blockingModifier;

            Function<Double, Double> armor = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(f - getDamageAfterArmorAbsorb(entity, damagesource.is(DamageTypeTags.BYPASSES_ARMOR), f.floatValue(), SimulationTarget.ITEM_DAMAGE_VALUES));
                }
            };
            float armorModifier = armor.apply((double) f).floatValue();
            f += armorModifier;

            Function<Double, Double> resistance = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if (!damagesource.is(DamageTypeTags.BYPASSES_EFFECTS) && entity.hasEffect(
                        MobEffects.DAMAGE_RESISTANCE) && !damagesource.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
                        int i = (entity.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                        int j = 25 - i;
                        float f1 = f.floatValue() * (float) j;
                        return -(f - (f1 / 25.0F));
                    }
                    return -0.0;
                }
            };
            float resistanceModifier = resistance.apply((double) f).floatValue();
            f += resistanceModifier;

            Function<Double, Double> magic = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(f - getDamageAfterMagicAbsorb(entity, damagesource, f.floatValue()));
                }
            };
            float magicModifier = magic.apply((double) f).floatValue();
            f += magicModifier;

            Function<Double, Double> absorption = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(Math.max(f - Math.max(f - entity.getAbsorptionAmount(), 0.0F), 0.0F));
                }
            };
            float absorptionModifier = absorption.apply((double) f).floatValue();

            EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(entity, damagesource, originalDamage, hardHatModifier, blockingModifier, armorModifier, resistanceModifier, magicModifier, absorptionModifier, hardHat, blocking, armor, resistance, magic, absorption);
            if (event.isCancelled()) {
                debugContext.fail("bukkit damage event cancelled");
                return false;
            }

            f = (float) event.getFinalDamage();

            // This is in LivingEntity, but only implemented by players
            if (entity instanceof Player player) {
                // Apply damage to helmet
                if (damagesource.is(DamageTypeTags.DAMAGES_HELMET) && !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                    int damage = (int) (event.getDamage() * 4.0F + player.getRandom().nextFloat() * event.getDamage() * 2.0F);
                    player.getInventory().hurtArmor(damagesource, damage, Inventory.HELMET_SLOT_ONLY);
                }

                // Apply damage to armor
                if (!damagesource.is(DamageTypeTags.BYPASSES_ARMOR)) {
                    float armorDamage = (float) (event.getDamage() + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.HARD_HAT));
                    player.getInventory().hurtArmor(damagesource, armorDamage, Inventory.ALL_ARMOR_SLOTS);
                }
            }

            // Resistance
            if (event.getDamage(DamageModifier.RESISTANCE) < 0) {
                float f3 = (float) -event.getDamage(DamageModifier.RESISTANCE);
                if (f3 > 0.0F && f3 < 3.4028235E37F) {
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).awardStat(Stats.DAMAGE_RESISTED, Math.round(f3 * 10.0F));
                    } else if (damagesource.getEntity() instanceof ServerPlayer) {
                        ((ServerPlayer) damagesource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f3 * 10.0F));
                    }
                }
            }

            absorptionModifier = (float) -event.getDamage(DamageModifier.ABSORPTION);
            entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount() - absorptionModifier, 0.0F));
            float f2 = absorptionModifier;

            if (f2 > 0.0F && f2 < 3.4028235E37F && entity instanceof net.minecraft.world.entity.player.Player) {
                ((net.minecraft.world.entity.player.Player) entity).awardStat(Stats.DAMAGE_ABSORBED, Math.round(f2 * 10.0F));
            }
            if (f2 > 0.0F && f2 < 3.4028235E37F) {
                Entity entity2 = damagesource.getEntity();

                if (entity2 instanceof ServerPlayer) {
                    ServerPlayer entityplayer = (ServerPlayer) entity2;

                    entityplayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f2 * 10.0F));
                }
            }

            if (f > 0 || !human) {
                if (human) {
                    // PAIL: Be sure to drag all this code from the EntityHuman subclass each update.
                    ((net.minecraft.world.entity.player.Player) entity).causeFoodExhaustion(damagesource.getFoodExhaustion(), org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.DAMAGED); // CraftBukkit - EntityExhaustionEvent
                    if (f < 3.4028235E37F) {
                        ((net.minecraft.world.entity.player.Player) entity).awardStat(Stats.DAMAGE_TAKEN, Math.round(f * 10.0F));
                    }
                }
                // CraftBukkit end

                entity.getCombatTracker().recordDamage(damagesource, f);
                debugContext.info("finalDamage=%.2f", f);
                entity.setHealth(entity.getHealth() - f);
                // CraftBukkit start
                if (!human) {
                    entity.setAbsorptionAmount(entity.getAbsorptionAmount() - f);
                }
                entity.gameEvent(GameEvent.ENTITY_DAMAGE);

                return true;
            } else {
                // Duplicate triggers if blocking
                if (event.getDamage(DamageModifier.BLOCKING) < 0) {
                    if (entity instanceof ServerPlayer) {
                        CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer) entity, damagesource, originalDamage, f, true); // Paper - fix taken/dealt param order
                        f2 = (float) -event.getDamage(DamageModifier.BLOCKING);
                        if (f2 > 0.0F && f2 < 3.4028235E37F) {
                            ((ServerPlayer) entity).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(originalDamage * 10.0F));
                        }
                    }

                    if (damagesource.getEntity() instanceof ServerPlayer) {
                        CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer) damagesource.getEntity(), entity, damagesource, originalDamage, f, true); // Paper - fix taken/dealt param order
                    }
                    debugContext.fail("BLOCKING modifier");
                    return false;
                } else {
                    return originalDamage > 0;
                }
                // CraftBukkit end
            }
        }
        debugContext.fail("likely isInvulnerableTo");
        return false; // CraftBukkit
    }


    public static float getDamageAfterArmorAbsorb(LivingEntity entity, boolean bypassArmor, float amount, SimulationTarget simulationTarget) {

        boolean oldItemValues = false;
        if (entity instanceof ServerPlayer serverPlayer) {
            oldItemValues = TargetRegistry.instance().isEnabled(serverPlayer.getBukkitEntity(), simulationTarget);
        }
        if (oldItemValues) {
            if (!bypassArmor) {
                int defenseValue = 25 - getTotalDefenseValue(entity);
                amount *= defenseValue / 25.0F;
            }
        } else {
            if (!bypassArmor) {
                // this.hurtArmor(damagesource, f); // CraftBukkit - Moved into damageEntity0(DamageSource, float)
                amount = CombatRules.getDamageAfterAbsorb(amount, (float) entity.getArmorValue(), (float) entity.getAttributeValue(
                        Attributes.ARMOR_TOUGHNESS));
            }
        }

        return amount;
    }

    protected static float getDamageAfterMagicAbsorb(LivingEntity entity, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return amount;
        } else {
            int i;

            // CraftBukkit - Moved to damageEntity0(DamageSource, float)


            if (amount <= 0.0F) {
                return 0.0F;
            } else if (source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
                return amount;
            } else {
                i = EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source);
                if (i > 20) {
                    i = 20;
                }

                if (i > 0) {
                    float j = 25 - i;
                    float f1 = amount * j;
                    amount = f1 / 25.0F;
                }

                return amount;
            }
        }
    }

    protected static boolean isEntityLegacyBlocking(LivingEntity livingEntity) {
        return Tag.ITEMS_SWORDS.isTagged(livingEntity.getBukkitLivingEntity().getActiveItem().getType());
    }

    private static int getTotalDefenseValue(LivingEntity livingEntity) {
        int i = 0;
        for (ItemStack itemStack : livingEntity.getArmorSlots()) {
            if (itemStack != null && itemStack.getItem() instanceof ArmorItem armorItem) {
                int l = armorItem.getDefense(); // values are the same for 1.8/1.20
                i += l;
            }
        }
        return i;
    }
}
