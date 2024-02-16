package de.timolia.legacycombatsimulation.attack.nms;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemStackHurt {
    /* TODO
    * - saddles
    * - hoe was not an tool
    */
    public static void hurtEnemy(ItemStack itemStack, LivingEntity target, Player attacker) {
        Item item = itemStack.getItem();
        if (item.hurtEnemy(itemStack, target, attacker)) {
            attacker.awardStat(Stats.ITEM_USED.get(item));
        }
    }
}
