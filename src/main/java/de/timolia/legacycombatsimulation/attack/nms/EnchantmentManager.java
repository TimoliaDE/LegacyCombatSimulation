package de.timolia.legacycombatsimulation.attack.nms;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.mutable.MutableFloat;

import static net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentId;
import static net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel;

public class EnchantmentManager {
    public static void doPostHurtEffects(LivingEntity user, Entity attacker) {
        EnchantmentHelper.doPostHurtEffects(user, attacker);
    }

    public static void doPostDamageEffects(LivingEntity user, Entity target) {
        EnchantmentHelper.doPostDamageEffects(user, target);
    }

    public static float getDamageBonus(ItemStack stack, MobType group) {
        MutableFloat mutableFloat = new MutableFloat();
        runIterationOnItem((enchantment, level) -> {
            mutableFloat.add(getEnchantmentDamageBonus(enchantment, level, group));
        }, stack);
        return mutableFloat.floatValue();
    }

    private static void runIterationOnItem(EnchantmentVisitor consumer, ItemStack stack) {
        if (!stack.isEmpty()) {
            ListTag listTag = stack.getEnchantmentTags();
            for(int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag = listTag.getCompound(i);
                BuiltInRegistries.ENCHANTMENT.getOptional(getEnchantmentId(compoundTag)).ifPresent((enchantment) -> {
                    consumer.accept(enchantment, getEnchantmentLevel(compoundTag));
                });
            }

        }
    }

    @FunctionalInterface
    interface EnchantmentVisitor {
        void accept(Enchantment enchantment, int level);
    }

    private static float getEnchantmentDamageBonus(Enchantment enchantment, int level, MobType group) {
        if (enchantment instanceof DamageEnchantment damageEnchantment)
            return damageEnchantment.type == 0 ?
                    //1.0F + (float) Math.max(0, level - 1) * 0.5F :
                    level * 1.25f : // 1.8 values
                    (damageEnchantment.type == 1 && group == MobType.UNDEAD ?
                            (float) level * 2.5F :
                            (damageEnchantment.type == 2 && group == MobType.ARTHROPOD ?
                                    (float) level * 2.5F :
                                    0.0F));
        return enchantment.getDamageBonus(level, group);
    }
}
