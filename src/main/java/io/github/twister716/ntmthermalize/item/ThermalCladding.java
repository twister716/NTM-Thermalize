package io.github.twister716.ntmthermalize.item;

import com.hbm.api.data.ArmorDataMaps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ItemArmorMod;
import dev.golem.core.world.CompatTooltipContext;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ThermalCladding extends ItemArmorMod {

    private final double temperature;

    public ThermalCladding(double temperature, Item.Properties properties) {
        super(ArmorModHandler.cladding, true, true, true, true, properties);
        this.temperature = temperature;
    }

    public double temperature() {
        return temperature;
    }

    @Override
    protected void appendCompatHoverText(ItemStack stack, CompatTooltipContext context,
                                         List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ntmthermalize.temperature", signed(temperature))
                .withStyle(temperatureColor()));
        double radiation = ArmorDataMaps.cladding(this);
        if (radiation > 0) {
            tooltip.add(Component.translatable("tooltip.ntmthermalize.radiation", signed(radiation))
                    .withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.empty());
        super.appendCompatHoverText(stack, context, tooltip, flag);
    }

    /** One line on the armor, as NTM's own claddings show it: the name, then every effect in brackets. */
    @Override
    public void addArmorModDescription(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        MutableComponent effects = Component.translatable("tooltip.ntmthermalize.temperature", signed(temperature));
        double radiation = ArmorDataMaps.cladding(this);
        if (radiation > 0) {
            effects.append(", ").append(Component.translatable("tooltip.ntmthermalize.radiation.armor", signed(radiation)));
        }
        tooltip.add(Component.literal("  ").append(stack.getHoverName()).append(" (").append(effects).append(")")
                .withStyle(temperatureColor()));
    }

    private static String signed(double value) {
        return (value > 0 ? "+" : "") + value;
    }

    private ChatFormatting temperatureColor() {
        return temperature > 0 ? ChatFormatting.GOLD : ChatFormatting.AQUA;
    }
}
