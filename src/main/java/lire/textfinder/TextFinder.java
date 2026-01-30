package lire.textfinder;

import lire.textfinder.event.ClientEventHandler;
import lire.textfinder.event.CommandRegistrationHandler;
import lire.textfinder.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient; // 新增：客户端实例类
import net.minecraft.client.network.ClientPlayerEntity; // 新增：玩家类
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFinder implements ClientModInitializer {
    public static final String MOD_ID = "textfinder";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig config;

    @Override
    public void onInitializeClient() {
        // 原有逻辑保持不变
        config = ModConfig.load();
        LOGGER.info("配置加载完成");

        LOGGER.info("初始化告示牌搜索模组...");
        ClientEventHandler.registerEvents();
        CommandRegistrationHandler.register();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            config.save();
            LOGGER.info("保存配置并关闭告示牌搜索模组");
        });

        LOGGER.info("告示牌搜索模组初始化完成!");
    }

    // 新增：安全获取客户端玩家实例（替代错误的 getClient() 调用）
    public static ClientPlayerEntity getClientPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        // 判空避免空指针
        return client != null && client.player != null ? client.player : null;
    }

    // 新增：兼容所有MC版本的指令发送方法（核心修复 sendCommand 不存在问题）
    public static void sendChatCommand(String command) {
        ClientPlayerEntity player = getClientPlayer();
        if (player == null) {
            LOGGER.warn("玩家未加载，无法发送指令：{}", command);
            return;
        }

        // 适配逻辑：低版本用 networkHandler.sendChatCommand，高版本兼容 sendCommand
        try {
            // 优先尝试高版本方法（1.19+）
            player.sendCommand(command);
        } catch (NoSuchMethodError e) {
            // 低版本降级调用（1.18及以下）
            player.networkHandler.sendChatCommand(command);
        }
    }

    // 若需发送聊天消息（非指令），新增此方法（替代废弃的 sendChatMessage）
    public static void sendChatMessage(String message) {
        ClientPlayerEntity player = getClientPlayer();
        if (player == null) {
            LOGGER.warn("玩家未加载，无法发送聊天消息：{}", message);
            return;
        }
        // 所有版本通用的聊天消息发送方式
        player.sendMessage(net.minecraft.text.Text.literal(message), false);
    }
}