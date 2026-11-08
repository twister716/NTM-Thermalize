package io.github.twister716.ntmthermalize.suit;

import java.util.function.Supplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public record ThermalSuit(
        Supplier<? extends Item> helmet,
        Supplier<? extends Item> chestplate,
        Supplier<? extends Item> leggings,
        Supplier<? extends Item> boots,
        SuitProfile profile
) {

    public boolean isWornBy(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(helmet.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(chestplate.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(leggings.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(boots.get());
    }

    public boolean contains(Item item) {
        return item == helmet.get() || item == chestplate.get() || item == leggings.get() || item == boots.get();
    }
}
