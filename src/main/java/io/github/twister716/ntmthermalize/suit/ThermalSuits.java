package io.github.twister716.ntmthermalize.suit;

import static io.github.twister716.ntmthermalize.suit.SuitProfile.DEFAULT_LIMIT;
import static io.github.twister716.ntmthermalize.suit.SuitProfile.euphemium;
import static io.github.twister716.ntmthermalize.suit.SuitProfile.fireProximity;
import static io.github.twister716.ntmthermalize.suit.SuitProfile.sealed;
import static io.github.twister716.ntmthermalize.suit.SuitProfile.sealedWithAirConditioner;
import static io.github.twister716.ntmthermalize.suit.SuitProfile.sealedWithEquilibriumAirConditioner;

import com.hbm.items.ModArmorItems;
import com.hbm.items.ModArmorModItems;
import com.hbm.items.ModArmorSetBItems;
import com.hbm.items.ModArmorSetItems;
import com.hbm.items.ModGearItems;
import dev.golem.core.world.Armors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public final class ThermalSuits {

    public static final List<EquipmentSlot> ARMOR_SLOTS = List.of(
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    );

    private static final List<ThermalSuit> SUITS = List.of(
            suit(ModGearItems.hazmat_helmet, ModGearItems.hazmat_plate,
                    ModGearItems.hazmat_legs, ModGearItems.hazmat_boots, sealed(DEFAULT_LIMIT)),
            suit(ModGearItems.hazmat_helmet_red, ModGearItems.hazmat_plate_red,
                    ModGearItems.hazmat_legs_red, ModGearItems.hazmat_boots_red, sealed(DEFAULT_LIMIT)),
            suit(ModGearItems.hazmat_helmet_grey, ModGearItems.hazmat_plate_grey,
                    ModGearItems.hazmat_legs_grey, ModGearItems.hazmat_boots_grey, sealed(DEFAULT_LIMIT)),
            suit(ModArmorModItems.hazmat_paa_helmet, ModArmorModItems.hazmat_paa_plate,
                    ModArmorModItems.hazmat_paa_legs, ModArmorModItems.hazmat_paa_boots, sealed(DEFAULT_LIMIT)),
            suit(ModArmorModItems.liquidator_helmet, ModArmorModItems.liquidator_plate,
                    ModArmorModItems.liquidator_legs, ModArmorModItems.liquidator_boots, sealed(2)),
            suit(ModArmorItems.taurun_helmet, ModArmorItems.taurun_plate,
                    ModArmorItems.taurun_legs, ModArmorItems.taurun_boots, sealed(DEFAULT_LIMIT)),
            suit(ModArmorSetItems.trenchmaster_helmet, ModArmorSetItems.trenchmaster_plate,
                    ModArmorSetItems.trenchmaster_legs, ModArmorSetItems.trenchmaster_boots, sealed(DEFAULT_LIMIT)),

            suit(ModArmorSetBItems.envsuit_helmet, ModArmorSetBItems.envsuit_plate,
                    ModArmorSetBItems.envsuit_legs, ModArmorSetBItems.envsuit_boots,
                    sealedWithAirConditioner(DEFAULT_LIMIT)),
            suit(ModArmorItems.asbestos_helmet, ModArmorItems.asbestos_plate,
                    ModArmorItems.asbestos_legs, ModArmorItems.asbestos_boots, fireProximity(2)),
            suit(ModArmorSetBItems.steamsuit_helmet, ModArmorSetBItems.steamsuit_plate,
                    ModArmorSetBItems.steamsuit_legs, ModArmorSetBItems.steamsuit_boots, sealed(DEFAULT_LIMIT)),
            suit(ModArmorSetBItems.dieselsuit_helmet, ModArmorSetBItems.dieselsuit_plate,
                    ModArmorSetBItems.dieselsuit_legs, ModArmorSetBItems.dieselsuit_boots, sealed(2)),
            suit(ModArmorSetItems.hev_helmet, ModArmorSetItems.hev_plate,
                    ModArmorSetItems.hev_legs, ModArmorSetItems.hev_boots, sealedWithAirConditioner(2)),

            suit(ModArmorItems.t51_helmet, ModArmorItems.t51_plate,
                    ModArmorItems.t51_legs, ModArmorItems.t51_boots, sealed(DEFAULT_LIMIT)),
            suit(ModArmorSetItems.ajr_helmet, ModArmorSetItems.ajr_plate,
                    ModArmorSetItems.ajr_legs, ModArmorSetItems.ajr_boots, sealed(2)),
            suit(ModArmorSetItems.ajro_helmet, ModArmorSetItems.ajro_plate,
                    ModArmorSetItems.ajro_legs, ModArmorSetItems.ajro_boots, sealed(2)),
            suit(ModArmorItems.rpa_helmet, ModArmorItems.rpa_plate,
                    ModArmorItems.rpa_legs, ModArmorItems.rpa_boots, sealedWithAirConditioner(2)),
            suit(ModArmorItems.ncrpa_helmet, ModArmorItems.ncrpa_plate,
                    ModArmorItems.ncrpa_legs, ModArmorItems.ncrpa_boots, sealed(2)),
            suit(ModArmorItems.bj_helmet, ModArmorItems.bj_plate,
                    ModArmorItems.bj_legs, ModArmorItems.bj_boots, sealed(2)),
            suit(ModArmorItems.dns_helmet, ModArmorItems.dns_plate,
                    ModArmorItems.dns_legs, ModArmorItems.dns_boots, sealedWithEquilibriumAirConditioner(2)),
            suit(ModArmorSetItems.fau_helmet, ModArmorSetItems.fau_plate,
                    ModArmorSetItems.fau_legs, ModArmorSetItems.fau_boots, sealedWithEquilibriumAirConditioner(2)),

            suit(ModArmorSetItems.euphemium_helmet, ModArmorSetItems.euphemium_plate,
                    ModArmorSetItems.euphemium_legs, ModArmorSetItems.euphemium_boots, euphemium())
    );

    private ThermalSuits() {}

    /** Suits by helmet and profiles by piece, built on first use: the items exist only once registration is over. */
    private static volatile @Nullable Map<Item, ThermalSuit> byHelmet;
    private static volatile @Nullable Map<Item, SuitProfile> byPiece;

    public static Optional<SuitProfile> profileOf(Player player) {
        ThermalSuit suit = byHelmet().get(player.getItemBySlot(EquipmentSlot.HEAD).getItem());
        if (suit != null && suit.isWornBy(player)) return Optional.of(suit.profile());
        if (wearsFullArmor(player)) return Optional.of(SuitProfile.otherFullSet());
        return Optional.empty();
    }

    public static Optional<SuitProfile> profileOfPiece(Item item) {
        return Optional.ofNullable(byPiece().get(item));
    }

    private static Map<Item, ThermalSuit> byHelmet() {
        Map<Item, ThermalSuit> map = byHelmet;
        if (map == null) {
            Map<Item, ThermalSuit> built = new HashMap<>();
            for (ThermalSuit suit : SUITS) built.put(suit.helmet().get(), suit);
            byHelmet = map = built;
        }
        return map;
    }

    private static Map<Item, SuitProfile> byPiece() {
        Map<Item, SuitProfile> map = byPiece;
        if (map == null) {
            Map<Item, SuitProfile> built = new HashMap<>();
            for (ThermalSuit suit : SUITS) {
                for (Supplier<? extends Item> piece : List.of(suit.helmet(), suit.chestplate(), suit.leggings(),
                        suit.boots())) {
                    built.putIfAbsent(piece.get(), suit.profile());
                }
            }
            byPiece = map = built;
        }
        return map;
    }

    private static boolean wearsFullArmor(Player player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!Armors.isArmor(player.getItemBySlot(slot))) return false;
        }
        return true;
    }

    private static ThermalSuit suit(Supplier<? extends Item> helmet, Supplier<? extends Item> chestplate,
                                    Supplier<? extends Item> leggings, Supplier<? extends Item> boots,
                                    SuitProfile profile) {
        return new ThermalSuit(helmet, chestplate, leggings, boots, profile);
    }
}
