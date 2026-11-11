package io.github.twister716.ntmthermalize.item;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.items.armor.ItemArmorMod;
import dev.golem.core.world.CompatTooltipContext;
import io.github.twister716.ntmthermalize.temperature.AirConditioning;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class AirConditioner extends ItemArmorMod {

    private static final int TICKS_PER_SECOND = 20;

    private final boolean equilibrium;
    private final long powerPerSecond;

    public AirConditioner(boolean equilibrium, long powerPerSecond, Item.Properties properties) {
        super(ArmorModHandler.legs_only, false, false, true, false, properties);
        this.equilibrium = equilibrium;
        this.powerPerSecond = powerPerSecond;
    }

    public boolean equilibrium() {
        return equilibrium;
    }

    @Override
    protected void appendCompatHoverText(ItemStack stack, CompatTooltipContext context,
                                         List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(descriptionKey()).withStyle(ChatFormatting.AQUA));
        tooltip.add(powerText());
        tooltip.add(Component.empty());
        super.appendCompatHoverText(stack, context, tooltip, flag);
    }

    @Override
    public void addArmorModDescription(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(Component.literal("  ").append(stack.getHoverName())
                .append(" (").append(Component.translatable("tooltip.ntmthermalize.power", powerPerSecond)).append(")")
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {
        if (entity.level().isClientSide() || !(entity instanceof Player player) || player.isCreative()) return;
        if (entity.level().getGameTime() % TICKS_PER_SECOND != 0) return;
        if (AirConditioning.poweredModuleInUse(player) != this) return;
        if (armor.getItem() instanceof ArmorFSBPowered powered) powered.dischargeBattery(armor, powerPerSecond);
    }

    private String descriptionKey() {
        return equilibrium
                ? "tooltip.ntmthermalize.thermal_equilibrium_air_conditioner"
                : "tooltip.ntmthermalize.air_conditioner";
    }

    private Component powerText() {
        return Component.translatable("tooltip.ntmthermalize.power", powerPerSecond).withStyle(ChatFormatting.YELLOW);
    }
}
