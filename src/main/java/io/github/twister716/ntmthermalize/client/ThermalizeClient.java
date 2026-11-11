package io.github.twister716.ntmthermalize.client;

import dev.golem.core.client.ClientTicks;
import dev.golem.core.client.ItemModelProperties;
import dev.golem.core.client.KeyMappings;
import dev.golem.core.client.Tooltips;
import dev.golem.core.net.GolemNetwork;
import io.github.twister716.ntmthermalize.NTMThermalize;
import io.github.twister716.ntmthermalize.network.ToggleAirConditionerPayload;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ThermalizeClient {

    private static KeyMapping toggleAirConditioner;

    private ThermalizeClient() {}

    public static void init() {
        toggleAirConditioner = KeyMappings.register(NTMThermalize.MOD_ID,
                "key.ntmthermalize.toggle_air_conditioner", GLFW.GLFW_KEY_G, "general");
        ClientTicks.onEndTick(ThermalizeClient::tick);
        Tooltips.onItemTooltip(FullSetBonusTooltip::addLines);
        ItemModelProperties.includeItems("hbm", "model_state", NTMThermalize.MOD_ID);
    }

    private static void tick() {
        while (toggleAirConditioner.consumeClick()) {
            GolemNetwork.sendToServer(ToggleAirConditionerPayload.CODEC, new ToggleAirConditionerPayload());
        }
    }
}
