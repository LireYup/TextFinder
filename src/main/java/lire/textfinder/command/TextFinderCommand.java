package lire.textfinder.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lire.textfinder.TextFinder;
import net.fabricmc.loader.api.FabricLoader;
import lire.textfinder.search.SignSearchManager;
import lire.textfinder.I18nHelper;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
// 修改导入为greedyString
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class TextFinderCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        // 主指令节点
        dispatcher.register(literal("textfinder")
                .then(literal("search")
                        .then(argument("keyword", greedyString())  // 修改为greedyString
                                .executes(TextFinderCommand::searchCommand)))
                .then(literal("display")
                        .executes(TextFinderCommand::displayCommand))
                .then(literal("refilter")
                        .then(argument("newKeyword", greedyString())  // 修改为greedyString
                                .executes(TextFinderCommand::refilterCommand)))
                .then(literal("clear")
                        .executes(TextFinderCommand::clearCommand)));

        // 简写指令
        dispatcher.register(literal("tf")
                .then(argument("keyword", greedyString())  // 修改为greedyString
                        .executes(TextFinderCommand::searchCommand)));

        dispatcher.register(literal("td")
                .executes(TextFinderCommand::displayCommand)
                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(TextFinderCommand::displayCommand)));

        dispatcher.register(literal("trf")
                .then(argument("newKeyword", greedyString())  // 修改为greedyString
                        .executes(TextFinderCommand::refilterCommand)));

        // 测试指令：/ttest - 输出当前配置的全部项
        dispatcher.register(literal("ttest")
                .executes(TextFinderCommand::ttestCommand));

        // /tglow <n> - 如果安装了 clientcommands，则执行 /cglow block <x> <y> <z> <cGlowTime> <cGlowColor>
        dispatcher.register(literal("tglow")
                .then(argument("n", IntegerArgumentType.integer())
                        .executes(TextFinderCommand::tglowCommand)));
    }

    private static int searchCommand(CommandContext<FabricClientCommandSource> context) {
        String keyword = StringArgumentType.getString(context, "keyword");
        FabricClientCommandSource source = context.getSource();

        source.getPlayer();

        SignSearchManager.getInstance().startSearch(keyword);
        source.sendFeedback(Text.literal(I18nHelper.translate("textfinder.command.search.start", keyword)));
        return 1;
    }

    // 关键修复：调用新的outputSearchResults方法，直接传入命令源
    private static int displayCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        SignSearchManager manager = SignSearchManager.getInstance();

        int page = 1;
        try {
            page = IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException e) {
            // 如果没有提供 page 参数，使用默认值 1
        }

        // 直接调用管理器的输出方法，传入命令源和页数
        manager.outputSearchResults(source, page);
        return 1;
    }

    private static int refilterCommand(CommandContext<FabricClientCommandSource> context) {
        String newKeyword = StringArgumentType.getString(context, "newKeyword");
        FabricClientCommandSource source = context.getSource();

        SignSearchManager manager = SignSearchManager.getInstance();
        manager.refilterSigns(newKeyword);

        source.sendFeedback(Text.literal(I18nHelper.translate("textfinder.command.refilter.success", newKeyword)));
        return displayCommand(context);
    }

    private static int clearCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        SignSearchManager.getInstance().clearFoundSigns();
        source.sendFeedback(Text.literal(I18nHelper.translate("textfinder.command.clear.success")));
        return 1;
    }

    private static int ttestCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        if (TextFinder.config == null) {
            source.sendFeedback(Text.literal("&b[TextFinder]&cConfig is not loaded"));
            return 1;
        }

        // 输出配置的各项值
        source.sendFeedback(Text.literal("maxSearchAmountPerTick: " + TextFinder.config.getMaxSearchAmountPerTick()));
        source.sendFeedback(Text.literal("outputComplexity: " + TextFinder.config.getOutputComplexity()));
        source.sendFeedback(Text.literal("debugPgt: " + TextFinder.config.getDebugPgt()));
        source.sendFeedback(Text.literal("cGlowTime: " + TextFinder.config.getCGlowTime()));
        source.sendFeedback(Text.literal("cGlowColor: " + TextFinder.config.getCGlowColor()));
        source.sendFeedback(Text.literal("searchRange: " + TextFinder.config.getSearchRange()));

        // 指示是否安装 clientcommands（直接输出英文标识）
        boolean hasClientCommands = FabricLoader.getInstance().isModLoaded("clientcommands");
        source.sendFeedback(Text.literal("withClientCommands=" + hasClientCommands));

        return 1;
    }

    private static int tglowCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();

        boolean hasClientCommands = FabricLoader.getInstance().isModLoaded("clientcommands");
        if (!hasClientCommands) {
            source.sendFeedback(Text.literal("Clientcommands is not installed"));
            return 1;
        }

        int n = 1;
        try {
            n = IntegerArgumentType.getInteger(context, "n");
        } catch (IllegalArgumentException ignored) {
        }

        // Use found signs list rather than player position
        var manager = SignSearchManager.getInstance();
        var results = manager.getFoundSigns();
        if (results.isEmpty()) {
            source.sendFeedback(Text.literal("No found signs to target"));
            return 1;
        }

        if (n < 1 || n > results.size()) {
            source.sendFeedback(Text.literal("Index out of range: " + n + " (found " + results.size() + ")"));
            return 1;
        }

        var sign = results.get(n - 1);
        int x = sign.pos().getX();
        int y = sign.pos().getY();
        int z = sign.pos().getZ();

        int cGlowTime = TextFinder.config.getCGlowTime();
        String cGlowColor = TextFinder.config.getCGlowColor();

        String cmd = String.format("/cglow block %d %d %d %d %s", x, y, z, cGlowTime, cGlowColor);

        try {
            boolean executed = false;

            // Try common client player methods via reflection
            var player = source.getPlayer();
            try {
                var m = player.getClass().getMethod("sendChatMessage", String.class);
                m.invoke(player, cmd);
                executed = true;
            } catch (NoSuchMethodException ignored) {
            }

            if (!executed) {
                try {
                    var m = player.getClass().getMethod("sendMessage", Text.class, boolean.class);
                    m.invoke(player, Text.literal(cmd), true);
                    executed = true;
                } catch (NoSuchMethodException ignored) {
                }
            }

            if (!executed) {
                try {
                    var m = player.getClass().getMethod("sendMessage", String.class);
                    m.invoke(player, cmd);
                    executed = true;
                } catch (NoSuchMethodException ignored) {
                }
            }

            if (!executed) {
                try {
                    var m = player.getClass().getMethod("sendMessage", net.minecraft.text.Text.class, boolean.class);
                    m.invoke(player, Text.literal(cmd), true);
                    executed = true;
                } catch (NoSuchMethodException ignored) {
                }
            }

            if (!executed) {
                try {
                    var m = player.getClass().getMethod("sendMessage", String.class);
                    m.invoke(player, cmd);
                    executed = true;
                } catch (NoSuchMethodException ignored) {
                }
            }

            // Fallback to network handler methods via reflection
            if (!executed) {
                try {
                    var nh = MinecraftClient.getInstance().getNetworkHandler();
                    if (nh != null) {
                        try {
                            var m2 = nh.getClass().getMethod("sendChatMessage", String.class);
                            m2.invoke(nh, cmd);
                            executed = true;
                        } catch (NoSuchMethodException ignored) {
                        }
                        if (!executed) {
                            try {
                                var m3 = nh.getClass().getMethod("sendCommand", String.class);
                                m3.invoke(nh, cmd);
                                executed = true;
                            } catch (NoSuchMethodException ignored) {
                            }
                        }
                    }
                } catch (Throwable t) {
                    // ignore
                }
            }

            if (executed) {
                source.sendFeedback(Text.literal("Executed: " + cmd));
            } else {
                source.sendFeedback(Text.literal("Failed to execute command: " + cmd));
            }
        } catch (Exception e) {
            source.sendFeedback(Text.literal("Failed to execute command: " + cmd));
        }

        return 1;
    }
}