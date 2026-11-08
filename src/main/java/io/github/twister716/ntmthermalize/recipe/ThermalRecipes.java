package io.github.twister716.ntmthermalize.recipe;

import static dev.golem.core.dejson.Crafting.rows;
import static dev.golem.core.dejson.Crafting.shaped;

import io.github.twister716.ntmthermalize.NTMThermalize;

public final class ThermalRecipes {

    private static final String NS = NTMThermalize.MOD_ID;

    private ThermalRecipes() {}

    public static void declare() {
        shaped(NS, "crafting/wool_insulation_cladding", NS + ":wool_insulation_cladding",
                rows("DWD", "WPW", "DWD"),
                "D", "hbm:ducttape", "W", "#minecraft:wool", "P", "#c:plates/iron");
        shaped(NS, "crafting/copper_cooling_cladding", NS + ":copper_cooling_cladding",
                rows("DCD", "CPC", "DCD"),
                "D", "hbm:ducttape", "C", "#c:plates/copper", "P", "#c:plates/iron");
    }
}
