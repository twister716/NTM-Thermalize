package io.github.twister716.ntmthermalize.platform.neoforge;

//? if neoforge {
import io.github.twister716.ntmthermalize.NTMThermalize;
import io.github.twister716.ntmthermalize.NTMThermalizeEntry;
import dev.golem.core.platform.ModEntries;
import net.neoforged.fml.common.Mod;

@Mod(NTMThermalize.MOD_ID)
public final class NeoForgeInit {

    public NeoForgeInit() {
        ModEntries.start(new NTMThermalizeEntry());
    }
}
//?}
