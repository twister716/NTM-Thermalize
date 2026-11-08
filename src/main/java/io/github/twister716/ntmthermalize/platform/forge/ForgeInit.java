package io.github.twister716.ntmthermalize.platform.forge;

//? if forge {
/*import dev.golem.core.platform.ModEntries;
import dev.golem.core.platform.Platform;
import io.github.twister716.ntmthermalize.NTMThermalize;
import io.github.twister716.ntmthermalize.NTMThermalizeEntry;
import io.github.twister716.ntmthermalize.client.ThermalizeClient;
import net.minecraftforge.fml.common.Mod;

@Mod(NTMThermalize.MOD_ID)
public final class ForgeInit {

    public ForgeInit() {
        ModEntries.start(new NTMThermalizeEntry());
        if (Platform.isClient()) ThermalizeClient.init();
    }
}
*///?}
