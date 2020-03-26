package com.sollian.stringmatcher.algorithm;

import com.sollian.stringmatcher.result.ResultSet;

public interface IAlgorithm {
    /**
     * @param from   源字符串
     * @param target 目标字符串
     * @return
     */
    ResultSet find(String from, String target);
}
