package com.sollian.stringmatcher.algorithm;

import com.sollian.stringmatcher.result.ResultSet;

public interface IAlgorithm {
    ResultSet find(String from, String target);
}
