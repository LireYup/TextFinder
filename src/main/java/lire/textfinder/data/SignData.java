package lire.textfinder.data;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * 存储单个告示牌的所有相关数据
 *
 * @param frontTexts 新增：存储BlockState
 */
public record SignData(BlockPos pos, int blockId, List<Text> frontTexts, String frontColor, boolean frontGlowing,
                       List<Text> backTexts, String backColor, boolean backGlowing) {
    public SignData(BlockPos pos, BlockState blockId, List<Text> frontTexts, String frontColor, boolean frontGlowing,
                    List<Text> backTexts, String backColor, boolean backGlowing) {
        this(pos, Block.getRawIdFromState(blockId), frontTexts, frontColor, frontGlowing, backTexts, backColor, backGlowing);
    }

    // 其他现有方法保持不变...
    public boolean matches(String searchContext) {
        // 检查正面文本
        for (Text text : frontTexts) {
            if (text.getString().contains(searchContext)) {
                return true;
            }
        }
        // 检查背面文本
        for (Text text : backTexts) {
            if (text.getString().contains(searchContext)) {
                return true;
            }
        }
        return false;
    }

}
