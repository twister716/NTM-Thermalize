package io.github.twister716.ntmthermalize.block;

/** The three room climate machines and how much air one machine looks after. */
public enum RoomClimateKind {
    COOLER(1024),
    HEATER(1024),
    CONTROLLER(512);

    private final int volumePerMachine;

    RoomClimateKind(int volumePerMachine) {
        this.volumePerMachine = volumePerMachine;
    }

    public int volumePerMachine() {
        return volumePerMachine;
    }
}
