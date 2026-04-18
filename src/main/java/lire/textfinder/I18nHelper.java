package lire.textfinder;

import net.minecraft.client.resource.language.I18n;

/**
 * 国际化帮助类
 */
public class I18nHelper {

    /**
     * 获取翻译文本
     */
    public static String translate(String key, Object... args) {
        return I18n.translate(key, args);
    }
}