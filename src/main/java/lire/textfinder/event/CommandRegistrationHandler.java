package lire.textfinder.event;

import lire.textfinder.command.TextFinderCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.CommandDispatcher;

public class CommandRegistrationHandler {
    // 注册客户端指令
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // 修复：TextFinderCommands → TextFinderCommand（类名拼写错误）
            TextFinderCommand.register(dispatcher);
        });
    }
}