package io.github.twister716.ntmthermalize.block;

import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.machine.IItemFluidId;
import com.hbm.util.BobMathUtil;
import dev.golem.core.registry.TickingBlock;
import dev.golem.core.world.CompatBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * A room cooler, room heater or climate controller. It cools, heats or holds at neutral the sealed room it stands in;
 * looking at it shows the room, its power and coolant, and what the machines of the room do to it. While it is
 * {@link #ACTIVE} (running) the lamp on top blinks. A fluid identifier sets the cooler's coolant.
 */
public class BlockRoomClimate extends CompatBlock implements TickingBlock, ILookOverlay {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final int TITLE_COLOR = 0xffff00;
    private static final int BACKGROUND_COLOR = 0x404000;

    private final RoomClimateKind kind;

    public BlockRoomClimate(RoomClimateKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    public RoomClimateKind kind() {
        return kind;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityRoomClimate(pos, state);
    }

    @Override
    protected InteractionResult onUseItem(BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        if (kind != RoomClimateKind.COOLER || !(stack.getItem() instanceof IItemFluidId identifier)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof TileEntityRoomClimate machine)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        var type = identifier.getType(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        if (machine.setCoolant(type)) {
            player.sendSystemMessage(Component.translatable("message.ntmthermalize.coolant_set",
                    type.getDisplayName()).withStyle(ChatFormatting.YELLOW));
        } else {
            player.sendSystemMessage(Component.translatable("message.ntmthermalize.coolant_invalid",
                    type.getDisplayName()).withStyle(ChatFormatting.RED));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Data getLookOverlay(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof TileEntityRoomClimate machine)) return null;

        List<String> text = new ArrayList<>();
        if (machine.status() == TileEntityRoomClimate.Status.NOT_SEALED) {
            text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! "
                    + translate("overlay.ntmthermalize.not_sealed") + " ! ! !");
        } else {
            text.add(translate("overlay.ntmthermalize.volume", machine.volume()));
            text.add(translate("overlay.ntmthermalize.needed", machine.needed()));
            text.add(translate("overlay.ntmthermalize.running", machine.runningInRoom()));
            text.add(effectLine(machine));
            if (machine.status() != TileEntityRoomClimate.Status.RUNNING) text.add(statusLine(machine));
        }
        text.add(ChatFormatting.YELLOW + translate("overlay.ntmthermalize.power",
                BobMathUtil.getShortNumber(machine.getPower()),
                BobMathUtil.getShortNumber(TileEntityRoomClimate.MAX_POWER)));
        if (machine.usesCoolant()) {
            text.add(tankLine(ChatFormatting.GREEN + "-> ", machine.coolant));
            text.add(tankLine(ChatFormatting.RED + "<- ", machine.steam));
        }
        return new Data(Component.translatable(getDescriptionId()).getString(),
                TITLE_COLOR, BACKGROUND_COLOR, text);
    }

    private String effectLine(TileEntityRoomClimate machine) {
        if (machine.levels() == 0) return ChatFormatting.RED + translate("overlay.ntmthermalize.too_few");
        return switch (kind) {
            case COOLER -> ChatFormatting.AQUA + translate("overlay.ntmthermalize.cooling", machine.levels());
            case HEATER -> ChatFormatting.GOLD + translate("overlay.ntmthermalize.heating", machine.levels());
            case CONTROLLER -> ChatFormatting.GREEN + translate("overlay.ntmthermalize.controlling");
        };
    }

    private static String statusLine(TileEntityRoomClimate machine) {
        String key = switch (machine.status()) {
            case NO_AIR -> "overlay.ntmthermalize.no_air";
            case ALREADY_ICY -> "overlay.ntmthermalize.already_icy";
            case ALREADY_HOT -> "overlay.ntmthermalize.already_hot";
            case ALREADY_NEUTRAL -> "overlay.ntmthermalize.already_neutral";
            case NO_POWER -> "overlay.ntmthermalize.no_power";
            case NO_COOLANT -> "overlay.ntmthermalize.no_coolant";
            case STEAM_FULL -> "overlay.ntmthermalize.steam_full";
            case RUNNING, NOT_SEALED -> "";
        };
        return ChatFormatting.GRAY + translate(key);
    }

    /** NTM's look overlay form: a green arrow for a tank that takes fluid in, a red one for a tank that gives out. */
    private static String tankLine(String arrow, FluidTankNTM tank) {
        return arrow + ChatFormatting.RESET + tank.getTankType().getDisplayName().getString() + ": " + tank.getFill()
                + "/" + tank.getMaxFill() + "mB";
    }

    private static String translate(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }
}
