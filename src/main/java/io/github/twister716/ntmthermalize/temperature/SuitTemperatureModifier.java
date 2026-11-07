package io.github.twister716.ntmthermalize.temperature;

import io.github.twister716.ntmthermalize.suit.SuitProfile;
import io.github.twister716.ntmthermalize.suit.ThermalSuits;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import toughasnails.api.temperature.IPlayerTemperatureModifier;
import toughasnails.api.temperature.TemperatureLevel;

public final class SuitTemperatureModifier implements IPlayerTemperatureModifier {

    private static final int STUFFINESS = 1;

    @Override
    public TemperatureLevel modify(Player player, TemperatureLevel current) {
        Optional<SuitProfile> worn = ThermalSuits.profileOf(player);
        if (worn.isEmpty()) return current;

        SuitProfile profile = worn.get();
        TemperatureLevel base = profile.fixedBase() != null ? profile.fixedBase() : current;
        return profile.stuffy() ? base.increment(STUFFINESS) : base;
    }
}
