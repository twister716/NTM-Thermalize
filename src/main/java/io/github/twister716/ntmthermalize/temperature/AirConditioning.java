package io.github.twister716.ntmthermalize.temperature;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.packet.PlayerInformPayload;
import com.mojang.serialization.Codec;
import dev.golem.core.data.GolemComponent;
import dev.golem.core.net.GolemNetwork;
import io.github.twister716.ntmthermalize.NTMThermalize;
import io.github.twister716.ntmthermalize.item.AirConditioner;
import io.github.twister716.ntmthermalize.suit.SuitProfile;
import io.github.twister716.ntmthermalize.suit.ThermalSuits;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class AirConditioning {

    public enum Mode { NONE, BASIC, EQUILIBRIUM }

    private static final int INFO_ID = 71600;

    public static final GolemComponent<Boolean> TURNED_OFF =
            GolemComponent.of(NTMThermalize.MOD_ID, "air_conditioner_off", Codec.BOOL);

    private AirConditioning() {}

    public static void init() {}

    public static Mode mode(Player player, SuitProfile profile) {
        if (isTurnedOff(leggings(player))) return Mode.NONE;
        return strongest(profile.builtInAirConditioner(), moduleMode(player));
    }

    public static @Nullable AirConditioner poweredModuleInUse(Player player) {
        Optional<SuitProfile> worn = ThermalSuits.profileOf(player);
        if (worn.isEmpty() || isTurnedOff(leggings(player))) return null;

        AirConditioner module = poweredModule(player);
        if (module == null) return null;
        if (modeOf(module).ordinal() <= worn.get().builtInAirConditioner().ordinal()) return null;
        return module;
    }

    public static void toggle(ServerPlayer player) {
        if (!hasAirConditioner(player)) return;

        ItemStack leggings = leggings(player);
        boolean turnOff = !isTurnedOff(leggings);
        if (turnOff) TURNED_OFF.set(leggings, true);
        else TURNED_OFF.remove(leggings);

        PlayerInformPayload message = turnOff
                ? PlayerInformPayload.of(INFO_ID, 1000, ChatFormatting.RED,
                        new PlayerInformPayload.Translated("message.ntmthermalize.air_conditioner.off"))
                : PlayerInformPayload.of(INFO_ID, 1000, ChatFormatting.GREEN,
                        new PlayerInformPayload.Translated("message.ntmthermalize.air_conditioner.on"));
        GolemNetwork.sendToPlayer(player, PlayerInformPayload.CODEC, message);
    }

    private static boolean hasAirConditioner(Player player) {
        if (installedModule(leggings(player)) != null) return true;
        return ThermalSuits.profileOf(player)
                .map(profile -> profile.builtInAirConditioner() != Mode.NONE)
                .orElse(false);
    }

    private static Mode moduleMode(Player player) {
        AirConditioner module = poweredModule(player);
        return module == null ? Mode.NONE : modeOf(module);
    }

    private static Mode modeOf(AirConditioner module) {
        return module.equilibrium() ? Mode.EQUILIBRIUM : Mode.BASIC;
    }

    private static Mode strongest(Mode first, Mode second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }

    private static @Nullable AirConditioner poweredModule(Player player) {
        ItemStack leggings = leggings(player);
        AirConditioner module = installedModule(leggings);
        if (module == null) return null;
        if (!(leggings.getItem() instanceof ArmorFSBPowered) || !ArmorFSB.hasFSBArmor(player)) return null;
        return module;
    }

    private static @Nullable AirConditioner installedModule(ItemStack leggings) {
        return ArmorModHandler.modItem(leggings, ArmorModHandler.legs_only) instanceof AirConditioner module
                ? module : null;
    }

    private static boolean isTurnedOff(ItemStack leggings) {
        return TURNED_OFF.getOr(leggings, false);
    }

    private static ItemStack leggings(Player player) {
        return player.getItemBySlot(EquipmentSlot.LEGS);
    }
}
