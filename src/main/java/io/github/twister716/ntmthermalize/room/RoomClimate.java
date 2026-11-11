package io.github.twister716.ntmthermalize.room;

import io.github.twister716.ntmthermalize.block.RoomClimateKind;
import toughasnails.api.temperature.TemperatureLevel;

/**
 * What the machines of one room do to it. Each machine looks after {@link RoomClimateKind#volumePerMachine} blocks of
 * air: a room needs one machine of a kind for each of those, and every full set of coolers or heaters moves it one
 * level. Climate controllers hold it at neutral once there are enough of them.
 */
public record RoomClimate(int volume, int coolers, int heaters, int controllers) {

    public int needed(RoomClimateKind kind) {
        int perMachine = kind.volumePerMachine();
        return Math.max(1, (volume + perMachine - 1) / perMachine);
    }

    public int coolingLevels() {
        return coolers / needed(RoomClimateKind.COOLER);
    }

    public int heatingLevels() {
        return heaters / needed(RoomClimateKind.HEATER);
    }

    public boolean controlled() {
        return controllers >= needed(RoomClimateKind.CONTROLLER);
    }

    public TemperatureLevel apply(TemperatureLevel current) {
        if (controlled()) return TemperatureLevel.NEUTRAL;
        return current.increment(heatingLevels() - coolingLevels());
    }
}
