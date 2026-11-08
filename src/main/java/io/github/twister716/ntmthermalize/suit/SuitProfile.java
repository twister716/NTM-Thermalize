package io.github.twister716.ntmthermalize.suit;

import io.github.twister716.ntmthermalize.temperature.AirConditioning;
import org.jspecify.annotations.Nullable;
import toughasnails.api.temperature.TemperatureLevel;

public record SuitProfile(
        int temperatureLimit,
        double claddingMultiplier,
        boolean stuffy,
        AirConditioning.Mode builtInAirConditioner,
        @Nullable TemperatureLevel fixedBase,
        boolean shownInTooltip
) {

    public static final int DEFAULT_LIMIT = 1;

    public static SuitProfile sealed(int temperatureLimit) {
        return new SuitProfile(temperatureLimit, 1.0, true, AirConditioning.Mode.NONE, null, true);
    }

    public static SuitProfile sealedWithAirConditioner(int temperatureLimit) {
        return new SuitProfile(temperatureLimit, 1.0, true, AirConditioning.Mode.BASIC, null, true);
    }

    public static SuitProfile sealedWithEquilibriumAirConditioner(int temperatureLimit) {
        return new SuitProfile(temperatureLimit, 1.0, true, AirConditioning.Mode.EQUILIBRIUM, null, true);
    }

    public static SuitProfile fireProximity(int temperatureLimit) {
        return new SuitProfile(temperatureLimit, 1.0, true, AirConditioning.Mode.NONE, TemperatureLevel.NEUTRAL, true);
    }

    public static SuitProfile euphemium() {
        return new SuitProfile(0, 0.0, false, AirConditioning.Mode.NONE, TemperatureLevel.NEUTRAL, false);
    }

    public static SuitProfile otherFullSet() {
        return new SuitProfile(DEFAULT_LIMIT, 0.5, false, AirConditioning.Mode.NONE, null, false);
    }
}
