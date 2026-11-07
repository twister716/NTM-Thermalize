package io.github.twister716.ntmthermalize.suit;

import org.jspecify.annotations.Nullable;
import toughasnails.api.temperature.TemperatureLevel;

public record SuitProfile(
        int temperatureLimit,
        double claddingMultiplier,
        boolean stuffy,
        @Nullable TemperatureLevel fixedBase
) {

    public static final int DEFAULT_LIMIT = 1;

    public static SuitProfile sealed(int temperatureLimit) {
        return new SuitProfile(temperatureLimit, 1.0, true, null);
    }

    public static SuitProfile fireProximity(int temperatureLimit) {
        return new SuitProfile(temperatureLimit, 1.0, true, TemperatureLevel.NEUTRAL);
    }

    public static SuitProfile otherFullSet() {
        return new SuitProfile(DEFAULT_LIMIT, 0.5, false, null);
    }
}
