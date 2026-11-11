package io.github.twister716.ntmthermalize.item;

import com.hbm.items.ModelState;
import com.hbm.packet.PlayerInformPayload;
import com.mojang.serialization.Codec;
import dev.golem.core.data.GolemComponent;
import dev.golem.core.net.GolemNetwork;
import dev.golem.core.world.CompatItem;
import io.github.twister716.ntmthermalize.NTMThermalize;
import io.github.twister716.ntmthermalize.temperature.RoomTemperature;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import toughasnails.api.temperature.TemperatureLevel;

/**
 * Shows the temperature around the player while it is anywhere in the inventory: a reading in the top left, updated
 * every second, and a needle that swings toward it one step at a time. The reading is the temperature before armor,
 * the same one the body temperature starts from.
 */
public class RoomThermometer extends CompatItem implements ModelState {

    /** The needle's position; see {@link ThermometerNeedle}. */
    public static final GolemComponent<Integer> NEEDLE =
            GolemComponent.of(NTMThermalize.MOD_ID, "room_thermometer_needle", Codec.INT);
    public static final GolemComponent<Integer> TARGET =
            GolemComponent.of(NTMThermalize.MOD_ID, "room_thermometer_target", Codec.INT);

    private static final int INFO_ID = 71601;
    private static final int UPDATE_TICKS = 20;
    private static final int NEEDLE_TICKS = 2;
    private static final int DISPLAY_MILLIS = 4000;

    public RoomThermometer(Item.Properties properties) {
        super(properties);
    }

    public static void init() {}

    @Override
    public int modelState(ItemStack stack) {
        return ThermometerNeedle.modelOfPosition(NEEDLE.getOr(stack, ThermometerNeedle.NEUTRAL));
    }

    @Override
    public boolean compatIsSameHeldItem(ItemStack shown, ItemStack held) {
        return shown.getItem() == held.getItem();
    }

    @Override
    protected void compatInventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot,
                                       boolean held) {
        if (!(owner instanceof ServerPlayer player)) return;
        long time = level.getGameTime();
        if (time % UPDATE_TICKS == 0) read(stack, player);
        if (time % NEEDLE_TICKS == 0) swingNeedle(stack);
    }

    private static void read(ItemStack stack, ServerPlayer player) {
        TemperatureLevel temperature = RoomTemperature.at(player);
        int target = temperature.ordinal() * ThermometerNeedle.STEPS_PER_LEVEL;
        if (TARGET.getOr(stack, ThermometerNeedle.NEUTRAL) != target) TARGET.set(stack, target);

        GolemNetwork.sendToPlayer(player, PlayerInformPayload.CODEC, PlayerInformPayload.of(INFO_ID, DISPLAY_MILLIS,
                ChatFormatting.RESET,
                new PlayerInformPayload.Translated("message.ntmthermalize.room_temperature"),
                new PlayerInformPayload.Colored(new PlayerInformPayload.Translated(temperatureKey(temperature)),
                        colorOf(temperature))));
    }

    private static void swingNeedle(ItemStack stack) {
        int needle = NEEDLE.getOr(stack, ThermometerNeedle.NEUTRAL);
        int target = TARGET.getOr(stack, ThermometerNeedle.NEUTRAL);
        if (needle != target) NEEDLE.set(stack, needle + Integer.signum(target - needle));
    }

    private static String temperatureKey(TemperatureLevel temperature) {
        return "temperature.ntmthermalize." + temperature.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static ChatFormatting colorOf(TemperatureLevel temperature) {
        return switch (temperature) {
            case ICY -> ChatFormatting.BLUE;
            case COLD -> ChatFormatting.AQUA;
            case NEUTRAL -> ChatFormatting.GREEN;
            case WARM -> ChatFormatting.GOLD;
            case HOT -> ChatFormatting.RED;
        };
    }
}
