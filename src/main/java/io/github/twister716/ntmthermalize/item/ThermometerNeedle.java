package io.github.twister716.ntmthermalize.item;

/**
 * The needle of the room thermometer: its 17 positions and which item model shows which position.
 *
 * <p>Kept apart from {@link RoomThermometer} on purpose. {@link ThermalItems} reads these numbers while it registers
 * the item models, which also happens at build time with no game running; touching {@code RoomThermometer} there
 * would load {@code Item}, and that needs a running game.
 */
public final class ThermometerNeedle {

    /** Positions from 0 (icy) to 16 (hot); every fourth one is a temperature level. */
    public static final int POSITIONS = 17;
    public static final int STEPS_PER_LEVEL = 4;
    /** The upright needle in the middle of the dial. */
    public static final int NEUTRAL = (POSITIONS - 1) / 2;

    private ThermometerNeedle() {}

    /**
     * The needle position the item model with index {@code model} shows. The model of the upright neutral needle
     * comes first because it is the item's own.
     */
    public static int positionOfModel(int model) {
        return (model + NEUTRAL) % POSITIONS;
    }

    /** The reverse of {@link #positionOfModel}: the index of the item model that shows {@code position}. */
    public static int modelOfPosition(int position) {
        return (position - NEUTRAL + POSITIONS) % POSITIONS;
    }
}
