package lire.textfinder.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 存储单个告示牌的所有相关数据
 *
 * @param frontTexts 新增：存储BlockState
 */
public record SignData(BlockPos pos, int blockId, List<Component> frontTexts, String frontColor, boolean frontGlowing,
                       List<Component> backTexts, String backColor, boolean backGlowing) {
    public SignData(BlockPos pos, BlockState blockId, List<Component> frontTexts, String frontColor, boolean frontGlowing,
                    List<Component> backTexts, String backColor, boolean backGlowing) {
        this(pos, Block.getId(blockId), frontTexts, frontColor, frontGlowing, backTexts, backColor, backGlowing);
    }

    // 其他现有方法保持不变...
    public boolean matches(String searchContext) {
        // 检查正面文本
        for (Component text : frontTexts) {
            if (text.getString().contains(searchContext)) {
                return true;
            }
        }
        // 检查背面文本
        for (Component text : backTexts) {
            if (text.getString().contains(searchContext)) {
                return true;
            }
        }
        return false;
    }

}