package com.sollian.stringmatcher.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ResultSet {
    private final ArrayList<Result> results = new ArrayList<>();

    private boolean isMixed;

    public boolean found() {
        return !results.isEmpty();
    }

    /**
     * 得到的是原数据的拷贝
     */
    public List<Result> copyResults() {
        return new ArrayList<>(results);
    }

    public void add(Result result) {
        results.add(result);
        isMixed = false;
    }

    public void add(Collection<Result> result) {
        results.addAll(result);
        isMixed = false;
    }

    public void add(ResultSet resultset) {
        results.addAll(resultset.results);
        isMixed = false;
    }

    public void remove(ResultSet resultset) {
        if (resultset == null) {
            return;
        }
        remove(resultset.results);
    }

    public void remove(Collection<Result> result) {
        if (result == null || result.isEmpty()) {
            return;
        }
        for (Result r : result) {
            remove(r);
        }
    }

    public void remove(Result result) {
        mix();
        ResultSet resultSet = new ResultSet();
        for (Result r : results) {
            resultSet.add(r.remove(result));
        }
        results.clear();
        results.addAll(resultSet.results);
        isMixed = false;
    }

    public void clear() {
        results.clear();
        isMixed = false;
    }

    /**
     * 移除不合法的Result
     */
    public void removeInvalidResults() {
        for (int i = results.size() - 1; i >= 0; i--) {
            if (!results.get(i).isValid()) {
                results.remove(i);
                isMixed = false;
            }
        }
    }

    /**
     * 将包含相同区间的Result合并
     *
     * @return true表明数据发生了改动；false数据未改动
     */
    public boolean mix() {
        if (results.isEmpty()) {
            return false;
        }
        if (isMixed) {
            return false;
        }

        removeInvalidResults();

        Collections.sort(results);
        Collection<Result> list = new LinkedList<>();
        Result result = null;
        boolean changed = false;
        for (Result r : results) {
            if (result == null) {
                result = r;
                list.add(result);
                continue;
            }
            if (result.start <= r.start && result.end >= r.start) {
                if (result.end < r.end) {
                    result.end = r.end;
                    changed = true;
                }
            } else {
                result = r;
                list.add(result);
            }
        }
        results.clear();
        results.addAll(list);
        isMixed = true;
        return changed;
    }
}
