package io.github.twister716.ntmthermalize.compat.space;

import net.minecraft.world.entity.player.Player;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.api.temperature.TemperatureLevel;

/** Registers the NTM Space temperatures with Tough As Nails. Only loaded when NTM Space is installed. */
public final class SpaceCompat {

    public static final String MOD_ID = "ntmspace";

    private SpaceCompat() {}

    public static void register() {
        TemperatureHelper.registerPositionalTemperatureModifier(new SpaceClimate());
        TemperatureHelper.registerPlayerTemperatureModifier(new SpaceRoomTemperature());
    }

    /** {@code place} with the sealed room or vacuum the player is in applied. */
    public static TemperatureLevel roomTemperature(Player player, TemperatureLevel place) {
        return SpaceRoomTemperature.adjust(player, place);
    }
}
