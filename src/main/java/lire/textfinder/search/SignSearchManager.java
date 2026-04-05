package lire.textfinder.search;

import lire.textfinder.I18nHelper;
import lire.textfinder.TextFinder;
import lire.textfinder.data.SignData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 管理告示牌搜索过程和结果的类
 */
public class SignSearchManager {
    // 存储搜索到的符合条件的告示牌
    private final List<SignData> foundSigns = new CopyOnWriteArrayList<>();
    // 存储所有检测过的告示牌总数（用于调试输出）
    private int totalSignsChecked = 0;
    // 所有检测过的告示牌总数（用于进度显示）
    // 当前搜索上下文
    private String currentSearchContext = "";
    // 搜索是否正在进行中
    private boolean isSearching = false;
    // 记录下一个要搜索的区块迭代器
    private Iterator<WorldChunk> chunkIterator;
    // 记录当前区块中要搜索的方块实体索引
    private int nextBlockEntityIndex = 0;

    private static SignSearchManager instance;

    private SignSearchManager() {}

    public static synchronized SignSearchManager getInstance() {
        if (instance == null) {
            instance = new SignSearchManager();
        }
        return instance;
    }

    /**
     * 开始新的搜索
     */
    public void startSearch(String searchContext) {
        resetSearch();
        this.currentSearchContext = searchContext;
        this.isSearching = true;

        // 初始化区块迭代器（1.21.7兼容方式）
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.player != null) {
            List<WorldChunk> loadedChunks = collectLoadedChunks(client);
            this.chunkIterator = loadedChunks.iterator();
        }

