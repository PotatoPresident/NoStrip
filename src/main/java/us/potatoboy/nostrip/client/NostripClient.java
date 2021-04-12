package us.potatoboy.nostrip.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolItem;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class NostripClient implements ClientModInitializer {
    private static KeyBinding keyBinding;
    private boolean doStrip = false;
    private TranslatableText on = new TranslatableText("text.nostrip.on");
    private TranslatableText off = new TranslatableText("text.nostrip.off");
    private static long lastMessage = 0;
    private static final int MESSAGE_REPEAT_TIME = 1000;
    private static NoStripConfig config;

    @Override
    public void onInitializeClient() {

        // Create config object from JSON
        try {
            config = NoStripConfig.read();
            doStrip = config.getStrip();
        }
        catch (IOException e) {
            LogManager.getLogger().info("Unable to find nostrip config file, creating");
            NoStripConfig.create();
        }

        UseBlockCallback.EVENT.register(((playerEntity, world, hand, blockHitResult) -> {
            if (!world.isClient) return ActionResult.PASS;
            if (doStrip) return ActionResult.PASS;

            ItemStack stack = playerEntity.getStackInHand(hand);
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (stack.getItem() instanceof ToolItem) {
                if (AxeItem.STRIPPED_BLOCKS.containsKey(blockState.getBlock())) {
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
            config.save(config);
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
                client.player.sendMessage(new TranslatableText("text.nostrip.toggle", doStrip ? on : off), true);
            }
        });
    }
    
    private void informPlayer(PlayerEntity player) {
        if (!config.sendFeedback() || System.currentTimeMillis() < lastMessage + MESSAGE_REPEAT_TIME) {
            return;
        }
        lastMessage = System.currentTimeMillis();
        Text message;
        if (KeyBindingHelper.getBoundKeyOf(keyBinding).equals(GLFW.GLFW_KEY_UNKNOWN)) {
            message = new TranslatableText("text.nostrip.prevented");
        } else {
            message = new TranslatableText("text.nostrip.enableby", KeyBindingHelper.getBoundKeyOf(keyBinding).getLocalizedText());
        }
        player.sendMessage(message, true);
    }
}
