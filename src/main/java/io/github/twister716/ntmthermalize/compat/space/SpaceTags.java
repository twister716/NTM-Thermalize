package io.github.twister716.ntmthermalize.compat.space;

import dev.golem.core.dejson.Tags;
import io.github.twister716.ntmthermalize.NTMThermalize;

/** The tags of the NTM Space temperatures, written out as JSON at build time. */
public final class SpaceTags {

    private static final String NS = NTMThermalize.MOD_ID;

    private SpaceTags() {}

    public static void declare() {
        Tags.of("worldgen/biome", NS, "colder_biomes")
                .optional("ntmspace:laythe_polar", "ntmspace:duna_polar", "ntmspace:duna_polar_hills")
                .register();
    }
}
