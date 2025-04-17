package us.potatoboy.nostrip.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.io.File;

@Environment(EnvType.CLIENT)
public class NostripClient implements ClientModInitializer {
    private static KeyBinding keyBinding;
    private boolean doStrip = false;
    private final Text on = Text.translatable("text.nostrip.on");
    private final Text off = Text.translatable("text.nostrip.off");
    private static long lastMessage = 0;
    private static final int MESSAGE_REPEAT_TIME = 1000;
    private static NoStripConfig config;

    @Override
    public void onInitializeClient() {

        // Create config object from JSON
        config = NoStripConfig.loadConfig(new File(FabricLoader.getInstance().getConfigDir() + "/nostrip_config.json"));
        doStrip = config.isStripping();

        UseBlockCallback.EVENT.register(((playerEntity, world, hand, blockHitResult) -> {
            if (!world.isClient) return ActionResult.PASS;
            if (doStrip) return ActionResult.PASS;

            ItemStack stack = playerEntity.getStackInHand(hand);
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (stack.getComponents().contains(DataComponentTypes.TOOL)) {
                if (AxeItem.STRIPPED_BLOCKS.containsKey(blockState.getBlock())
                        || Oxidizable.OXIDATION_LEVEL_DECREASES.get().containsKey(blockState.getBlock())
                        || HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().containsKey(blockState.getBlock())
                ) {
                    informPlayer(playerEntity);
                    return ActionResult.FAIL;
                }
            }

            if (stack.getItem() instanceof ShovelItem) {
                if (ShovelItem.PATH_STATES.containsKey(blockState.getBlock())) {
                    informPlayer(playerEntity);
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        }));
        ClientLifecycleEvents.CLIENT_STOPPING.register((MinecraftClient client) -> {
            config.saveConfig(new File(FabricLoader.getInstance().getConfigDir() + "/nostrip_config.json"));
        });

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nostrip.togglestrip",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.nostrip.title"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                doStrip = !doStrip;
                client.player.sendMessage(Text.translatable("text.nostrip.toggle", doStrip ? on : off), true);
            }
        });
    }

    private void informPlayer(PlayerEntity player) {
        if (!config.isFeedback() || System.currentTimeMillis() < lastMessage + MESSAGE_REPEAT_TIME) {
            return;
        }
        lastMessage = System.currentTimeMillis();
        Text message;
        if (KeyBindingHelper.getBoundKeyOf(keyBinding).getCode() == GLFW.GLFW_KEY_UNKNOWN) {
            message = Text.translatable("text.nostrip.prevented");
        } else {
            message = Text.translatable("text.nostrip.enableby", KeyBindingHelper.getBoundKeyOf(keyBinding).getLocalizedText());
        }
        player.sendMessage(message, true);
    }
}
