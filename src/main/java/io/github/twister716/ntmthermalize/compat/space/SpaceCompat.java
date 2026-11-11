package io.github.twister716.ntmthermalize.compat.space;

import com.ntmspace.atmosphere.AtmosphereHandler;
import com.ntmspace.trait.CBT_Atmosphere;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.api.temperature.TemperatureLevel;

/** Registers the NTM Space temperatures with Tough As Nails. Only loaded when NTM Space is installed. */
public final class SpaceCompat {

    public static final String MOD_ID = "ntmspace";

    private SpaceCompat() {}

    public static void register() {
        TemperatureHelper.registerPositionalTemperatureModifier(new SpaceClimate());
    }

    /** The air pressure at {@code pos} in atm: the body's air and any NTM Space room's air there. */
    public static double airPressure(Level level, BlockPos pos) {
        CBT_Atmosphere atmosphere = AtmosphereHandler.getAtmosphere(level, pos);
        return atmosphere != null ? atmosphere.getPressure() : 0;
    }

    /**
     * {@code current} for a player outside every sealed room: in a vacuum the heat of nearby blocks does not reach
     * them, so it is the temperature of the place alone.
     */
    public static TemperatureLevel outsideRooms(Player player, TemperatureLevel current) {
        Level level = player.level();
        if (!SpaceClimate.isVacuum(level)) return current;
        TemperatureLevel outside = SpaceClimate.climateAt(level, player.blockPosition());
        return outside != null ? outside : current;
    }
}
