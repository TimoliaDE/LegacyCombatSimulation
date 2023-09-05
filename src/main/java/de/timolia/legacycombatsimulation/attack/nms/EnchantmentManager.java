package de.timolia.legacycombatsimulation.attack.nms;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantmentManager {
    public static void doPostHurtEffects(LivingEntity user, Entity attacker) {
        EnchantmentHelper.doPostHurtEffects(user, attacker);
    }

    public static void doPostDamageEffects(LivingEntity user, Entity target) {
        EnchantmentHelper.doPostDamageEffects(user, target);
    }
}
