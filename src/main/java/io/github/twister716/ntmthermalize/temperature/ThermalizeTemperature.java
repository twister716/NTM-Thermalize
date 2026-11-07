package io.github.twister716.ntmthermalize.temperature;

import dev.golem.core.platform.Lifecycle;
import toughasnails.api.temperature.TemperatureHelper;

public final class ThermalizeTemperature {

    private static boolean registered;

    private ThermalizeTemperature() {}

    public static void init() {
        Lifecycle.onServerStarting(server -> register());
    }

    private static void register() {
        if (registered) return;
        TemperatureHelper.registerPlayerTemperatureModifier(new SuitTemperatureModifier());
        registered = true;
    }
}
