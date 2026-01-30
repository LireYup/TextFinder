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
    private boolean firstLaunch = false; // 首次启动标识
    private int searchRange = 12; // 搜索范围
    private boolean cGlow = false; // 新增配置项：发光开关（驼峰命名）

    public static ModConfig load() {
        ModConfig config = new ModConfig();

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, ModConfig.class);

                // 首次启动或配置异常时重置为默认值
                if (config.isFirstLaunch()) {
                    config = new ModConfig();
                    config.setFirstLaunch(false);
                    config.save();
                }
            } catch (IOException e) {
                TextFinder.LOGGER.error("加载配置文件失败", e);
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
            TextFinder.LOGGER.error("保存配置文件失败", e);
        }
    }

    // 最大每tick搜索数量（驼峰+范围1~10000）
    public int getMaxSearchAmountPerTick() {
        return Math.max(1, Math.min(10000, maxSearchAmountPerTick));
    }

    public void setMaxSearchAmountPerTick(int value) {
        this.maxSearchAmountPerTick = Math.max(1, Math.min(10000, value));
    }

    // 输出复杂度
    public int getOutputComplexity() {
        return Math.max(1, Math.min(4, outputComplexity));
    }

    public void setOutputComplexity(int outputComplexity) {
        this.outputComplexity = Math.max(1, Math.min(4, outputComplexity));
    }

    // 调试信息间隔
    public int getDebugPgt() {
        return Math.max(1, Math.min(255, debugPgt));
    }

    public void setDebugPgt(int debugPgt) {
        this.debugPgt = Math.max(1, Math.min(255, debugPgt));
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

    // 首次启动标识
    public boolean isFirstLaunch() {
        return firstLaunch;
    }

    public void setFirstLaunch(boolean firstLaunch) {
        this.firstLaunch = firstLaunch;
    }

    // 搜索范围
    public int getSearchRange() {
        return Math.max(1, Math.min(32, searchRange));
    }

    public void setSearchRange(int searchRange) {
        this.searchRange = Math.max(1, Math.min(32, searchRange));
    }

    // 发光开关（新增配置项）
    public boolean isCGlow() {
        return cGlow;
    }

    public void setCGlow(boolean cGlow) {
        this.cGlow = cGlow;
    }
}