package lire.textfinder.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lire.textfinder.TextFinder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "textfinder.json");

    // 配置项定义（严格小驼峰）
    private int maxSearchAmountPerTick = 256; // 默认值改为256，驼峰命名
    private int outputComplexity = 2; // 1=simple, 2=normal, 3=complex, 4=debug
    private int debugPgt = 4;
    private int cGlowTime = 60; // 发光秒数
    private String cGlowColor = "white"; // 发光颜色
    private int searchRange = 12; // 搜索范围

    public static ModConfig load() {
        ModConfig config = new ModConfig();

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, ModConfig.class);

                // 配置异常时重置为默认值（不再使用 firstLaunch 标记）
                // 如果 file 中的内容无法正确解析，会在 catch 块重写默认配置
            } catch (IOException e) {
                TextFinder.LOGGER.error("[TextFinder]Failed to load config file", e);
                config = new ModConfig();
                config.save();
            }
        } else {
            config.save();
        }

        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            TextFinder.LOGGER.error("[TextFinder]Failed to save config file", e);
        }
    }

    // 最大每tick搜索数量（驼峰+范围1~10000）
    public int getMaxSearchAmountPerTick() {
        return Math.clamp(maxSearchAmountPerTick, 1, 10000);
    }

    public void setMaxSearchAmountPerTick(int value) {
        this.maxSearchAmountPerTick = Math.clamp(value, 1, 10000);
    }

    // 输出复杂度
    public int getOutputComplexity() {
        return Math.clamp(outputComplexity, 1, 4);
    }

    public void setOutputComplexity(int outputComplexity) {
        this.outputComplexity = Math.clamp(outputComplexity, 1, 4);
    }

    // 调试信息间隔
    public int getDebugPgt() {
        return Math.clamp(debugPgt, 1, 255);
    }

    public void setDebugPgt(int debugPgt) {
        this.debugPgt = Math.clamp(debugPgt, 1, 255);
    }

    // 发光时间
    public int getCGlowTime() {
        return cGlowTime;
    }

    public void setCGlowTime(int cGlowTime) {
        this.cGlowTime = cGlowTime;
    }

    // 发光颜色
    public String getCGlowColor() {
        return cGlowColor;
    }

    public void setCGlowColor(String cGlowColor) {
        this.cGlowColor = cGlowColor;
    }

    // 搜索范围
    public int getSearchRange() {
        return Math.clamp(searchRange, 1, 32);
    }

    public void setSearchRange(int searchRange) {
        this.searchRange = Math.clamp(searchRange, 1, 32);
    }
}
