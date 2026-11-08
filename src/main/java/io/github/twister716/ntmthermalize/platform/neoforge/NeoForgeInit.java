package io.github.twister716.ntmthermalize.platform.neoforge;

//? if neoforge {
import dev.golem.core.platform.ModEntries;
import dev.golem.core.platform.Platform;
import io.github.twister716.ntmthermalize.NTMThermalize;
import io.github.twister716.ntmthermalize.NTMThermalizeEntry;
import io.github.twister716.ntmthermalize.client.ThermalizeClient;
import net.neoforged.fml.common.Mod;

@Mod(NTMThermalize.MOD_ID)
public final class NeoForgeInit {

    public NeoForgeInit() {
        ModEntries.start(new NTMThermalizeEntry());
        if (Platform.isClient()) ThermalizeClient.init();
    }
}
//?}
