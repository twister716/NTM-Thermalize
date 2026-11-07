package io.github.twister716.ntmthermalize;

import dev.golem.core.platform.ModEntry;
import io.github.twister716.ntmthermalize.temperature.ThermalizeTemperature;

public final class NTMThermalizeEntry implements ModEntry {

    @Override
    public void declare() {
        ThermalizeTemperature.init();
        NTMThermalize.LOG.info("NTM: Thermalize loaded");
    }
}