        TextFinder.LOGGER.info("Starting search for: {}", searchContext);
    }

    /**
     * 收集所有已加载的区块（1.21.7兼容实现）
     */
    private List<WorldChunk> collectLoadedChunks(MinecraftClient client) {
        List<WorldChunk> chunks = new ArrayList<>();
        if (client.world == null || client.player == null) return chunks;

        // 获取玩家所在区块
        BlockPos playerPos = client.player.getBlockPos();
        ChunkPos playerChunkPos = new ChunkPos(playerPos);

        // 修复：调用标准驼峰方法名
        int renderDistance = TextFinder.config.getSearchRange();

        // 搜索玩家周围一定范围内的区块
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                ChunkPos chunkPos = new ChunkPos(
                        playerChunkPos.x + x,
                        playerChunkPos.z + z
                );
                // 获取区块（1.21.7兼容方式）
                WorldChunk chunk = client.world.getChunkManager()
                        .getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
                if (chunk != null) {
                    chunks.add(chunk);
                }
            }
        }
        return chunks;
    }

    /**
     * 重置搜索状态
     */
    public void resetSearch() {
        foundSigns.clear();
        totalSignsChecked = 0;
        currentSearchContext = "";
        isSearching = false;
        chunkIterator = null;
        nextBlockEntityIndex = 0;
    }

    /**
     * 继续搜索过程（每个游戏刻调用一次）
     */
    public void tickSearch() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!isSearching || client.world == null || chunkIterator == null) {
            return;
        }

        // 修复：调用标准驼峰方法名
        int maxpertick = TextFinder.config.getMaxSearchAmountPerTick();
        int processedthistick = 0;

        // 遍历区块
        while (chunkIterator.hasNext() && processedthistick < maxpertick) {
            WorldChunk chunk = chunkIterator.next();

            // 获取区块中的所有方块实体
            List<BlockEntity> blockEntities = new ArrayList<>(chunk.getBlockEntities().values());
            int entitycount = blockEntities.size();

            // 遍历方块实体
            while (nextBlockEntityIndex < entitycount && processedthistick < maxpertick) {
                BlockEntity blockEntity = blockEntities.get(nextBlockEntityIndex);
                nextBlockEntityIndex++;

                // 检查是否是告示牌
                if (blockEntity instanceof SignBlockEntity signBlockEntity) {
                    totalSignsChecked++;
                    processedthistick++;

                    // 提取告示牌数据
                    SignData signData = createSignData(signBlockEntity);

                    // 检查是否匹配搜索内容
                    if (signData.matches(currentSearchContext)) {
                        foundSigns.add(signData);
                    }
                }
            }

            // 当前区块处理完毕，重置方块实体索引
            if (nextBlockEntityIndex >= entitycount) {
                nextBlockEntityIndex = 0;
            } else {
                // 本tick已达到最大处理数量，退出循环
                break;
            }
        }

        // 所有区块处理完毕，结束搜索
        if (!chunkIterator.hasNext() && nextBlockEntityIndex == 0) {
            isSearching = false;
            int foundCount = foundSigns.size();
            TextFinder.LOGGER.info("Search completed. Found {} matching signs.", foundCount);

            // 发送结果消息给玩家
            client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal(I18nHelper.translate("textfinder.search.completed", foundCount)), false);
            }
        }
    }

    /**
     * 从告示牌方块实体创建SignData对象
     */
    private SignData createSignData(SignBlockEntity sign) {
        BlockPos pos = sign.getPos();
        BlockState state = sign.getCachedState();

        // 获取正反面文本
        List<Text> frontTexts = List.of(
                sign.getFrontText().getMessage(0, false),
                sign.getFrontText().getMessage(1, false),
                sign.getFrontText().getMessage(2, false),
                sign.getFrontText().getMessage(3, false)
        );
        List<Text> backTexts = List.of(
                sign.getBackText().getMessage(0, false),
                sign.getBackText().getMessage(1, false),
                sign.getBackText().getMessage(2, false),
                sign.getBackText().getMessage(3, false)
        );

        // 获取颜色和发光状态
        String frontColor = sign.getFrontText().getColor() != null ?
                sign.getFrontText().getColor().toString() : "default";
        String backColor = sign.getBackText().getColor() != null ?
                sign.getBackText().getColor().toString() : "default";

        return new SignData(
                pos,
                state,
                frontTexts,
                frontColor,
                sign.getFrontText().isGlowing(),
                backTexts,
                backColor,
                sign.getBackText().isGlowing()
        );
    }

    /**
     * 重新筛选已找到的告示牌
     */
    public void refilterSigns(String newContext) {
        List<SignData> filtered = new ArrayList<>();
        for (SignData sign : foundSigns) {
            if (sign.matches(newContext)) {
                filtered.add(sign);
            }
        }
        foundSigns.clear();
        foundSigns.addAll(filtered);
        currentSearchContext = newContext;
    }

    /**
     * 输出搜索结果到命令源（根据数字类型的输出复杂度）
     * 复杂度等级：1=极简(仅坐标)，2=简单(坐标+首行文本)，3=详细(坐标+多行文本)，4=调试(全信息+进度)
     */
    public void outputSearchResults(FabricClientCommandSource source, int page) {
        if (isSearching()) {
            source.sendFeedback(Text.literal(I18nHelper.translate("textfinder.command.display.searching", getTotalSignsChecked())));
            return;
        }

        List<SignData> results = getFoundSigns();
        if (results.isEmpty()) {
            source.sendFeedback(Text.literal(I18nHelper.translate("textfinder.command.display.no_results")));
            return;
        }

        // 修复：调用标准驼峰方法名
        int complexity;
        try {
            complexity = TextFinder.config.getOutputComplexity();
        } catch (Exception e) {
            complexity = 2; // 默认简单模式
        }

        // 复杂度4：显示进度信息（仅显示每tick处理数量，进度将合并到结果标题中）
        if (complexity >= 4) {
            source.sendFeedback(Text.literal(I18nHelper.translate("textfinder.command.display.max_per_tick", TextFinder.config.getMaxSearchAmountPerTick())));
        }

        int pageSize = 10;
        int totalResults = results.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalResults);

        if (startIndex >= totalResults) {
            source.sendFeedback(Text.literal(I18nHelper.translate("textfinder.command.display.page_out_of_range")));
            return;
        }

        // 显示已找到/已检查 的计数并附带页码
        source.sendFeedback(Text.literal(I18nHelper.translate("textfinder.command.display.found_with_page", totalResults, totalSignsChecked, page)));

        for (int index = startIndex; index < endIndex; index++) {
            SignData sign = results.get(index);
            source.sendFeedback(formatSignText(sign, index + 1, complexity));
        }

        if (endIndex < totalResults) {
            source.sendFeedback(Text.literal("§e" + I18nHelper.translate("textfinder.command.display.more_results", totalResults - endIndex, page + 1)));
        }
    }

    /**
     * 根据数字复杂度格式化单个告示牌信息
     */
    private Text formatSignText(SignData sign, int index, int complexity) {
        BlockPos pos = sign.pos();
        StringBuilder sb = new StringBuilder();
        sb.append("§b").append(index).append(". §r").append(I18nHelper.translate("textfinder.display.coordinate")).append(pos.toShortString());

        // 复杂度2及以上：显示首行文本
        if (complexity >= 2) {
            String firstLine = sign.frontTexts().getFirst().getString();
            if (firstLine.isEmpty() && !sign.backTexts().isEmpty()) {
                firstLine = sign.backTexts().getFirst().getString();
            }
            sb.append(" §7").append(truncateText(firstLine, 20));
        }

        // 复杂度3及以上：显示更多文本行
        if (complexity >= 3) {
            sb.append("\n  §d").append(I18nHelper.translate("textfinder.display.front_text"));
            for (int i = 0; i < sign.frontTexts().size(); i++) {
                String line = sign.frontTexts().get(i).getString();
                if (!line.isEmpty()) {
                    sb.append(line).append(" | ");
                }
            }
            sb.append("\n  §d").append(I18nHelper.translate("textfinder.display.back_text"));
            for (int i = 0; i < sign.backTexts().size(); i++) {
                String line = sign.backTexts().get(i).getString();
                if (!line.isEmpty()) {
                    sb.append(line).append(" | ");
                }
            }
        }

        // 复杂度4（调试模式）：显示完整信息
        if (complexity >= 4) {
            sb.append("\n  §f").append(I18nHelper.translate("textfinder.display.glow_status", sign.frontGlowing(), sign.backGlowing()));
            sb.append("\n  §6").append(I18nHelper.translate("textfinder.display.block_id")).append(sign.blockId())
                    .append(" §6").append(I18nHelper.translate("textfinder.display.color", sign.frontColor(), sign.backColor()));
        }

        return Text.literal(sb.toString());
    }

    /**
     * 截断文本到指定长度
     */
    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * 清除已找到的告示牌列表
     */
    public void clearFoundSigns() {
        foundSigns.clear();
        totalSignsChecked = 0;
        nextBlockEntityIndex = 0;
        chunkIterator = null;
        isSearching = false;
    }

    // ----------------- 通用Getter方法 -----------------
    public boolean isSearching() {
        return isSearching;
    }

    public List<SignData> getFoundSigns() {
        return new ArrayList<>(foundSigns); // 返回副本避免并发修改
    }

    public int getTotalSignsChecked() {
        return totalSignsChecked;
    }

}
