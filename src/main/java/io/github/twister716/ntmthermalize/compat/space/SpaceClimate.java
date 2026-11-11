package io.github.twister716.ntmthermalize.compat.space;

import com.ntmspace.trait.CBT_Atmosphere;
import com.ntmspace.trait.CelestialTraits;
import com.ntmspace.world.SolarSystem;
import dev.golem.core.Ids;
import io.github.twister716.ntmthermalize.NTMThermalize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import toughasnails.api.temperature.IPositionalTemperatureModifier;
import toughasnails.api.temperature.TemperatureLevel;

/**
 * The temperature outside on NTM Space's bodies: set by the distance from the star, one level warmer under a thick
 * atmosphere, one level colder in polar biomes, and icy in a vacuum. Kerbin (the overworld) is left to Tough As Nails.
 */
public final class SpaceClimate implements IPositionalTemperatureModifier {

    static final double VACUUM_PRESSURE = 0.001;

    private static final String NAMESPACE = "ntmspace";
    private static final String ORBIT = "orbit";
    private static final String HOT_IN_VACUUM = "moho";
    private static final double GREENHOUSE_PRESSURE = 0.5;
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

    static TemperatureLevel vacuumTemperature(Level level) {
        String body = bodyOf(level);
        return body != null ? vacuumTemperature(body) : TemperatureLevel.ICY;
    }

    private static boolean isVacuum(Level level, String body) {
        return body.equals(ORBIT) || pressure(level, body) <= VACUUM_PRESSURE;
    }

    private static TemperatureLevel vacuumTemperature(String body) {
        return body.equals(HOT_IN_VACUUM) ? TemperatureLevel.HOT : TemperatureLevel.ICY;
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
