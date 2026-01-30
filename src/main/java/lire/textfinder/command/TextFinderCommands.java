package lire.textfinder.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lire.textfinder.TextFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.List;

public class TextFinderCommand {
    // 注册客户端指令（替代服务端指令）
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        // 注册/trf客户端指令
        dispatcher.register(ClientCommandManager.literal("trf")
                .then(ClientCommandManager.argument("text", StringArgumentType.string())
                        .executes(context -> executeTextFind(context, StringArgumentType.getString(context, "text")))));

        // 注册/td客户端指令
        dispatcher.register(ClientCommandManager.literal("td")
                .executes(TextFinderCommand::executeDebugFind));
    }

    /**
     * 修复：客户端/trf指令逻辑（替换服务端API）
     */
    private static int executeTextFind(CommandContext<FabricClientCommandSource> context, String targetText) {
        FabricClientCommandSource source = context.getSource();
        ClientPlayerEntity player = source.getPlayer();
        if (player == null) {
            // 修复sendFeedback参数：使用Supplier<Text>
            source.sendFeedback(() -> Text.literal("仅玩家可执行该指令！"));
            return 0;
        }

        // 1. 筛选目标方块（替换为你的实际逻辑）
        List<BlockPos> targetBlocks = findBlocksByText(player, targetText);

        // 2. 配置开启则触发发光
        if (TextFinder.config.isCGlow()) {
            triggerBlockGlow(player, targetBlocks);
            // 修复sendFeedback参数格式
            source.sendFeedback(() -> Text.literal("已为" + targetBlocks.size() + "个方块添加发光效果！"));
        }

        return 1;
    }

    /**
     * 修复：客户端/td指令逻辑
     */
    private static int executeDebugFind(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        ClientPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("仅玩家可执行该指令！"));
            return 0;
        }

        List<BlockPos> targetBlocks = findDebugBlocks(player);

        if (TextFinder.config.isCGlow()) {
            triggerBlockGlow(player, targetBlocks);
            source.sendFeedback(() -> Text.literal("调试模式：已为" + targetBlocks.size() + "个方块添加发光效果！"));
        }

        return 1;
    }

    /**
     * 修复：客户端执行指令（替换不存在的sendCommand）
     * 客户端通过sendChatMessage模拟玩家输入指令
     */
    private static void triggerBlockGlow(ClientPlayerEntity player, List<BlockPos> blocks) {
        if (blocks.isEmpty() || player.getClient() == null) return;

        int glowSeconds = TextFinder.config.getCGlowTime();
        String glowColor = TextFinder.config.getCGlowColor();

        for (BlockPos pos : blocks) {
            String glowCommand = String.format(
                    "/cglow block %d %d %d %d color %s",
                    pos.getX(), pos.getY(), pos.getZ(),
                    glowSeconds, glowColor
            );
            // 修复：客户端模拟玩家发送指令（替代ServerPlayNetworkHandler.sendCommand）
            player.sendChatMessage(glowCommand);
            // 可选：添加微小延迟避免指令堆积
            // MinecraftClient.getInstance().execute(() -> player.sendChatMessage(glowCommand));
        }
    }

    // 示例：方块筛选逻辑（保留你的业务逻辑）
    private static List<BlockPos> findBlocksByText(ClientPlayerEntity player, String targetText) {
        // 替换为你的实际筛选逻辑（客户端版本）
        return List.of();
    }

    private static List<BlockPos> findDebugBlocks(ClientPlayerEntity player) {
        // 替换为你的实际调试逻辑
        return List.of(player.getBlockPos());
    }
}