package io.github.twister716.ntmthermalize.room;

import com.hbm.blocks.generic.BlockDoorGeneric;
import com.hbm.blocks.multiblock.BlockMultiblockCell;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import dev.golem.core.Ids;
import io.github.twister716.ntmthermalize.NTMThermalize;
import io.github.twister716.ntmthermalize.block.BlockRoomClimate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Which blocks close a room, after NTM Space's rules: full blocks seal; air, liquids, leaves, fences, farmland,
 * furnaces and NTM's multiblock machines do not. Unlike NTM Space, slabs, stairs and the blocks of
 * {@code #ntmthermalize:seals_rooms} (glass panes) seal too, and doors and trapdoors seal while closed. An unloaded
 * chunk counts as sealed, so looking for a room never loads one.
 */
public final class RoomSealing {

    private static final Map<BlockState, Boolean> FULL_BLOCK = new ConcurrentHashMap<>();
    private static final TagKey<Block> SEALS_ROOMS =
            TagKey.create(Registries.BLOCK, Ids.of(NTMThermalize.MOD_ID, "seals_rooms"));

    private RoomSealing() {}

    public static boolean isSealed(Level level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return false;
        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return true;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (state.isAir()) return false;
        if (block instanceof BlockRoomClimate) return false;
        if (block instanceof DoorBlock || block instanceof TrapDoorBlock) return !state.getValue(BlockStateProperties.OPEN);
        if (block instanceof BlockDoorGeneric door) return !door.hasExtra(state);
        if (block instanceof SlabBlock || block instanceof StairBlock) return true;
        if (state.is(SEALS_ROOMS)) return true;
        if (block instanceof FenceBlock || state.is(Blocks.FARMLAND)) return false;
        if (block instanceof AbstractFurnaceBlock) return false;
        if (block instanceof BlockMultiblockCore || block instanceof BlockMultiblockCell) return false;
        if (block instanceof LiquidBlock || state.is(BlockTags.LEAVES)) return false;
        return FULL_BLOCK.computeIfAbsent(state, RoomSealing::isFullBlock);
    }

    private static boolean isFullBlock(BlockState state) {
        try {
            return Block.isShapeFullBlock(state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        } catch (RuntimeException e) {
            return false;
        }
    }
}
