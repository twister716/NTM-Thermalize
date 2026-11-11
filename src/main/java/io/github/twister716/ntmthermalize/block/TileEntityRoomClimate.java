package io.github.twister716.ntmthermalize.block;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.tileentity.TilePortShapes;
import dev.golem.core.data.DataIn;
import dev.golem.core.data.DataOut;
import io.github.twister716.ntmthermalize.compat.LoadedMods;
import io.github.twister716.ntmthermalize.compat.space.SpaceCompat;
import io.github.twister716.ntmthermalize.room.RoomClimate;
import io.github.twister716.ntmthermalize.room.RoomTracker;
import io.github.twister716.ntmthermalize.room.SealedRoom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.api.temperature.TemperatureLevel;

/**
 * A room climate machine. Every {@link RoomTracker#RESCAN_TICKS} ticks it finds its room; every second it reads the
 * temperature of the room before any machine and decides what to do; every tick it runs if it has the power and the
 * coolant for it, and only in air of at least {@link #MIN_PRESSURE} atm. Only a running machine counts towards its
 * room, and its lamp blinks.
 *
 * <ul>
 *   <li>Cooler: 500 HE/t and 10 mB/t of water (giving 10 mB/t of low-pressure steam), or 100 HE/t and 1 mB/t of
 *       cryogel; idle in an icy room.</li>
 *   <li>Heater: 750 HE/t; idle in a hot room.</li>
 *   <li>Climate controller: in a warm or hot room 750 HE/t and 15 mB/t of water (giving 15 mB/t of low-pressure
 *       steam), in a cold or icy room 1000 HE/t; idle in a neutral room.</li>
 * </ul>
 */
