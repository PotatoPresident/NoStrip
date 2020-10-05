package us.potatoboy.nostrip.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import javax.tools.Tool;

@Environment(EnvType.CLIENT)
public class NostripClient implements ClientModInitializer {
    private static KeyBinding keyBinding;
    private boolean doStrip = false;
    private TranslatableText on = new TranslatableText("text.nostrip.on");
    private TranslatableText off = new TranslatableText("text.nostrip.off");

    @Override
    public void onInitializeClient() {
        UseBlockCallback.EVENT.register(((playerEntity, world, hand, blockHitResult) -> {
            if (doStrip) return ActionResult.PASS;

            ItemStack stack = playerEntity.getStackInHand(hand);
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (stack.getItem() instanceof ToolItem) {
                if (blockState.isIn(BlockTags.LOGS)) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        }));

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
}
