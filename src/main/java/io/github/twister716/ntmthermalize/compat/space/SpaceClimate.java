package io.github.twister716.ntmthermalize.compat.space;

import com.ntmspace.orbit.OrbitalStation;
import com.ntmspace.orbit.StationData;
import com.ntmspace.trait.CBT_Atmosphere;
import com.ntmspace.trait.CelestialTraits;
import com.ntmspace.world.SolarSystem;
import dev.golem.core.Ids;
import io.github.twister716.ntmthermalize.NTMThermalize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;
import toughasnails.api.temperature.IPositionalTemperatureModifier;
import toughasnails.api.temperature.TemperatureLevel;

/**
 * The temperature outside on NTM Space's bodies: set by the distance from the star, one level warmer under a thick
 * atmosphere, one level colder in polar biomes, and icy in a vacuum. Kerbin (the overworld) is left to Tough As Nails.
 * In orbit it is set by the distance of the body the station circles, shifting with the progress of a transfer, one
 * level warmer in sunlight and two levels colder in the body's shadow.
 */
public final class SpaceClimate implements IPositionalTemperatureModifier {

    private static final double VACUUM_PRESSURE = 0.001;
    private static final String NAMESPACE = "ntmspace";
    private static final String ORBIT = "orbit";
    private static final String HOT_IN_VACUUM = "moho";
    private static final double GREENHOUSE_PRESSURE = 0.5;
    private static final double SHADOW_ECLIPSE = 0.5;
    private static final TagKey<Biome> COLDER_BIOMES =
            TagKey.create(Registries.BIOME, Ids.of(NTMThermalize.MOD_ID, "colder_biomes"));

    @Override
    public TemperatureLevel modify(Level level, BlockPos pos, TemperatureLevel current) {
        TemperatureLevel climate = climateAt(level, pos);
        return climate != null ? climate : current;
    }

    /** The temperature outside at {@code pos}, or null when the level is not one of NTM Space's bodies. */
    static @Nullable TemperatureLevel climateAt(Level level, BlockPos pos) {
        String body = bodyOf(level);
        if (body == null) return null;
        if (body.equals(ORBIT)) return orbitTemperature(level, pos);
        if (isVacuum(level, body)) return vacuumTemperature(body);

        TemperatureLevel climate = byDistance(body);
        if (pressure(level, body) >= GREENHOUSE_PRESSURE) climate = climate.increment(1);
        if (level.getBiome(pos).is(COLDER_BIOMES)) climate = climate.decrement(1);
        return climate;
    }

    /** Whether the open air of the level is a vacuum (always in orbit). */
    static boolean isVacuum(Level level) {
        String body = bodyOf(level);
        return body != null && isVacuum(level, body);
    }

    private static boolean isVacuum(Level level, String body) {
        return body.equals(ORBIT) || pressure(level, body) <= VACUUM_PRESSURE;
    }

    private static TemperatureLevel vacuumTemperature(String body) {
        return body.equals(HOT_IN_VACUUM) ? TemperatureLevel.HOT : TemperatureLevel.ICY;
    }

    /**
     * The distance level of the body the station circles; while a station or a rocket travels, moving from the one
     * body's to the other's as the transfer progresses. Then one level up in sunlight, two down in shadow, except
     * that sunlight is too weak to warm an icy orbit.
     */
    private static TemperatureLevel orbitTemperature(Level level, BlockPos pos) {
        OrbitalStation station = stationAt(level, pos);
        if (station == null) return TemperatureLevel.ICY;

        TemperatureLevel base = byDistance(station.orbiting.name);
        if (station.state == OrbitalStation.StationState.TRANSFER) {
            TemperatureLevel to = byDistance(station.target.name);
            double progress = station.getTransferProgress(0);
            int step = (int) Math.round(Mth.lerp(progress, base.ordinal(), to.ordinal()));
            base = TemperatureLevel.values()[step];
        }

        if (base == TemperatureLevel.ICY || level.isClientSide()) return base;
        boolean inShadow = station.getEclipseAmount(level.getGameTime()) >= SHADOW_ECLIPSE;
        return inShadow ? base.decrement(2) : base.increment(1);
    }

    /** The station whose square {@code pos} lies in, without creating one; the client knows only its own. */
    private static @Nullable OrbitalStation stationAt(Level level, BlockPos pos) {
        if (level.isClientSide()) return OrbitalStation.clientStation;
        MinecraftServer server = level.getServer();
        return server != null ? StationData.get(server).getStationFromPosition(pos.getX(), pos.getZ()) : null;
    }

    private static TemperatureLevel byDistance(String body) {
        float sunPower = SolarSystem.body(body).getSunPower();
        if (sunPower >= 4.0F) return TemperatureLevel.HOT;
        if (sunPower >= 1.5F) return TemperatureLevel.WARM;
        if (sunPower >= 0.7F) return TemperatureLevel.NEUTRAL;
        if (sunPower >= 0.25F) return TemperatureLevel.COLD;
        return TemperatureLevel.ICY;
    }

    private static double pressure(Level level, String body) {
        CBT_Atmosphere atmosphere = CelestialTraits.getTrait(body, CBT_Atmosphere.class, level.isClientSide());
        return atmosphere != null ? atmosphere.getPressure() : 0;
    }

    /**
     * The body the level is the surface of, read from the dimension name ({@code ntmspace:duna} is Duna), or
     * {@code orbit}, or null. NTM Space's own lookup answers Kerbin for Thatmo, so it is not used here.
     */
    private static @Nullable String bodyOf(Level level) {
        var id = Ids.keyId(level.dimension());
        if (!id.getNamespace().equals(NAMESPACE)) return null;
        String name = id.getPath();
        if (name.equals(ORBIT)) return ORBIT;
        return SolarSystem.body(name) != null ? name : null;
    }
}
