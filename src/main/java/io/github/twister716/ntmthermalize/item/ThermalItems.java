package io.github.twister716.ntmthermalize.item;

import com.hbm.registration.Presets;
import dev.golem.core.dejson.Models;
import dev.golem.core.registry.DeferredRegister;
import dev.golem.core.registry.Registrar;
import dev.golem.core.registry.RegistryObject;
import io.github.twister716.ntmthermalize.NTMThermalize;

public final class ThermalItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NTMThermalize.MOD_ID);
    private static final Registrar REG = Registrar.of(ITEMS);

    public static final RegistryObject<ThermalCladding> WOOL_INSULATION_CLADDING =
            cladding("wool_insulation_cladding", 0.25);
    public static final RegistryObject<ThermalCladding> ASBESTOS_INSULATION_CLADDING =
            cladding("asbestos_insulation_cladding", 0.5);
    public static final RegistryObject<ThermalCladding> FERROURANIUM_INSULATION_CLADDING =
            cladding("ferrouranium_insulation_cladding", 0.5);

    public static final RegistryObject<ThermalCladding> COPPER_COOLING_CLADDING =
            cladding("copper_cooling_cladding", -0.25);
    public static final RegistryObject<ThermalCladding> GRAPHITE_COOLING_CLADDING =
            cladding("graphite_cooling_cladding", -0.5);
    public static final RegistryObject<ThermalCladding> FERROURANIUM_COOLING_CLADDING =
            cladding("ferrouranium_cooling_cladding", -0.5);

    public static final RegistryObject<AirConditioner> AIR_CONDITIONER =
            airConditioner("air_conditioner", false, 10);
    public static final RegistryObject<AirConditioner> THERMAL_EQUILIBRIUM_AIR_CONDITIONER =
            airConditioner("thermal_equilibrium_air_conditioner", true, 25);

    public static final RegistryObject<RoomThermometer> ROOM_THERMOMETER = roomThermometer();

    private ThermalItems() {}

    public static void init() {
        RoomThermometer.init();
    }

    private static RegistryObject<ThermalCladding> cladding(String name, double temperature) {
        return REG.item(name, p -> new ThermalCladding(temperature, p), p -> p.stacksTo(1))
                .transform(Presets.sprite())
                .tab(ThermalTab.TAB);
    }

    private static RegistryObject<AirConditioner> airConditioner(String name, boolean equilibrium,
                                                                 long powerPerSecond) {
        return REG.item(name, p -> new AirConditioner(equilibrium, powerPerSecond, p), p -> p.stacksTo(1))
                .transform(Presets.sprite())
                .tab(ThermalTab.TAB);
    }

    private static RegistryObject<RoomThermometer> roomThermometer() {
        String texture = NTMThermalize.MOD_ID + ":items/room_thermometer";
        Models.Group needles = Models.group().and("", Models.generated(texture));
        for (int model = 1; model < ThermometerNeedle.POSITIONS; model++) {
            String suffix = String.format("_%02d", ThermometerNeedle.positionOfModel(model));
            needles = needles.and(suffix, Models.generated(texture + suffix));
        }
        Models.Group allNeedles = needles;
        return REG.item("room_thermometer", RoomThermometer::new, p -> p.stacksTo(1))
                .icon(item -> item.models(allNeedles).selects("hbm:model_state"))
                .tab(ThermalTab.TAB);
    }
}
