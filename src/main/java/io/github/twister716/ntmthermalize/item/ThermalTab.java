package io.github.twister716.ntmthermalize.item;

import dev.golem.core.registry.DeferredRegister;
import dev.golem.core.registry.RegistryObject;
import dev.golem.core.registry.TabTarget;
import dev.golem.core.registry.Tabs;
import io.github.twister716.ntmthermalize.NTMThermalize;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The mod's own creative tab, "HBM Addon: Thermalize". A registration joins it with {@code .tab(ThermalTab.TAB)} and
 * shows up in the order it was registered.
 */
public final class ThermalTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(() -> Registries.CREATIVE_MODE_TAB, NTMThermalize.MOD_ID);

    private static final List<RegistryObject<? extends Item>> CONTENTS = new ArrayList<>();

    /** Where {@code .tab(...)} puts an item: at the end of this tab. */
    public static final TabTarget TAB = CONTENTS::add;

    public static final RegistryObject<CreativeModeTab> THERMALIZE = TABS.register("thermalize",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.ntmthermalize"))
                    .icon(() -> new ItemStack(ThermalItems.ROOM_THERMOMETER.get()))
                    .build());

    private ThermalTab() {}

    public static void init() {
        Tabs.modifyEntries(THERMALIZE.getKey(),
                entries -> CONTENTS.forEach(item -> entries.accept(new ItemStack(item.get()))));
    }
}
