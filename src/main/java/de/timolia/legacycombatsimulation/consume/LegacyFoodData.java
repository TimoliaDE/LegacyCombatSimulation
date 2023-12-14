package de.timolia.legacycombatsimulation.consume;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;

public class LegacyFoodData extends FoodData {

    private int lastFoodLevel1 = 20;
    private int tickTimer1;

    public LegacyFoodData(Player entityhuman, boolean copyValues) {
        super(entityhuman);
        if (copyValues) {
            FoodData toOverrideData = entityhuman.getFoodData();
            this.lastFoodLevel1 = toOverrideData.getLastFoodLevel();
            this.foodLevel = toOverrideData.getFoodLevel();
            this.saturationLevel = toOverrideData.getSaturationLevel();
            this.exhaustionLevel = toOverrideData.getExhaustionLevel();
        }
    }

    @Override
    public void tick(Player player) {
        Difficulty enumdifficulty = player.level().getDifficulty();
        this.lastFoodLevel1 = this.foodLevel;
        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F;
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
            } else if (enumdifficulty != Difficulty.PEACEFUL) {
                // CraftBukkit start
                org.bukkit.event.entity.FoodLevelChangeEvent event = org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.callFoodLevelChangeEvent(player, Math.max(this.foodLevel - 1, 0));

                if (!event.isCancelled()) {
                    this.foodLevel = event.getFoodLevel();
                }

                ((ServerPlayer) player).connection.send(new ClientboundSetHealthPacket(((ServerPlayer) player).getBukkitEntity().getScaledHealth(), this.foodLevel, this.saturationLevel));
                // CraftBukkit end
            }
        }

        boolean flag = player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);

        /*if (flag && this.saturationLevel > 0.0F && player.isHurt() && this.foodLevel >= 20) {
            ++this.tickTimer1;
            if (this.tickTimer1 >= this.saturatedRegenRate) { // CraftBukkit
                float f = Math.min(this.saturationLevel, 6.0F);

                player.heal(f / 6.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED, true); // CraftBukkit - added RegainReason // Paper - This is fast regen
                // this.addExhaustion(f); CraftBukkit - EntityExhaustionEvent
                player.causeFoodExhaustion(f, org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.REGEN); // CraftBukkit - EntityExhaustionEvent
                this.tickTimer1 = 0;
            }
        } else*/ if (flag && this.foodLevel >= 18 && player.isHurt()) {
            ++this.tickTimer1;
            if (this.tickTimer1 >= /*this.unsaturatedRegenRate*/ 80) { // CraftBukkit - add regen rate manipulation
                player.heal(1.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED); // CraftBukkit - added RegainReason
                // this.a(6.0F); CraftBukkit - EntityExhaustionEvent
                player.causeFoodExhaustion(player.level().spigotConfig.regenExhaustion, org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.REGEN); // CraftBukkit - EntityExhaustionEvent // Spigot - Change to use configurable value
                this.tickTimer1 = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.tickTimer1;
            if (this.tickTimer1 >= /*this.starvationRate*/ 80) { // CraftBukkit - add regen rate manipulation
                if (player.getHealth() > 10.0F || enumdifficulty == Difficulty.HARD || player.getHealth() > 1.0F && enumdifficulty == Difficulty.NORMAL) {
                    player.hurt(player.damageSources().starve(), 1.0F);
                }

                this.tickTimer1 = 0;
            }
        } else {
            this.tickTimer1 = 0;
        }
    }

    public void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains("foodLevel", 99)) {
            this.foodLevel = nbt.getInt("foodLevel");
            this.tickTimer1 = nbt.getInt("foodTickTimer");
            this.saturationLevel = nbt.getFloat("foodSaturationLevel");
            this.exhaustionLevel = nbt.getFloat("foodExhaustionLevel");
        }

    }

    public void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("foodLevel", this.foodLevel);
        nbt.putInt("foodTickTimer", this.tickTimer1);
        nbt.putFloat("foodSaturationLevel", this.saturationLevel);
        nbt.putFloat("foodExhaustionLevel", this.exhaustionLevel);
    }

    @Override
    public int getLastFoodLevel() {
        return this.lastFoodLevel1;
    }
}
