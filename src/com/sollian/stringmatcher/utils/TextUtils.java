package com.sollian.stringmatcher.utils;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {
    public static boolean isEmpty(String text) {
        return text == null || text.equals("");
    }

    public static String[] split(String str, char ch) {
        return split(str, ch, true);
    }

    /**
     * @author anning
     */
    public static String[] split(String str, char ch, boolean includeEmptySplit) {
        if (TextUtils.isEmpty(str)) {
            return new String[]{str};
        }

        List<String> result = null;

        int start = 0;
        int len = str.length();
        while (start < len) {
            int index = str.indexOf(ch, start);
            // 未找到
            if (index < 0) {
                break;
            }
            // 已找到
            if (index > start || includeEmptySplit) {
                if (result == null) {
                    result = new ArrayList<>(5);
                }
                result.add(str.substring(start, index));
            }
            // 下一组
            start = index + 1;
        }

        if (start != 0 && start < len && result != null) {
            result.add(str.substring(start));
        }

        return result == null ? new String[]{str} : result.toArray(new String[result.size()]);
    }
}
