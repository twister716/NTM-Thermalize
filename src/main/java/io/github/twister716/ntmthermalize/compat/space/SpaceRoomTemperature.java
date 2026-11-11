package io.github.twister716.ntmthermalize.compat.space;

import com.ntmspace.atmosphere.AtmosphereBlob;
import com.ntmspace.atmosphere.AtmosphereHandler;
import dev.golem.core.Ids;
import io.github.twister716.ntmthermalize.NTMThermalize;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import toughasnails.api.temperature.IPlayerTemperatureModifier;
import toughasnails.api.temperature.TemperatureLevel;

/**
 * The temperature inside NTM Space's sealed rooms. Breathable air moves the outside temperature one level toward
 * neutral; a heater (a block in {@code room_heat_sources}) makes a room on a cold body neutral, a cooler (a block in
 * {@code room_cooling_sources}) a room on a hot one. Ordinary heat sources still warm or cool the player standing
 * near them by one level, as Tough As Nails does elsewhere; in a vacuum they do nothing.
 */
public final class SpaceRoomTemperature implements IPlayerTemperatureModifier {

    private static final int RESCAN_TICKS = 100;
    private static final TagKey<Block> HEAT_SOURCES =
            TagKey.create(Registries.BLOCK, Ids.of(NTMThermalize.MOD_ID, "room_heat_sources"));
    private static final TagKey<Block> COOLING_SOURCES =
            TagKey.create(Registries.BLOCK, Ids.of(NTMThermalize.MOD_ID, "room_cooling_sources"));

    private static final Map<AtmosphereBlob, Sources> SCANNED = new WeakHashMap<>();

    @Override
    public TemperatureLevel modify(Player player, TemperatureLevel current) {
        return adjust(player, current);
    }

    /** {@code current} with the sealed room or vacuum the player is in applied. */
    static TemperatureLevel adjust(Player player, TemperatureLevel current) {
        Level level = player.level();
        if (level.isClientSide()) return current;

        TemperatureLevel outside = SpaceClimate.climateAt(level, player.blockPosition());
        if (outside == null) return current;

        AtmosphereBlob room = breathableRoom(level, BlockPos.containing(player.getEyePosition()));
        if (room == null) return SpaceClimate.isVacuum(level) ? outside : current;

        Sources sources = sourcesOf(level, room);
        boolean cold = outside.ordinal() < TemperatureLevel.NEUTRAL.ordinal();
        boolean hot = outside.ordinal() > TemperatureLevel.NEUTRAL.ordinal();
        TemperatureLevel inside = (cold && sources.heating()) || (hot && sources.cooling())
                ? TemperatureLevel.NEUTRAL
                : towardNeutral(outside);
        int nearbyBlocks = current.ordinal() - outside.ordinal();
        return inside.increment(nearbyBlocks);
    }

    private static @Nullable AtmosphereBlob breathableRoom(Level level, BlockPos pos) {
        List<AtmosphereBlob> rooms = AtmosphereHandler.getBlobs(level, pos);
        if (rooms.isEmpty()) return null;
        return AtmosphereHandler.canBreathe(AtmosphereHandler.getAtmosphere(level, pos)) ? rooms.get(0) : null;
    }

    private static TemperatureLevel towardNeutral(TemperatureLevel outside) {
        int distance = TemperatureLevel.NEUTRAL.ordinal() - outside.ordinal();
        return outside.increment(Integer.signum(distance));
    }

    private static Sources sourcesOf(Level level, AtmosphereBlob room) {
        long now = level.getGameTime();
        Sources known = SCANNED.get(room);
        if (known != null && now - known.scannedAt() < RESCAN_TICKS) return known;

        Sources found = scan(level, room, now);
        SCANNED.put(room, found);
        return found;
    }

    /** Looks for heaters and coolers in the room and in the blocks around it. */
    private static Sources scan(Level level, AtmosphereBlob room, long now) {
        boolean heating = false;
        boolean cooling = false;
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();

        for (BlockPos pos : room.getLocations()) {
            for (int side = -1; side < Direction.values().length; side++) {
                if (side < 0) neighbor.set(pos);
                else neighbor.setWithOffset(pos, Direction.values()[side]);

                BlockState state = level.getBlockState(neighbor);
                heating |= isHeatSource(state);
                cooling |= isCoolingSource(state);
                if (heating && cooling) return new Sources(now, true, true);
            }
        }
        return new Sources(now, heating, cooling);
    }

    private static boolean isHeatSource(BlockState state) {
        return state.is(HEAT_SOURCES);
    }

    private static boolean isCoolingSource(BlockState state) {
        return state.is(COOLING_SOURCES);
    }

    private record Sources(long scannedAt, boolean heating, boolean cooling) {}
}
