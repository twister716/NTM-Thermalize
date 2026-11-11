package io.github.twister716.ntmthermalize.room;

import dev.golem.core.dejson.Tags;
import io.github.twister716.ntmthermalize.NTMThermalize;

/** The tags of the room climate machines, written out as JSON at build time. */
public final class RoomTags {

    private RoomTags() {}

    public static void declare() {
        Tags.of("block", NTMThermalize.MOD_ID, "seals_rooms")
                .optional("#c:glass_panes", "#forge:glass_panes",
                        "hbm:reinforced_glass", "hbm:reinforced_glass_pane", "hbm:reinforced_laminate_pane")
                .register();
    }
}
