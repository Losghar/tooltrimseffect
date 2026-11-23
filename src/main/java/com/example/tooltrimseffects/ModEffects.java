package com.example.tooltrimseffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

public class ModEffects {

    public static class CustomStatusEffect extends StatusEffect {
        public CustomStatusEffect(StatusEffectCategory category, int color) {
            super(category, color);
        }
    }

    public static class BleedingEffect extends StatusEffect {
        public BleedingEffect() {
            super(StatusEffectCategory.HARMFUL, 0xAA0000);
        }

        @id86240433 (@Override)
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return duration % 20 == 0;
        }

        @id86240433 (@Override)
        public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (!entity.getWorld().isClient) {
                boolean isMoving = entity.getVelocity().horizontalLengthSquared() > 0.002;
                float damage = (1.5f + amplifier * 0.5f) * (isMoving ? 2.0f : 1.0f);
                entity.damage(entity.getDamageSources().magic(), damage);

                if (entity.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(new DustParticleEffect(new Vector3f(0.8f, 0.0f, 0.0f), 1.2f),
                            entity.getX(), entity.getBodyY(0.5), entity.getZ(),
                            10, 0.3, 0.4, 0.3, 0.05);
                }
            }
            return true;
        }
    }

    public static final RegistryEntry<StatusEffect> BLEEDING = register("bleeding", new BleedingEffect());

    public static final RegistryEntry<StatusEffect> FRAGILITY = register("fragility",
            new CustomStatusEffect(StatusEffectCategory.HARMFUL, 0x88CCFF));

    public static final RegistryEntry<StatusEffect> SWORD_MASTERY = register("sword_mastery",
            new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0xE0C068)
                    .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED,
                            Identifier.of(ToolTrimsEffectsMod.MOD_ID, "sword_speed"),
                            0.20, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final RegistryEntry<StatusEffect> TOOL_HASTE = register("tool_haste",
            new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700)
                    .addAttributeModifier(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED,
                            Identifier.of(ToolTrimsEffectsMod.MOD_ID, "tool_break_speed"),
                            0.25, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final RegistryEntry<StatusEffect> TRACKER_AGILITY = register("tracker_agility",
            new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0x8B7355)
                    .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                            Identifier.of(ToolTrimsEffectsMod.MOD_ID, "tracker_speed"),
                            0.18, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_STEP_HEIGHT,
                            Identifier.of(ToolTrimsEffectsMod.MOD_ID, "tracker_step"),
                            0.5, EntityAttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE,
                            Identifier.of(ToolTrimsEffectsMod.MOD_ID, "tracker_reach"),
                            1.5, EntityAttributeModifier.Operation.ADD_VALUE))


;

    public static final RegistryEntry<StatusEffect> OVERCHARGE_POWER = register("overcharge_power",
            new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF5500)
                    .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                            Identifier.of(ToolTrimsEffectsMod.MOD_ID, "overcharge_dmg"),
                            1.5, EntityAttributeModifier.Operation.ADD_VALUE));

    public static final RegistryEntry<StatusEffect> FROST_AURA = register("frost_aura",
            new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0x55FFFF)
                    .addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                            Identifier.of(ToolTrimsEffectsMod.MOD_ID, "frost_kb"),
                            0.25, EntityAttributeModifier.Operation.ADD_VALUE));

    public static final RegistryEntry<StatusEffect> IRON_TEMPER = register("iron_temper",
            new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0xCCCCCC)
                    .addAttributeModifier(EntityAttributes.GENERIC_ARMOR,
                            Identifier.of(ToolTrimsEffectsMod.MOD_ID, "iron_armor"),
                            2.0, EntityAttributeModifier.Operation.ADD_VALUE));

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(ToolTrimsEffectsMod.MOD_ID, name), effect);
    }

    public static void registerEffects() {}
}
