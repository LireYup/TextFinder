package lire.textfinder.event;

import lire.textfinder.TextFinder;
import lire.textfinder.search.SignSearchManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * 客户端事件处理器，管理搜索过程和数据清除
 */
public class ClientEventHandler {
    public static void registerEvents() {
        // 世界加载时清除搜索结果
        ServerLevelEvents.LOAD.register((server, world) -> {
            if (Thread.currentThread() == Minecraft.getInstance().getRunningThread()) {
                SignSearchManager.getInstance().clearFoundSigns();
            }
        });

        // 世界卸载时清除搜索结果
        ServerLevelEvents.UNLOAD.register((server, world) -> {
            if (Thread.currentThread() == Minecraft.getInstance().getRunningThread()) {
                SignSearchManager.getInstance().clearFoundSigns();
            }
        });

        // 注册客户端Tick事件，处理搜索逻辑
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null) {
                SignSearchManager searchManager = SignSearchManager.getInstance();
                if (searchManager.isSearching()) {
                    searchManager.tickSearch();
                    handleDebugOutput(searchManager, client);
                }
            }
        });
    }

    /**
     * 处理调试模式下的进度输出
     */
    private static void handleDebugOutput(SignSearchManager searchManager, Minecraft client) {
        // 修复：调用标准驼峰方法名
        int outputComplexity = TextFinder.config.getOutputComplexity();
        if (outputComplexity == 4 && client.player != null) {
            int debugInterval = TextFinder.config.getDebugPgt();
            int totalChecked = searchManager.getTotalSignsChecked();

            if (totalChecked % debugInterval == 0 && totalChecked > 0) {
                int found = searchManager.getFoundSigns().size();
                // Use translatable text so the message is localized (was hardcoded Chinese)
                client.player.sendSystemMessage(
                        Component.translatable("textfinder.command.display.found_progress", found, totalChecked)
                );
            }
        }
    }
}