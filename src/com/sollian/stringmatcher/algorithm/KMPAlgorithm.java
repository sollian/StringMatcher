package com.sollian.stringmatcher.algorithm;

import com.sollian.stringmatcher.result.Result;
import com.sollian.stringmatcher.result.ResultSet;

/**
 * KMP算法进行字符串匹配
 */
public class KMPAlgorithm implements IAlgorithm {
    @Override
    public ResultSet find(String from, String target) {
        ResultSet set = new ResultSet();

        int[] next = getNext(target);

        int i = 0;
        int j = 0;

        while (i < from.length()) {
            if (j == -1 || from.charAt(i) == target.charAt(j)) {
                i++;
                j++;
            } else {
                j = next[j];
            }
            if (j == target.length()) {
                set.add(new Result(i - target.length(), i));
                j = next[j];
            }
        }
        return set;
    }

    /**
     * 获取next数组
     */
    private static int[] getNext(CharSequence target) {
        int[] next = new int[target.length() + 1];

        int j = 0, k = -1;
        next[0] = -1;
        while (j < target.length() - 1) {
            if (k == -1 || target.charAt(j) == target.charAt(k)) {
                j++;
                k++;
                if (target.charAt(j) == target.charAt(k))// 当两个字符相同时，就跳过
                    next[j] = next[k];
                else
                    next[j] = k;
            } else
                k = next[k];
        }
        return next;
    }
}