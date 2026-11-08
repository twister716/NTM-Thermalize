package io.github.twister716.ntmthermalize.client;

import io.github.twister716.ntmthermalize.suit.SuitProfile;
import io.github.twister716.ntmthermalize.suit.ThermalSuits;
import io.github.twister716.ntmthermalize.temperature.AirConditioning;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;

public final class FullSetBonusTooltip {

    private static final String HEADER_KEY = "armor.fullSetBonus";
    private static final Set<String> RESISTANCE_KEYS = Set.of("damage.inset", "damage.item");
    private static final String BONUS_INDENT = "  ";

    private FullSetBonusTooltip() {}

    public static void addLines(ItemStack stack, List<Component> lines) {
        Optional<SuitProfile> found = ThermalSuits.profileOfPiece(stack.getItem());
        if (found.isEmpty() || !found.get().shownInTooltip()) return;

        List<Component> bonuses = bonuses(found.get());
        int header = headerIndex(lines);
        if (header < 0) {
            bonuses.add(0, Component.translatable(HEADER_KEY).withStyle(ChatFormatting.GOLD));
            lines.addAll(insertionIndex(lines), bonuses);
            return;
        }

        int end = header + 1;
        while (end < lines.size() && lines.get(end).getString().startsWith(BONUS_INDENT)) end++;
        lines.addAll(end, bonuses);
    }

    private static List<Component> bonuses(SuitProfile profile) {
        List<Component> bonuses = new ArrayList<>();
        if (profile.fixedBase() != null) {
            bonuses.add(bonus("armor.ntmthermalize.temperature_balance", ChatFormatting.GOLD));
        } else if (profile.stuffy()) {
            bonuses.add(bonus("armor.ntmthermalize.heat_retention", ChatFormatting.GOLD));
        }

        if (profile.builtInAirConditioner() == AirConditioning.Mode.BASIC) {
            bonuses.add(bonus("armor.ntmthermalize.air_conditioner", ChatFormatting.AQUA));
        } else if (profile.builtInAirConditioner() == AirConditioning.Mode.EQUILIBRIUM) {
            bonuses.add(bonus("armor.ntmthermalize.thermal_equilibrium_air_conditioner", ChatFormatting.AQUA));
        }

        bonuses.add(bonus("armor.ntmthermalize.temperature_limit", ChatFormatting.BLUE, profile.temperatureLimit()));
        return bonuses;
    }

    private static int insertionIndex(List<Component> lines) {
        for (int i = 1; i < lines.size(); i++) {
            Component line = lines.get(i);
            if (line.getString().isEmpty() || RESISTANCE_KEYS.contains(translationKey(line))) return i;
        }
        return lines.size();
    }

    private static int headerIndex(List<Component> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (HEADER_KEY.equals(translationKey(lines.get(i)))) return i;
        }
        return -1;
    }

    private static String translationKey(Component line) {
        return line.getContents() instanceof TranslatableContents contents ? contents.getKey() : "";
    }

    private static Component bonus(String key, ChatFormatting color, Object... args) {
        return Component.literal(BONUS_INDENT).append(Component.translatable(key, args)).withStyle(color);
    }
}
