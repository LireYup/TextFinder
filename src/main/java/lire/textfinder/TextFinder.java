package lire.textfinder;

import lire.textfinder.event.ClientEventHandler;
import lire.textfinder.event.CommandRegistrationHandler;
import lire.textfinder.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFinder implements ClientModInitializer {
    public static final String MOD_ID = "textfinder";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig config;

    @Override
    public void onInitializeClient() {
        config = ModConfig.load();
        LOGGER.info("配置加载完成");

        LOGGER.info("初始化告示牌搜索模组...");
        ClientEventHandler.registerEvents();
        CommandRegistrationHandler.register();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (config != null) {
                config.save();
                LOGGER.info("保存配置并关闭告示牌搜索模组");
            } else {
                LOGGER.warn("配置实例为空，跳过保存！");
            }
        });

        LOGGER.info("告示牌搜索模组初始化完成!");
    }

    // 安全获取客户端玩家（不变）
    public static ClientPlayerEntity getClientPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null ? client.player : null;
    }

    // 核心修改：改用 sendMessage 发送聊天消息（替代 sendCommand）
    public static void sendClientChatMessage(String message) {
        ClientPlayerEntity player = getClientPlayer();
        if (player == null) {
            LOGGER.warn("玩家未加载，无法发送聊天消息：{}", message);
            return;
        }
        // sendMessage 第一个参数：Text对象（必须，避免抽象方法错误）
        // 第二个参数：false = 玩家主动发送的聊天消息；true = 系统消息（仅自己可见）
        player.sendMessage(Text.literal(message), false);
    }

    // 安全构建Text（不变，确保Text实例化正确）
    public static Text buildText(String content) {
        return Text.literal(content);
    }
}