package lire.textfinder.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lire.textfinder.TextFinder;
import net.fabricmc.loader.api.FabricLoader;
import lire.textfinder.search.SignSearchManager;
import lire.textfinder.I18nHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
// 修改导入为greedyString
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class TextFinderCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        // 主指令节点
        dispatcher.register(ClientCommands.literal("textfinder")
                .then(ClientCommands.literal("search")
                        .then(ClientCommands.argument("keyword", greedyString())  // 修改为greedyString
                                .executes(TextFinderCommand::searchCommand)))
                .then(ClientCommands.literal("display")
                        .executes(TextFinderCommand::displayCommand))
                .then(ClientCommands.literal("refilter")
                        .then(ClientCommands.argument("newKeyword", greedyString())  // 修改为greedyString
                                .executes(TextFinderCommand::refilterCommand)))
                .then(ClientCommands.literal("clear")
                        .executes(TextFinderCommand::clearCommand)));

        // 简写指令
        dispatcher.register(ClientCommands.literal("tf")
                .then(ClientCommands.argument("keyword", greedyString())  // 修改为greedyString
                        .executes(TextFinderCommand::searchCommand)));

        dispatcher.register(ClientCommands.literal("td")
                .executes(TextFinderCommand::displayCommand)
                .then(ClientCommands.argument("page", IntegerArgumentType.integer(1))
                        .executes(TextFinderCommand::displayCommand)));

        dispatcher.register(ClientCommands.literal("trf")
                .then(ClientCommands.argument("newKeyword", greedyString())  // 修改为greedyString
                        .executes(TextFinderCommand::refilterCommand)));

        // 测试指令：/ttest - 输出当前配置的全部项
        dispatcher.register(ClientCommands.literal("ttest")
                .executes(TextFinderCommand::ttestCommand));

        // /tglow <n> - 如果安装了 clientcommands，则执行 /cglow block <x> <y> <z> <cGlowTime> <cGlowColor>
        dispatcher.register(ClientCommands.literal("tglow")
                .then(ClientCommands.argument("n", IntegerArgumentType.integer())
                        .executes(TextFinderCommand::tglowCommand)));
    }

    private static int searchCommand(CommandContext<FabricClientCommandSource> context) {
        String keyword = StringArgumentType.getString(context, "keyword");
        FabricClientCommandSource source = context.getSource();

        source.getPlayer();

        SignSearchManager.getInstance().startSearch(keyword);
        source.sendFeedback(Component.literal(I18nHelper.translate("textfinder.command.search.start", keyword)));
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

        source.sendFeedback(Component.literal(I18nHelper.translate("textfinder.command.refilter.success", newKeyword)));
        return displayCommand(context);
    }

    private static int clearCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        SignSearchManager.getInstance().clearFoundSigns();
        source.sendFeedback(Component.literal(I18nHelper.translate("textfinder.command.clear.success")));
        return 1;
    }

    private static int ttestCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        if (TextFinder.config == null) {
            source.sendFeedback(Component.literal("&b[TextFinder]&cConfig is not loaded"));
            return 1;
        }

        // 输出配置的各项值
        source.sendFeedback(Component.literal("maxSearchAmountPerTick: " + TextFinder.config.getMaxSearchAmountPerTick()));
        source.sendFeedback(Component.literal("outputComplexity: " + TextFinder.config.getOutputComplexity()));
        source.sendFeedback(Component.literal("debugPgt: " + TextFinder.config.getDebugPgt()));
        source.sendFeedback(Component.literal("cGlowTime: " + TextFinder.config.getCGlowTime()));
        source.sendFeedback(Component.literal("cGlowColor: " + TextFinder.config.getCGlowColor()));
        source.sendFeedback(Component.literal("searchRange: " + TextFinder.config.getSearchRange()));

        // 指示是否安装 clientcommands（直接输出英文标识）
        boolean hasClientCommands = FabricLoader.getInstance().isModLoaded("clientcommands");
        source.sendFeedback(Component.literal("withClientCommands=" + hasClientCommands));

        return 1;
    }

    private static int tglowCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();

        boolean hasClientCommands = FabricLoader.getInstance().isModLoaded("clientcommands");
        if (!hasClientCommands) {
            source.sendFeedback(Component.literal("§c" + I18nHelper.translate("textfinder.command.tglow.no_clientcommands")));
            return 1;
        }

        int n;
        try {
            n = IntegerArgumentType.getInteger(context, "n");
        } catch (IllegalArgumentException ignored) {
            n = 1;
        }

        // Use found signs list
        var manager = SignSearchManager.getInstance();
        var results = manager.getFoundSigns();
        if (results.isEmpty()) {
            source.sendFeedback(Component.literal("§c" + I18nHelper.translate("textfinder.command.tglow.no_results")));
            return 1;
        }

        if (n < 1 || n > results.size()) {
            source.sendFeedback(Component.literal("§c" + I18nHelper.translate("textfinder.command.tglow.index_out_of_range", n, results.size())));
            return 1;
        }

        var sign = results.get(n - 1);
        int x = sign.pos().getX();
        int y = sign.pos().getY();
        int z = sign.pos().getZ();

        int cGlowTime = TextFinder.config.getCGlowTime();
        String cGlowColor = TextFinder.config.getCGlowColor();

        // sendCommand() expects raw command WITHOUT leading "/"
        String cmd = String.format("cglow block %d %d %d %d color %s", x, y, z, cGlowTime, cGlowColor);

        try {
            var networkHandler = Minecraft.getInstance().getConnection();
            if (networkHandler != null) {
                networkHandler.sendCommand(cmd);
                source.sendFeedback(Component.literal("§a" + I18nHelper.translate("textfinder.command.tglow.success", n, sign.pos().toShortString())));
            } else {
                source.sendFeedback(Component.literal("§c" + I18nHelper.translate("textfinder.command.tglow.not_connected")));
            }
        } catch (Exception e) {
            TextFinder.LOGGER.error("Failed to send glow command", e);
            source.sendFeedback(Component.literal("§c" + I18nHelper.translate("textfinder.command.tglow.failed")));
        }

        return 1;
    }
}