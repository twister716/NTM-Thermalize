package io.github.twister716.ntmthermalize.temperature;

import dev.golem.core.platform.Lifecycle;
import dev.golem.core.platform.Platform;
import io.github.twister716.ntmthermalize.compat.space.SpaceCompat;
import toughasnails.api.temperature.TemperatureHelper;

public final class ThermalizeTemperature {

    private static boolean registered;

    private ThermalizeTemperature() {}

    public static void init() {
        Lifecycle.onServerStarting(server -> register());
    }

    private static void register() {
        if (registered) return;
        if (Platform.isModLoaded(SpaceCompat.MOD_ID)) SpaceCompat.register();
        TemperatureHelper.registerPlayerTemperatureModifier(new SuitTemperatureModifier());
        registered = true;
    }
}
