package io.github.twister716.ntmthermalize.network;

import dev.golem.core.Ids;
import dev.golem.core.net.GolemCodec;
import dev.golem.core.net.GolemNetwork;
import dev.golem.core.net.GolemPayload;
import io.github.twister716.ntmthermalize.NTMThermalize;
import io.github.twister716.ntmthermalize.temperature.AirConditioning;
import net.minecraft.resources.Identifier;

public record ToggleAirConditionerPayload() implements GolemPayload {

    public static final GolemCodec<ToggleAirConditionerPayload> CODEC = GolemCodec.of(
            ToggleAirConditionerPayload.class,
            Ids.of(NTMThermalize.MOD_ID, "toggle_air_conditioner"),
            (payload, buffer) -> {},
            buffer -> new ToggleAirConditionerPayload());

    @Override
    public Identifier id() {
        return CODEC.id();
    }

    public static void init() {
        GolemNetwork.registerPlayToServer(CODEC, (payload, player) -> AirConditioning.toggle(player));
    }
}
