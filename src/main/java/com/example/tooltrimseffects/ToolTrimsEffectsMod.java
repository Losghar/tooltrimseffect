package com.example.tooltrimseffects;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.HoeItem;
import net.minecraft.item.MaceItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ToolTrimsEffectsMod implements ModInitializer {
    public static final String MOD_ID = "tooltrimseffects";

    @id86240433 (@Override)
    public void onInitialize() {
        ModEffects.registerEffects();
        ToolMechanicsHandler.register();
        CombatMechanicsHandler.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 10 != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                TrimHelper.tickPassiveEffects(player, player.getMainHandStack());
                TrimHelper.tickPassiveEffects(player, player.getOffHandStack());

                if (player.getMainHandStack().getItem() instanceof HoeItem && TrimHelper.hasPattern(player.getMainHandStack(), "frost")) {
                    BlockPos center = player.getBlockPos();
                    for (BlockPos pos : BlockPos.iterate(center.add(-4, -1, -4), center.add(4, 1, 4))) {
                        if (player.getWorld().getBlockState(pos).isOf(Blocks.FARMLAND)) {
                            player.getWorld().setBlockState(pos, Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7), 2);
                        }
                    }
                }
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player && source.isOf(DamageTypes.FALL)) {
                if (player.getMainHandStack().getItem() instanceof MaceItem && TrimHelper.hasPattern(player.getMainHandStack(), "tracks")) {
                    return false;
                }
            }
            return true;
        });
    }
}
