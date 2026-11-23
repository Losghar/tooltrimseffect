package com.example.tooltrimseffects;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

public class TrimHelper {

    public static ArmorTrim getTrim(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        return stack.get(DataComponentTypes.TRIM);
    }

    public static String getPattern(ItemStack stack) {
        ArmorTrim trim = getTrim(stack);
        if (trim == null) return "";
        return trim.getPattern().getKey().map(k -> k.getValue().getPath()).orElse("");
    }

    public static String getMaterial(ItemStack stack) {
        ArmorTrim trim = getTrim(stack);
        if (trim == null) return "";
        return trim.getMaterial().getKey().map(k -> k.getValue().getPath()).orElse("");
    }

    public static boolean hasPattern(ItemStack stack, String patternName) {
        return getPattern(stack).contains(patternName);
    }

    public static boolean hasMaterial(ItemStack stack, String materialName) {
        return getMaterial(stack).contains(materialName);
    }

    public static int getAmplifier(ItemStack stack) {
        return hasMaterial(stack, "netherite") ? 1 : 0;
    }

    public static int scaleDuration(ItemStack stack, int baseTicks) {
        return hasMaterial(stack, "diamond") ? (int) (baseTicks * 1.5) : baseTicks;
    }

    public static double getRadiusBonus(ItemStack stack) {
        return hasMaterial(stack, "amethyst") ? 1.5 : 0.0;
    }

    public static int scaleCooldown(ItemStack stack, int baseTicks) {
        return hasMaterial(stack, "redstone") ? (int) (baseTicks * 0.7) : baseTicks;
    }

    public static void tickPassiveEffects(ServerPlayerEntity player, ItemStack stack) {
        String pattern = getPattern(stack);
        String material = getMaterial(stack);
        if (pattern.isEmpty()) return;

        int amp = getAmplifier(stack);
        Item item = stack.getItem();

        if (pattern.contains("linear")) {
            if (item instanceof SwordItem || item instanceof MaceItem) {
                player.addStatusEffect(new StatusEffectInstance(ModEffects.SWORD_MASTERY, 25, amp, true, false, true));
            } else if (item instanceof PickaxeItem || item instanceof ShovelItem || item instanceof HoeItem) {
                player.addStatusEffect(new StatusEffectInstance(ModEffects.TOOL_HASTE, 25, amp, true, false, true));
            }
        } else if (pattern.contains("tracks")) {
            player.addStatusEffect(new StatusEffectInstance(ModEffects.TRACKER_AGILITY, 25, amp, true, false, true));
            if (item instanceof TridentItem && player.isTouchingWater()) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 25, amp, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 25, amp, true, false, true));
            }
            if (item instanceof MaceItem) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 25, 1 + amp, true, false, true));
            }
        } else if (pattern.contains("charge")) {
            if (item instanceof SwordItem || item instanceof AxeItem) {
                player.addStatusEffect(new StatusEffectInstance(ModEffects.OVERCHARGE_POWER, 25, amp, true, false, true));
            } else if (item instanceof HoeItem) {
                player.addStatusEffect(new StatusEffectInstance(ModEffects.TOOL_HASTE, 25, amp, true, false, true));
            }
        } else if (pattern.contains("frost")) {
            player.addStatusEffect(new StatusEffectInstance(ModEffects.FROST_AURA, 25, amp,


true, false, true));
            if (item instanceof PickaxeItem && (player.isOnFire() || player.isInLava())) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, true, false, true));
            }
            if (item instanceof AxeItem) {
                player.addStatusEffect(new StatusEffectInstance(ModEffects.TOOL_HASTE, 25, amp, true, false, true));
            }
        }

        if (material.contains("emerald")) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 25, 0, true, false, true));
        } else if (material.contains("iron")) {
            player.addStatusEffect(new StatusEffectInstance(ModEffects.IRON_TEMPER, 25, 0, true, false, true));
        } else if (material.contains("copper") && player.getWorld().isRaining()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 25, 0, true, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 25, 0, true, false, true));
        } else if (material.contains("gold")) {
            double radius = 5.0 + getRadiusBonus(stack);
            Box box = player.getBoundingBox().expand(radius);
            List<ItemEntity> items = player.getWorld().getEntitiesByClass(ItemEntity.class, box, e -> !e.cannotPickup());
            for (ItemEntity itemEntity : items) {
                itemEntity.setVelocity(player.getPos().add(0, 0.5, 0).subtract(itemEntity.getPos()).normalize().multiply(0.35));
            }
        }
    }

    public static void onItemAction(PlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) return;
        String material = getMaterial(stack);
        if (material.isEmpty()) return;

        if (material.contains("lapis") && stack.isDamaged() && player.totalExperience > 0) {
            if (player.getRandom().nextFloat() < 0.20f) {
                player.addExperience(-1);
                stack.setDamage(Math.max(0, stack.getDamage() - 2));
            }
        }
        if (material.contains("iron") && stack.isDamaged()) {
            if (player.getRandom().nextFloat() < 0.25f) {
                stack.setDamage(Math.max(0, stack.getDamage() - 1));
            }
        }
    }
}
