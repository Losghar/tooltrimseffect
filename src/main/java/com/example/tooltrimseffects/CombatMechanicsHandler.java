package com.example.tooltrimseffects;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.*;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import java.util.*;

public class CombatMechanicsHandler {
    private static final Map<UUID, Integer> HITS = new HashMap<>();
    private static final Map<UUID, Integer> SHIELD = new HashMap<>();

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof PersistentProjectileEntity p) || !(p.getOwner() instanceof ServerPlayerEntity pl)) return;
            ItemStack w = TrimHelper.getPattern(pl.getMainHandStack()).isEmpty() ? pl.getOffHandStack() : pl.getMainHandStack();
            String pat = TrimHelper.getPattern(w);
            if (pat.contains("linear")) { p.setNoGravity(true); p.setVelocity(p.getVelocity().multiply(1.35)); }
            else if (pat.contains("charge") && !(p instanceof TridentEntity)) p.setDamage(p.getDamage() * 1.3);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient || !(entity instanceof LivingEntity target)) return ActionResult.PASS;
            ItemStack st = player.getMainHandStack();
            TrimHelper.onItemAction(player, st);
            String pat = TrimHelper.getPattern(st);
            if (TrimHelper.getMaterial(st).contains("quartz") && target.getType().isIn(EntityTypeTags.UNDEAD)) target.damage(player.getDamageSources().playerAttack(player), 3.0f);
            if (target.hasStatusEffect(ModEffects.FRAGILITY)) { target.damage(player.getDamageSources().playerAttack(player), 3.5f); target.removeStatusEffect(ModEffects.FRAGILITY); }
            if (st.getItem() instanceof SwordItem && pat.contains("linear")) {
                int c = HITS.getOrDefault(player.getUuid(), 0) + 1;
                HITS.put(player.getUuid(), c >= 3 ? 0 : c);
                if (c >= 3) { target.damage(player.getDamageSources().playerAttack(player), 4.0f); player.addCritParticles(target); }
            }
            return ActionResult.PASS;
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, dmg, blocked) -> {
            if (!(entity.getWorld() instanceof ServerWorld w)) return;
            if (blocked && entity instanceof ServerPlayerEntity def && def.getActiveItem().getItem() instanceof ShieldItem) {
                ItemStack sh = def.getActiveItem();
                String pat = TrimHelper.getPattern(sh);
                if (pat.contains("linear") && source.getAttacker() instanceof LivingEntity att && def.getItemUseTime() <= 12) {
                    att.damage(def.getDamageSources().thorns(def), base * 0.5f);
                    att.takeKnockback(1.2, def.getX() - att.getX(), def.getZ() - att.getZ());
                } else if (pat.contains("frost") && source.getAttacker() instanceof LivingEntity att) {
                    int dur = TrimHelper.scaleDuration(sh, 80);
                    att.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, dur, 2));
                    att.setFrozenTicks(att.getMinFreezeDamageTicks() + dur);
                } else if (pat.contains("charge")) {
                    int ch = SHIELD.getOrDefault(def.getUuid(), 0) + 1;
                    SHIELD.put(def.getUuid(), ch >= 3 ? 0 : ch);
                    if (ch >= 3) for (LivingEntity m : w.getEntitiesByClass(LivingEntity.class, def.getBoundingBox().expand(5.0 + TrimHelper.getR


adiusBonus(sh)), e -> e != def))
                        m.takeKnockback(1.8, def.getX() - m.getX(), def.getZ() - m.getZ());
                }
                return;
            }
            if (!(source.getAttacker() instanceof ServerPlayerEntity pl)) return;
            ItemStack wp = pl.getMainHandStack();
            if (source.getSource() instanceof PersistentProjectileEntity && !(wp.getItem() instanceof RangedWeaponItem) && !(wp.getItem() instanceof TridentItem)) wp = pl.getOffHandStack();
            String pat = TrimHelper.getPattern(wp), mat = TrimHelper.getMaterial(wp);
            Item it = wp.getItem();
            int dur = TrimHelper.scaleDuration(wp, 80);
            double rad = TrimHelper.getRadiusBonus(wp);

            if (mat.contains("copper") && w.random.nextFloat() < 0.15f) {
                List<LivingEntity> ch = w.getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(3.5 + rad), e -> e != entity && e != pl);
                for (int i = 0; i  e != entity && e != pl)) m.damage(pl.getDamageSources().playerAttack(pl), dmg * 0.5f);
                else if (pat.contains("frost")) { entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, dur, 2)); entity.addStatusEffect(new StatusEffectInstance(ModEffects.FRAGILITY, dur, 0)); }
            } else if (it instanceof RangedWeaponItem) {
                if (pat.contains("tracks")) entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, dur + 20, 0));
                else if (pat.contains("frost")) for (LivingEntity m : w.getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(2.5 + rad), e -> e != pl)) { m.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, dur, 2)); m.setFrozenTicks(m.getMinFreezeDamageTicks() + dur); }
            } else if (it instanceof TridentItem) {
                if (pat.contains("charge") && source.getSource() instanceof TridentEntity) { LightningEntity l = EntityType.LIGHTNING_BOLT.create(w); if (l != null) { l.refreshPositionAfterTeleport(entity.getPos()); w.spawnEntity(l); } }
                else if (pat.contains("frost")) { entity.setFrozenTicks(entity.getMinFreezeDamageTicks() + dur); BlockPos u = entity.getBlockPos().down(); if (w.getBlockState(u).isOf(Blocks.WATER)) w.setBlockState(u, Blocks.FROSTED_ICE.getDefaultState()); }
            } else if (it instanceof MaceItem && pl.fallDistance > 1.5f) {
                if (pat.contains("charge")) for (LivingEntity m : w.getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(4.0 + rad), e -> e != entity && e != pl)) { m.damage(pl.getDamageSources().playerAttack(pl), dmg * 0.5f); m.addVelocity(0, 0.6, 0); }
                else if (pat.contains("frost")) for (LivingEntity m : w.getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(3.0 + rad), e -> e != pl)) { m.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, dur, 3)); m.setFrozenTicks(m.getMinFreezeDamageTicks() + dur); }
            }
        });
    }
}
