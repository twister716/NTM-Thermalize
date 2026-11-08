package io.github.twister716.ntmthermalize.temperature;

import com.hbm.handler.ArmorModHandler;
import io.github.twister716.ntmthermalize.item.ThermalCladding;
import io.github.twister716.ntmthermalize.suit.SuitProfile;
import io.github.twister716.ntmthermalize.suit.ThermalSuits;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import toughasnails.api.item.TANItems;
import toughasnails.api.temperature.IPlayerTemperatureModifier;
import toughasnails.api.temperature.TemperatureLevel;

public final class SuitTemperatureModifier implements IPlayerTemperatureModifier {

    private static final int STUFFINESS = 1;

    @Override
    public TemperatureLevel modify(Player player, TemperatureLevel current) {
        Optional<SuitProfile> worn = ThermalSuits.profileOf(player);
        if (worn.isEmpty()) return current;

        SuitProfile profile = worn.get();
        AirConditioning.Mode airConditioning = AirConditioning.mode(player, profile);
        TemperatureLevel base = profile.fixedBase() != null ? profile.fixedBase() : current;

        int limit = profile.temperatureLimit();
        int steps = airConditioning == AirConditioning.Mode.EQUILIBRIUM
                ? TemperatureLevel.NEUTRAL.ordinal() - base.ordinal()
                : (int) (claddingTotal(player) * profile.claddingMultiplier());
        TemperatureLevel adjusted = base.increment(Mth.clamp(steps, -limit, limit));

        boolean stuffy = profile.stuffy() && airConditioning == AirConditioning.Mode.NONE;
        TemperatureLevel result = stuffy ? adjusted.increment(STUFFINESS) : adjusted;
        return preventExtremes(result, current);
    }

    private static TemperatureLevel preventExtremes(TemperatureLevel result, TemperatureLevel outside) {
        if (result == TemperatureLevel.HOT && outside != TemperatureLevel.HOT) return TemperatureLevel.WARM;
        if (result == TemperatureLevel.ICY && outside != TemperatureLevel.ICY) return TemperatureLevel.COLD;
        return result;
    }

    private static double claddingTotal(Player player) {
        double total = 0;
        for (EquipmentSlot slot : ThermalSuits.ARMOR_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            if (ignoresCladding(armor)) continue;
            Item mod = ArmorModHandler.modItem(armor, ArmorModHandler.cladding);
            if (mod instanceof ThermalCladding cladding) total += cladding.temperature();
        }
        return total;
    }

    private static boolean ignoresCladding(ItemStack armor) {
        return armor.is(TANItems.WOOL_HELMET) || armor.is(TANItems.WOOL_CHESTPLATE)
                || armor.is(TANItems.WOOL_LEGGINGS) || armor.is(TANItems.WOOL_BOOTS)
                || armor.is(TANItems.LEAF_HELMET) || armor.is(TANItems.LEAF_CHESTPLATE)
                || armor.is(TANItems.LEAF_LEGGINGS) || armor.is(TANItems.LEAF_BOOTS);
    }
}
