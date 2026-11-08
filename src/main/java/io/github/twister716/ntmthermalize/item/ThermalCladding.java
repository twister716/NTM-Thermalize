package io.github.twister716.ntmthermalize.item;

import com.hbm.api.data.ArmorDataMaps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ItemArmorMod;
import dev.golem.core.world.CompatTooltipContext;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
        tooltip.add(temperatureText());
        double radiation = ArmorDataMaps.cladding(this);
        if (radiation > 0) tooltip.add(radiationText(radiation));
        tooltip.add(Component.empty());
        super.appendCompatHoverText(stack, context, tooltip, flag);
    }

    @Override
    public void addArmorModDescription(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(Component.literal("  ").append(stack.getHoverName()).withStyle(temperatureColor()));
        tooltip.add(Component.literal("    ").append(temperatureText()));
        double radiation = ArmorDataMaps.cladding(this);
        if (radiation > 0) tooltip.add(Component.literal("    ").append(radiationText(radiation)));
    }

    private Component temperatureText() {
        String value = (temperature > 0 ? "+" : "") + temperature;
        return Component.translatable("tooltip.ntmthermalize.temperature", value).withStyle(temperatureColor());
    }

    private static Component radiationText(double radiation) {
        return Component.translatable("tooltip.ntmthermalize.radiation", "+" + radiation)
                .withStyle(ChatFormatting.YELLOW);
    }

    private ChatFormatting temperatureColor() {
        return temperature > 0 ? ChatFormatting.GOLD : ChatFormatting.AQUA;
    }
}