public class TileEntityRoomClimate extends TileEntityLoadedBase implements IEnergyReceiverMK2,
        IFluidStandardTransceiverMK2 {

    public static final long MAX_POWER = 100_000;
    private static final int TANK_CAPACITY = 16_000;
    private static final int INFO_TICKS = 20;
    private static final double MIN_PRESSURE = 0.1;
    private static final FluidTankNTM[] NO_TANKS = new FluidTankNTM[0];

    /** Why the machine is or is not running, shown in the look overlay. */
    public enum Status {
        RUNNING, NOT_SEALED, NO_AIR, ALREADY_ICY, ALREADY_HOT, ALREADY_NEUTRAL, NO_POWER, NO_COOLANT, STEAM_FULL
    }

    private enum Mode { IDLE, COOL, HEAT }

    public final FluidTankNTM coolant = new FluidTankNTM(Fluids.WATER, TANK_CAPACITY);
    public final FluidTankNTM steam = new FluidTankNTM(Fluids.SPENTSTEAM, TANK_CAPACITY);
    // The ports ask for these every tick, so the arrays are made once.
    private final FluidTankNTM[] allTanks = {coolant, steam};
    private final FluidTankNTM[] receivingTanks = {coolant};
    private final FluidTankNTM[] sendingTanks = {steam};
    private long power;

    private boolean scanned;
    private boolean sealed;
    private boolean hasAir = true;
    private TemperatureLevel roomTemperature = TemperatureLevel.NEUTRAL;
    private boolean running;
    private Status status = Status.NOT_SEALED;
    private int volume;
    private int needed;
    private int runningInRoom;
    private int levels;

    private PortDef[] ports;

    public TileEntityRoomClimate(BlockPos pos, BlockState state) {
        super(ThermalBlocks.ROOM_CLIMATE_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityRoomClimate machine) {
        machine.updateServer(level);
    }

    public RoomClimateKind kind() {
        return ((BlockRoomClimate) getBlockState().getBlock()).kind();
    }

    private void updateServer(Level level) {
        setupAllPorts(getPorts());
        updatePortPIFIFO();

        long time = level.getGameTime() + Math.floorMod(worldPosition.hashCode(), RoomTracker.RESCAN_TICKS);
        if (!scanned || time % RoomTracker.RESCAN_TICKS == 0) {
            SealedRoom room = RoomTracker.update(level, worldPosition, kind());
            sealed = room.sealed();
            volume = room.volume();
            scanned = true;
        }
        if (time % INFO_TICKS == 0) {
            roomTemperature = TemperatureHelper.getTemperatureAtPos(level, worldPosition);
            hasAir = airPressure(level) >= MIN_PRESSURE;
            readClimate(level);
        }

        status = run();
        setRunning(level, status == Status.RUNNING);
        setChanged();
        networkPackNT();
    }

    private Status run() {
        if (!sealed) return Status.NOT_SEALED;
        if (!hasAir) return Status.NO_AIR;
        Mode mode = mode();
        if (mode == Mode.IDLE) {
            return switch (kind()) {
                case COOLER -> Status.ALREADY_ICY;
                case HEATER -> Status.ALREADY_HOT;
                case CONTROLLER -> Status.ALREADY_NEUTRAL;
            };
        }

        boolean cryogel = coolant.getTankType() == Fluids.CRYOGEL;
        boolean usesWater = mode == Mode.COOL && !cryogel;
        long powerUse = powerUse(mode, cryogel);
        int coolantUse = mode == Mode.COOL ? (kind() == RoomClimateKind.CONTROLLER ? 15 : cryogel ? 1 : 10) : 0;
        int steamMade = usesWater ? coolantUse : 0;

        if (power < powerUse) return Status.NO_POWER;
        if (coolant.getFill() < coolantUse) return Status.NO_COOLANT;
        if (steam.getFill() + steamMade > steam.getMaxFill()) return Status.STEAM_FULL;

        power -= powerUse;
        coolant.setFill(coolant.getFill() - coolantUse);
        steam.setFill(steam.getFill() + steamMade);
        return Status.RUNNING;
    }

    /**
     * The air around the machine: NTM Space's air (the body's own and any vented room's) in the best of the six
     * blocks next to it, or ordinary air without NTM Space.
     */
    private double airPressure(Level level) {
        if (!LoadedMods.NTM_SPACE) return 1.0;
        double best = 0;
        for (Direction direction : Direction.values()) {
            best = Math.max(best, SpaceCompat.airPressure(level, worldPosition.relative(direction)));
        }
        return best;
    }

    private Mode mode() {
        int fromNeutral = roomTemperature.ordinal() - TemperatureLevel.NEUTRAL.ordinal();
        return switch (kind()) {
            case COOLER -> roomTemperature == TemperatureLevel.ICY ? Mode.IDLE : Mode.COOL;
            case HEATER -> roomTemperature == TemperatureLevel.HOT ? Mode.IDLE : Mode.HEAT;
            case CONTROLLER -> fromNeutral > 0 ? Mode.COOL : fromNeutral < 0 ? Mode.HEAT : Mode.IDLE;
        };
    }

    private long powerUse(Mode mode, boolean cryogel) {
        return switch (kind()) {
            case COOLER -> cryogel ? 100 : 500;
            case HEATER -> 750;
            case CONTROLLER -> mode == Mode.COOL ? 750 : 1000;
        };
    }

    private void setRunning(Level level, boolean running) {
        if (this.running == running) return;
        this.running = running;
        RoomTracker.setRunning(level, worldPosition, running);
        BlockState state = getBlockState();
        if (state.getValue(BlockRoomClimate.ACTIVE) != running) {
            level.setBlock(worldPosition, state.setValue(BlockRoomClimate.ACTIVE, running), Block.UPDATE_CLIENTS);
        }
    }

    private void readClimate(Level level) {
        RoomClimate climate = sealed ? RoomTracker.climateAt(level, worldPosition) : null;
        if (climate == null) {
            needed = runningInRoom = levels = 0;
            return;
        }
        needed = climate.needed(kind());
        switch (kind()) {
            case COOLER -> {
                runningInRoom = climate.coolers();
                levels = climate.coolingLevels();
            }
            case HEATER -> {
                runningInRoom = climate.heaters();
                levels = climate.heatingLevels();
            }
            case CONTROLLER -> {
                runningInRoom = climate.controllers();
                levels = climate.controlled() ? 1 : 0;
            }
        }
    }

    /** Sets the cooler's coolant; returns false for a fluid it cannot use. */
    public boolean setCoolant(FluidType type) {
        if (kind() != RoomClimateKind.COOLER || (type != Fluids.WATER && type != Fluids.CRYOGEL)) return false;
        coolant.setTankTypeByIdentifier(type);
        setChanged();
        return true;
    }

    public boolean usesCoolant() {
        return kind() != RoomClimateKind.HEATER;
    }

    public Status status() {
        return status;
    }

    public int volume() {
        return volume;
    }

    public int needed() {
        return needed;
    }

    public int runningInRoom() {
        return runningInRoom;
    }

    /** Levels this kind moves the room by (for a controller, 1 while it holds the room at neutral). */
    public int levels() {
        return levels;
    }

    private PortDef[] getPorts() {
        if (ports == null) ports = TilePortShapes.around(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        return ports;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) RoomTracker.remove(level, worldPosition);
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return usesCoolant() ? allTanks : NO_TANKS;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return usesCoolant() ? receivingTanks : NO_TANKS;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return usesCoolant() ? sendingTanks : NO_TANKS;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        coolant.serialize(buf);
        steam.serialize(buf);
        buf.writeInt(status.ordinal());
        buf.writeInt(volume);
        buf.writeInt(needed);
        buf.writeInt(runningInRoom);
        buf.writeInt(levels);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();
        coolant.deserialize(buf);
        steam.deserialize(buf);
        status = Status.values()[buf.readInt()];
        volume = buf.readInt();
        needed = buf.readInt();
        runningInRoom = buf.readInt();
        levels = buf.readInt();
    }

    @Override
    protected void saveData(DataOut output) {
        super.saveData(output);
        output.putLong("power", power);
        coolant.writeToNBT(output, "coolant");
        steam.writeToNBT(output, "steam");
    }

    @Override
    protected void loadData(DataIn input) {
        super.loadData(input);
        power = input.getLongOr("power", 0);
        coolant.readFromNBT(input, "coolant");
        steam.readFromNBT(input, "steam");
        if (coolant.getTankType() != Fluids.CRYOGEL) coolant.setTankType(Fluids.WATER);
        steam.setTankType(Fluids.SPENTSTEAM);
    }
}
