package io.github.twister716.ntmthermalize;

import dev.golem.core.platform.ModEntry;
import io.github.twister716.ntmthermalize.block.ThermalBlocks;
import io.github.twister716.ntmthermalize.item.ThermalItems;
import io.github.twister716.ntmthermalize.item.ThermalTab;
import io.github.twister716.ntmthermalize.network.ToggleAirConditionerPayload;
import io.github.twister716.ntmthermalize.temperature.AirConditioning;
import io.github.twister716.ntmthermalize.temperature.ThermalizeTemperature;

public final class NTMThermalizeEntry implements ModEntry {

    @Override
    public void declare() {
        ThermalItems.init();
        ThermalBlocks.init();
        ThermalTab.init();
        AirConditioning.init();
        ToggleAirConditionerPayload.init();
        ThermalizeTemperature.init();
        NTMThermalize.LOG.info("NTM: Thermalize loaded");
    }
}
