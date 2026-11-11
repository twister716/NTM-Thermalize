package io.github.twister716.ntmthermalize.temperature;

import dev.golem.core.platform.Platform;
import io.github.twister716.ntmthermalize.compat.space.SpaceCompat;
import net.minecraft.world.entity.player.Player;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.api.temperature.TemperatureLevel;

/** The temperature around a player before armor: the place, then NTM Space's rooms and vacuum. */
public final class RoomTemperature {

    private RoomTemperature() {}

    public static TemperatureLevel at(Player player) {
        TemperatureLevel place = TemperatureHelper.getTemperatureAtPos(player.level(), player.blockPosition());
        if (Platform.isModLoaded(SpaceCompat.MOD_ID)) return SpaceCompat.roomTemperature(player, place);
        return place;
    }
}
