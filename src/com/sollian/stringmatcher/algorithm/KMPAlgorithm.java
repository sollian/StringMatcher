package com.sollian.stringmatcher.algorithm;

import com.sollian.stringmatcher.result.Result;
import com.sollian.stringmatcher.result.ResultSet;

/**
 * KMP算法进行字符串匹配
 *
 */
public class KMPAlgorithm implements IAlgorithm {
    @Override
    public ResultSet find(String from, String target) {
        ResultSet set = new ResultSet();

        int[] inform = preprocessing(target);

        int i = 0;
        int j = 0;

        while (i < from.length()) {
            if (j == -1 || from.charAt(i) == target.charAt(j)) {
                i++;
                j++;
            } else {
                j = inform[j];
            }
            if (j == target.length()) {
                set.add(new Result(i - target.length(), i));
                j = inform[j];
            }
        }
        return set;
    }

    private static int[] preprocessing(CharSequence target) {
        int[] inform = new int[target.length() + 1];

        inform[0] = -1;

        int j = 1;
        int k = 0;
        while (j < target.length()) {
            if (k == -1 || target.charAt(j) == target.charAt(k)) {
                j++;
                k++;
                inform[j] = k;
            } else {
                k = inform[k];
            }
        }
        return inform;
    }
}