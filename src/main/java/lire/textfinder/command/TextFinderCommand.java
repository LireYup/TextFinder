package lire.textfinder.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lire.textfinder.TextFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text; // 仅保留核心Text导入即可
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.List;

public class TextFinderCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("trf")
                .then(ClientCommandManager.argument("text", StringArgumentType.string())
                        .executes(context -> executeTextFind(context, StringArgumentType.getString(context, "text")))));

        dispatcher.register(ClientCommandManager.literal("td")
                .executes(TextFinderCommand::executeDebugFind));
    }

    private static int executeTextFind(CommandContext<FabricClientCommandSource> context, String targetText) {
        FabricClientCommandSource source = context.getSource();
        ClientPlayerEntity player = source.getPlayer();

        List<BlockPos> targetBlocks = findBlocksByText(player, targetText);

        if (TextFinder.config.isCGlow()) {
            triggerBlockGlow(player, targetBlocks);
            // 修复：直接传Text对象
            Text successMsg = Text.literal("已为" + targetBlocks.size() + "个方块添加发光效果！");
            source.sendFeedback(successMsg);
        }

        return 1;
    }

    private static int executeDebugFind(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        ClientPlayerEntity player = source.getPlayer();

        List<BlockPos> targetBlocks = findDebugBlocks(player);

        if (TextFinder.config.isCGlow()) {
            triggerBlockGlow(player, targetBlocks);
            // 修复：直接传Text对象
            Text debugMsg = Text.literal("调试模式：已为" + targetBlocks.size() + "个方块添加发光效果！");
            source.sendFeedback(debugMsg);
        }

        return 1;
    }

    private static void triggerBlockGlow(ClientPlayerEntity player, List<BlockPos> blocks) {
        if (blocks.isEmpty() || player == null) return;

        int glowSeconds = TextFinder.config.getCGlowTime();
        String glowColor = TextFinder.config.getCGlowColor();

        for (BlockPos pos : blocks) {
            String glowCommand = String.format(
                    "/cglow block %d %d %d %d color %s",
                    pos.getX(), pos.getY(), pos.getZ(),
                    glowSeconds, glowColor
            );
            Text commandText = Text.literal(glowCommand);
            // 主线程执行避免指令堆积
            MinecraftClient.getInstance().execute(() -> player.sendMessage(commandText, false));
        }
    }

    private static List<BlockPos> findBlocksByText(ClientPlayerEntity player, String targetText) {
        return List.of();
    }

    private static List<BlockPos> findDebugBlocks(ClientPlayerEntity player) {
        return List.of(player.getBlockPos());
    }
}