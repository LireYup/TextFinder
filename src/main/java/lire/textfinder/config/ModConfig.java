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

    // 配置项定义
    private int maxSearchAmountPerTick = 100; // 修正变量名驼峰
    private boolean withBaritone = false;
    private boolean withClientCommands = false;
    private int outputComplexity = 2; // 1=simple, 2=normal, 3=complex, 4=debug
    private int debugPgt = 4;
    private int cglowTime = 60;
    private String cglowColor = "white";
    private boolean firstLaunch = false; // 首次启动标识
    private int searchRange = 12; // 搜索范围（修正变量名驼峰）

    public static ModConfig load() {
        ModConfig config = new ModConfig();

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, ModConfig.class);

                // 首次启动或配置异常时重置为默认值
                if (config.firstLaunch) {
                    config = new ModConfig();
                    config.firstLaunch = false;
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

    // 最大每tick搜索数量（修正方法名为标准驼峰）
    public int getMaxSearchAmountPerTick() {
        return Math.max(1, Math.min(255, maxSearchAmountPerTick));
    }

    public void setMaxSearchAmountPerTick(int value) {
        this.maxSearchAmountPerTick = Math.max(1, Math.min(255, value));
    }

    // Baritone集成开关
    public boolean isWithBaritone() {
        return withBaritone;
    }

    public void setWithBaritone(boolean withBaritone) {
        this.withBaritone = withBaritone;
    }

    // 客户端指令集成开关
    public boolean isWithClientCommands() {
        return withClientCommands;
    }

    public void setWithClientCommands(boolean withClientCommands) {
        this.withClientCommands = withClientCommands;
    }

    // 输出复杂度（修正方法名为标准驼峰）
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
    public int getCglowTime() {
        return cglowTime;
    }

    public void setCglowTime(int cglowTime) {
        this.cglowTime = cglowTime;
    }

    // 发光颜色
    public String getCglowColor() {
        return cglowColor;
    }

    public void setCglowColor(String cglowColor) {
        this.cglowColor = cglowColor;
    }

    // 首次启动标识
    public boolean isFirstLaunch() {
        return firstLaunch;
    }

    public void setFirstLaunch(boolean firstLaunch) {
        this.firstLaunch = firstLaunch;
    }

    // 搜索范围（修正方法名为标准驼峰）
    public int getSearchRange() {
        return Math.max(1, Math.min(32, searchRange));
    }

    public void setSearchRange(int searchRange) {
        this.searchRange = Math.max(1, Math.min(32, searchRange));
    }

    // 兼容旧配置的降级方法（可选，保证旧配置文件仍能读取）
    @Deprecated
    public int getmaxsearchamountpertick() {
        return getMaxSearchAmountPerTick();
    }

    @Deprecated
    public int getoutputcomplexity() {
        return getOutputComplexity();
    }

    @Deprecated
    public int getsearchrange() {
        return getSearchRange();
    }
}