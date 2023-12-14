package de.timolia.legacycombatsimulation.consume;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import de.timolia.legacycombatsimulation.api.SimulationTarget;
import de.timolia.legacycombatsimulation.api.SimulationTargetChangeEvent;
import de.timolia.legacycombatsimulation.api.TargetRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.food.FoodData;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spigotmc.SpigotWorldConfig;

import java.lang.reflect.Field;

public class HungerSystem implements Listener {

    private String foodMetaReflectionKey = null;

    @EventHandler
    public void onSimulationStatus(SimulationTargetChangeEvent event) {
        if (!event.doesTargetApply(SimulationTarget.HUNGER_AND_REGENERATION))
            return;
        if (event.gotEnabled()) {
            setLegacyFoodInfo(event.getPlayer());
        } else {
            resetLegacyFoodInfo(event.getPlayer());
        }
    }

    // Death resets FoodData
    @EventHandler
    public void onPostDeath(PlayerPostRespawnEvent event) {
        if (!TargetRegistry.instance().isEnabled(event.getPlayer(), SimulationTarget.HUNGER_AND_REGENERATION))
            return;
        setLegacyFoodInfo(event.getPlayer());
    }

    @EventHandler
    public void onExhaustion(EntityExhaustionEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (!TargetRegistry.instance().isEnabled(player, SimulationTarget.HUNGER_AND_REGENERATION))
            return;

        SpigotWorldConfig spigotConfig = ((CraftWorld)event.getEntity().getWorld()).getHandle().spigotConfig;
        float amount = switch (event.getExhaustionReason()) {
            case BLOCK_MINED -> 0.025F;
            case HUNGER_EFFECT -> quotient(event.getExhaustion(), 0.025F, 0.005F);
            case ATTACK -> 0.3F;
            case REGEN -> 3F;
            case DAMAGED -> exhaustionByLastDamage(player);
            case UNKNOWN -> 0.0F;

            // dependent on movementLength
            // underwater movement have apparently all the same config values. Keep them separated, if this changes in the future
            case SWIM -> quotient(event.getExhaustion(), 0.015F, spigotConfig.swimMultiplier);
            case WALK_UNDERWATER -> quotient(event.getExhaustion(), 0.015F, spigotConfig.swimMultiplier);
            case WALK_ON_WATER -> quotient(event.getExhaustion(), 0.015F, spigotConfig.swimMultiplier);

            case SPRINT -> quotient(event.getExhaustion(), 0.099999994F, spigotConfig.sprintMultiplier);
            case WALK -> quotient(event.getExhaustion(), 0.01F, spigotConfig.otherMultiplier);
            case CROUCH -> 0;
            case JUMP -> 0.2F; // taken from hunger.sprint-exhaustion from 1.8 spigot config. This config value might be mislabeled...
            case JUMP_SPRINT -> 0.8F; // same as in JUMP // 0.8 is a lot! Tested it, and it seems comparable to 1.8
        };
        event.setExhaustion(amount);
    }

    private float quotient(float value, float oldCombatValue, float newCombatValue) {
        if (newCombatValue == 0)
            return 0;
        return value * (oldCombatValue / newCombatValue);
    }

    private float exhaustionByLastDamage(Player player) {
        if (player.getLastDamageCause() == null)
            return 0;

        // if damage ignores armor, or is starvation, no exhaustion
        switch (player.getLastDamageCause().getCause()) {
            case STARVATION, WORLD_BORDER, SUFFOCATION, FALL, FIRE_TICK, DROWNING, VOID, MAGIC, WITHER -> {
                return 0;
            }
        }

        // otherwise, always 0.3
        return 0.3F;
    }

    // In case foodInfo gets reset by some plugin
    @EventHandler
    public void onRegenerate(EntityRegainHealthEvent event) {
        if (event.isCancelled() || !event.isFastRegen())
            return;
        if (!(event.getEntity() instanceof Player player))
            return;
        if (!TargetRegistry.instance().isEnabled(player, SimulationTarget.HUNGER_AND_REGENERATION))
            return;
        event.setCancelled(true);
        System.out.println("Legacy Combat Simulation: Fast Regeneration?!");
        setLegacyFoodInfo(player);
    }


    private void setLegacyFoodInfo(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        setFoodData(serverPlayer, new LegacyFoodData(serverPlayer, true));
    }

    private void resetLegacyFoodInfo(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        FoodData oldFoodData = serverPlayer.getFoodData();
        if (!(oldFoodData instanceof LegacyFoodData))
            return;
        FoodData newFoodData = new FoodData(serverPlayer);
        newFoodData.exhaustionLevel = oldFoodData.exhaustionLevel;
        newFoodData.foodLevel = oldFoodData.getFoodLevel();
        newFoodData.saturationLevel = oldFoodData.getSaturationLevel();
        // can't set lastFoodLevel directly and not doing it by reflection
        setFoodData(serverPlayer, newFoodData);
    }

    private void setFoodData(net.minecraft.world.entity.player.Player player, FoodData foodData) {
        if (foodMetaReflectionKey == null)
            setReflectionKey();
        try {
            Field foodMetaDataField = net.minecraft.world.entity.player.Player.class.getDeclaredField(foodMetaReflectionKey);
            foodMetaDataField.setAccessible(true);
            foodMetaDataField.set(player, foodData);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setReflectionKey() {
        for (Field field : net.minecraft.world.entity.player.Player.class.getDeclaredFields()) {
            if (field.getType() == FoodData.class) {
                System.out.println("LegacyCombat: Initially setting foodMetaReflectionKey to: " + field.getName());
                foodMetaReflectionKey = field.getName();
                return;
            }
        }
    }
}
