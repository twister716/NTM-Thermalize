package io.github.twister716.ntmthermalize;

import dev.golem.core.platform.ModEntry;

public final class NTMThermalizeEntry implements ModEntry {

    @Override
    public void declare() {
        NTMThermalize.LOG.info("NTM: Thermalize loaded");
    }
}
