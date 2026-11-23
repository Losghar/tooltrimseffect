package com.example.tooltrimseffects;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ToolMechanicsHandler {

    private static final Set<BlockPos> BREAKING_BLOCKS = new HashSet<>();

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !(world instanceof ServerWorld serverWorld)) return;
            if (BREAKING_BLOCKS.contains(pos)) return;

            ItemStack stack = player.getMainHandStack();
            if (stack.isEmpty()) return;

            TrimHelper.onItemAction(player, stack);

            String pattern = TrimHelper.getPattern(stack);
            String material = TrimHelper.getMaterial(stack);
            Item item = stack.getItem();

            if (material.contains("emerald") && state.isIn(BlockTags.PICKAXE_MINEABLE) && state.getBlock().getHardness() > 2.0f) {
                if (world.random.nextFloat() < 0.35f) {
                    ExperienceOrbEntity.spawn(serverWorld, pos.toCenterPos(), 2);
                }
            }

            if (pattern.isEmpty() || player.isSneaking()) return;

            if (item instanceof PickaxeItem) {
                if (pattern.contains("charge")) {
                    break3x3(serverWorld, player, pos, stack, BlockTags.PICKAXE_MINEABLE);
                } else if (pattern.contains("tracks") && state.getBlock().toString().contains("ore")) {
                    highlightOres(serverWorld, pos, state.getBlock(), (int) (8 + TrimHelper.getRadiusBonus(stack)));
                } else if (pattern.contains("frost") && state.getBlock().toString().contains("ore")) {
                    if (world.random.nextFloat() < 0.20f) {
                        Block.dropStacks(state, serverWorld, pos, blockEntity, player, stack);
                    }
                }
            } else if (item instanceof AxeItem) {
                if (pattern.contains("linear") && state.isIn(BlockTags.LOGS)) {
                    fellTree(serverWorld, player, pos, stack);
                }
            } else if (item instanceof ShovelItem) {
                if (pattern.contains("charge")) {
                    break3x3(serverWorld, player, pos, stack, BlockTags.SHOVEL_MINEABLE);
                } else if (pattern.contains("linear") && state.getBlock() instanceof FallingBlock) {
                    BlockPos above = pos.up();
                    while (serverWorld.getBlockState(above).getBlock() instanceof FallingBlock) {
                        serverWorld.breakBlock(above, true, player);
                        above = above.up();
                    }
                } else if (pattern.contains("frost") && (state.isOf(Blocks.SNOW_BLOCK) || state.isOf(Blocks.SNOW))) {
                    if (world.random.nextFloat() < 0.25f) {
                        Block.dropStack(serverWorld, pos, new ItemStack(Items.PACKED_ICE));
                    }
                }
            } else if (item instanceof HoeItem) {
                if (pattern.contains("charge")) {
                    break3x3(serverWorld, player, pos, stack, BlockTags.HOE_MINEABLE);
                }
            }
        });

        UseBlockCal


lback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || player.isSneaking()) return ActionResult.PASS;
            ItemStack stack = player.getStackInHand(hand);
            String pattern = TrimHelper.getPattern(stack);
            if (pattern.isEmpty()) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Item item = stack.getItem();

            if (item instanceof ShovelItem && pattern.contains("tracks") && state.isOf(Blocks.GRASS_BLOCK)) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos target = pos.add(dx, 0, dz);
                        if (world.getBlockState(target).isOf(Blocks.GRASS_BLOCK) && world.getBlockState(target.up()).isAir()) {
                            world.setBlockState(target, Blocks.DIRT_PATH.getDefaultState());
                        }
                    }
                }
                return ActionResult.SUCCESS;
            }

            if (item instanceof HoeItem && pattern.contains("tracks") && (state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT))) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos target = pos.add(dx, 0, dz);
                        BlockState ts = world.getBlockState(target);
                        if ((ts.isOf(Blocks.GRASS_BLOCK) || ts.isOf(Blocks.DIRT)) && world.getBlockState(target.up()).isAir()) {
                            world.setBlockState(target, Blocks.FARMLAND.getDefaultState());
                        }
                    }
                }
                return ActionResult.SUCCESS;
            }

            if (item instanceof HoeItem && pattern.contains("linear") && state.getBlock() instanceof CropBlock crop && crop.isMature(state)) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos target = pos.add(dx, 0, dz);
                        BlockState ts = world.getBlockState(target);
                        if (ts.getBlock() instanceof CropBlock targetCrop && targetCrop.isMature(ts)) {
                            Block.dropStacks(ts, world, target, null, player, stack);
                            world.setBlockState(target, targetCrop.withAge(0));
                        }
                    }
                }
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }

    private static void break3x3(ServerWorld world, PlayerEntity player, BlockPos center, ItemStack stack, net.minecraft.registry.tag.TagKey<Block> validTag) {
        HitResult hit = player.raycast(5.0, 0.0f, false);
        Direction side = (hit instanceof BlockHitResult bhr) ? bhr.getSide() : Direction.NORTH;

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                BlockPos target = switch (side.getAxis()) {
                    case Y -> center.add(a, 0, b);
                    case X -> center.add(0, a, b);
                    case Z -> center.add(a, b, 0);
                };
                BlockState state = world.getBlockState(target);
                if (!state.isAir() && state.isIn(validTag) && state.getHardness(world, target) >= 0) {
                    BREAKING_BLOCKS.add(target);
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.interactionManager.tryBreakBlock(target);
                    }
                    BREAKING_BLOCKS.remove(target);
                }
            }
        }
    }

    private static void fellTree(ServerWorld world, PlayerEntity player, BlockPos start, ItemStack stack) {
        Queue<BlockPos> queue = new LinkedList<>();
        Set<Block


Pos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        int broken = 0;
        while (!queue.isEmpty() && broken < 64) {
            BlockPos current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos next = current.add(dx, dy, dz);
                        if (!visited.contains(next) && world.getBlockState(next).isIn(BlockTags.LOGS)) {
                            visited.add(next);
                            queue.add(next);
                            BREAKING_BLOCKS.add(next);
                            if (player instanceof ServerPlayerEntity sp) {
                                sp.interactionManager.tryBreakBlock(next);
                            }
                            BREAKING_BLOCKS.remove(next);
                            broken++;
                        }
                    }
                }
            }
        }
    }

    private static void highlightOres(ServerWorld world, BlockPos center, Block targetOre, int radius) {
        for (BlockPos pos : BlockPos.iterateOutwards(center, radius, radius, radius)) {
            if (!pos.equals(center) && world.getBlockState(pos).isOf(targetOre)) {
                DisplayEntity.BlockDisplayEntity marker = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
                marker.setBlockState(targetOre.getDefaultState());
                marker.setPosition(pos.getX(), pos.getY(), pos.getZ());
                marker.setGlowing(true);
                world.spawnEntity(marker);
                marker.age = 0;
            }
        }
    }
}
