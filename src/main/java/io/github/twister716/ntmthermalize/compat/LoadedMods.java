package io.github.twister716.ntmthermalize.compat;

import dev.golem.core.platform.Platform;
import io.github.twister716.ntmthermalize.compat.space.SpaceCompat;

/**
 * Which optional mods are installed, looked up once. Machines and temperature modifiers ask every tick, so the answer
 * is kept here instead of asking the loader each time.
 */
public final class LoadedMods {

    public static final boolean NTM_SPACE = Platform.isModLoaded(SpaceCompat.MOD_ID);

    private LoadedMods() {}
}
