package io.github.twister716.ntmthermalize.block;

import dev.golem.core.dejson.Models;
import dev.golem.core.dejson.Variants;
import dev.golem.core.registry.BlockEntities;
import dev.golem.core.registry.BlockTag;
import dev.golem.core.registry.DeferredRegister;
import dev.golem.core.registry.Loot;
import dev.golem.core.registry.Registrar;
import dev.golem.core.registry.RegistryObject;
import io.github.twister716.ntmthermalize.NTMThermalize;
import io.github.twister716.ntmthermalize.item.ThermalItems;
import io.github.twister716.ntmthermalize.item.ThermalTab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ThermalBlocks {

    private static final String NS = NTMThermalize.MOD_ID;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NS);
    public static final DeferredRegister.BlockEntityTypes BLOCK_ENTITIES = DeferredRegister.createBlockEntityTypes(NS);
    private static final Registrar REG = Registrar.of(BLOCKS, ThermalItems.ITEMS, BLOCK_ENTITIES);

    public static final RegistryObject<BlockRoomClimate> ROOM_COOLER = machine("room_cooler", RoomClimateKind.COOLER);
    public static final RegistryObject<BlockRoomClimate> ROOM_HEATER = machine("room_heater", RoomClimateKind.HEATER);
    public static final RegistryObject<BlockRoomClimate> CLIMATE_CONTROLLER =
            machine("climate_controller", RoomClimateKind.CONTROLLER);

    public static final RegistryObject<BlockItem> ROOM_COOLER_ITEM = REG.blockItem(ROOM_COOLER).tab(ThermalTab.TAB);
    public static final RegistryObject<BlockItem> ROOM_HEATER_ITEM = REG.blockItem(ROOM_HEATER).tab(ThermalTab.TAB);
    public static final RegistryObject<BlockItem> CLIMATE_CONTROLLER_ITEM =
            REG.blockItem(CLIMATE_CONTROLLER).tab(ThermalTab.TAB);

    public static final RegistryObject<BlockEntityType<TileEntityRoomClimate>> ROOM_CLIMATE_ENTITY =
            REG.blockEntity("room_climate", () -> BlockEntities.serverTicking(TileEntityRoomClimate::new,
                    TileEntityRoomClimate::serverTick,
                    ROOM_COOLER.get(), ROOM_HEATER.get(), CLIMATE_CONTROLLER.get()));

    private ThermalBlocks() {}

    public static void init() {}

    private static RegistryObject<BlockRoomClimate> machine(String name, RoomClimateKind kind) {
        String side = NS + ":blocks/" + name + "_side";
        String top = NS + ":blocks/" + name + "_top";
        return REG.block(name, p -> new BlockRoomClimate(kind, p),
                        p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops())
                .model(d -> d
                        .models(Models.group()
                                .and("", Models.cubeBottomTop(side, side, top))
                                .and("_active", Models.cubeBottomTop(side, side, top + "_active")))
                        .states(Variants.flag("active", "_active"))
                        .blockItem())
                .loot(Loot.self())
                .tag(BlockTag.PICKAXE);
    }
}
