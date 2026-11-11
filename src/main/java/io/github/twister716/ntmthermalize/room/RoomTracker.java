package io.github.twister716.ntmthermalize.room;

import io.github.twister716.ntmthermalize.block.RoomClimateKind;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * The climate machines of each level and the rooms they found. Machines in the same room share the room one of them
 * found recently instead of searching it again. Only running machines count towards a room's climate.
 */
public final class RoomTracker {

    public static final int RESCAN_TICKS = 100;

    private static final class Machine {
        private final RoomClimateKind kind;
        private SealedRoom room;
        private boolean running;

        private Machine(RoomClimateKind kind, SealedRoom room) {
            this.kind = kind;
            this.room = room;
        }
    }

    private static final Map<Level, Map<BlockPos, Machine>> MACHINES = new WeakHashMap<>();

    private RoomTracker() {}

    /** Finds (or shares) the room of the machine at {@code pos} and records the machine in it. */
    public static SealedRoom update(Level level, BlockPos pos, RoomClimateKind kind) {
        long now = level.getGameTime();
        Map<BlockPos, Machine> machines = machinesOf(level);

        SealedRoom room = null;
        for (Machine machine : machines.values()) {
            if (now - machine.room.scannedAt() < RESCAN_TICKS && machine.room.contains(pos)) {
                room = machine.room;
                break;
            }
        }
        if (room == null) room = SealedRoom.scan(level, pos, now);

        Machine machine = machines.get(pos);
        if (machine == null || machine.kind != kind) {
            machine = new Machine(kind, room);
            machines.put(pos.immutable(), machine);
        }
        machine.room = room;
        return room;
    }

    public static void setRunning(Level level, BlockPos pos, boolean running) {
        Map<BlockPos, Machine> machines = MACHINES.get(level);
        Machine machine = machines != null ? machines.get(pos) : null;
        if (machine != null) machine.running = running;
    }

    public static void remove(Level level, BlockPos pos) {
        Map<BlockPos, Machine> machines = MACHINES.get(level);
        if (machines != null) machines.remove(pos);
    }

    /** The climate of the room around {@code pos}, or null outside every machine's room. */
    public static @Nullable RoomClimate climateAt(Level level, BlockPos pos) {
        Map<BlockPos, Machine> machines = MACHINES.get(level);
        if (machines == null || machines.isEmpty()) return null;

        int volume = 0;
        int coolers = 0;
        int heaters = 0;
        int controllers = 0;
        for (Machine machine : machines.values()) {
            if (!machine.room.contains(pos)) continue;
            volume = machine.room.volume();
            if (!machine.running) continue;
            switch (machine.kind) {
                case COOLER -> coolers++;
                case HEATER -> heaters++;
                case CONTROLLER -> controllers++;
            }
        }
        return volume == 0 ? null : new RoomClimate(volume, coolers, heaters, controllers);
    }

    private static Map<BlockPos, Machine> machinesOf(Level level) {
        return MACHINES.computeIfAbsent(level, key -> new HashMap<>());
    }
}
