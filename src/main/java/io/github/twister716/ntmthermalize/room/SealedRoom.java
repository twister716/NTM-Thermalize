package io.github.twister716.ntmthermalize.room;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * The air a climate machine stands in, found by spreading from the machine through every block that does not seal.
 * If it spreads past {@link #MAX_VOLUME} blocks the room has a gap and is not sealed.
 */
public final class SealedRoom {

    public static final int MAX_VOLUME = 8192;
    private static final Direction[] DIRECTIONS = Direction.values();

    private final LongOpenHashSet cells;
    private final boolean sealed;
    private final long scannedAt;

    private SealedRoom(LongOpenHashSet cells, boolean sealed, long scannedAt) {
        this.cells = cells;
        this.sealed = sealed;
        this.scannedAt = scannedAt;
    }

    public static SealedRoom scan(Level level, BlockPos start, long now) {
        LongOpenHashSet cells = new LongOpenHashSet();
        LongOpenHashSet walls = new LongOpenHashSet();      // each wall block is checked once, not once per side
        LongArrayFIFOQueue open = new LongArrayFIFOQueue();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        cells.add(start.asLong());
        open.enqueue(start.asLong());

        while (!open.isEmpty()) {
            long pos = open.dequeueLong();
            for (Direction direction : DIRECTIONS) {
                long next = BlockPos.offset(pos, direction);
                if (cells.contains(next) || walls.contains(next)) continue;
                if (RoomSealing.isSealed(level, cursor.set(next))) {
                    walls.add(next);
                    continue;
                }
                if (cells.size() >= MAX_VOLUME) return new SealedRoom(new LongOpenHashSet(), false, now);
                cells.add(next);
                open.enqueue(next);
            }
        }
        return new SealedRoom(cells, true, now);
    }

    public boolean sealed() {
        return sealed;
    }

    public int volume() {
        return cells.size();
    }

    public long scannedAt() {
        return scannedAt;
    }

    public boolean contains(BlockPos pos) {
        return sealed && cells.contains(pos.asLong());
    }
}
