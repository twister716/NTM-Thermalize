package io.github.twister716.ntmthermalize.temperature;

import io.github.twister716.ntmthermalize.compat.LoadedMods;
import io.github.twister716.ntmthermalize.compat.space.SpaceCompat;
import io.github.twister716.ntmthermalize.room.RoomClimate;
import io.github.twister716.ntmthermalize.room.RoomTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import toughasnails.api.temperature.IPlayerTemperatureModifier;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.api.temperature.TemperatureLevel;

/**
 * The temperature around a player before armor: the place, then the climate machines of the sealed room the player is
 * in, or outside every room NTM Space's vacuum.
 */
public final class RoomTemperature implements IPlayerTemperatureModifier {

    @Override
    public TemperatureLevel modify(Player player, TemperatureLevel current) {
        return adjust(player, current);
    }

    public static TemperatureLevel at(Player player) {
        return adjust(player, TemperatureHelper.getTemperatureAtPos(player.level(), player.blockPosition()));
    }

    private static TemperatureLevel adjust(Player player, TemperatureLevel current) {
        Level level = player.level();
        if (level.isClientSide()) return current;

        RoomClimate climate = RoomTracker.climateAt(level, BlockPos.containing(player.getEyePosition()));
        if (climate != null) return climate.apply(current);
        if (LoadedMods.NTM_SPACE) return SpaceCompat.outsideRooms(player, current);
        return current;
    }
}
