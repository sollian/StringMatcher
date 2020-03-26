package com.sollian.stringmatcher.result;

import java.util.Locale;

public class Result implements Comparable<Result> {
    /**
     * [start, end) include
     */
    public int start;
    /**
     * [start, end) exclude
     */
    public int end;

    public Result(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * 相交（包括包含与被包含关系）
     */
    public boolean intersect(Result result) {
        return !(result == null || !isValid() || !result.isValid()) && (start <= result.start && end > result.start
                || start < result.end && end >= result.end || start > result.start && end < result.end);
    }

    /**
     * 包含
     */
    public boolean contains(Result result) {
        return !(result == null || !isValid() || !result.isValid()) && start <= result.start && end >= result.end;
    }

    /**
     * 被包含
     */
    public boolean containsBy(Result result) {
        return !(result == null || !isValid() || !result.isValid()) && start >= result.start && end <= result.end;
    }

    public ResultSet remove(Result result) {
        ResultSet resultSet = new ResultSet();
        if (contains(result)) {
            resultSet.add(new Result(start, result.start));
            resultSet.add(new Result(result.end, end));
            resultSet.removeInvalidResults();
        } else if (containsBy(result)) {
        } else if (intersect(result)) {
            if (start < result.start) {
                resultSet.add(new Result(start, result.start));
            } else {
                resultSet.add(new Result(result.end, end));
            }
            resultSet.removeInvalidResults();
        } else {
            resultSet.add(this);
        }
        return resultSet;
    }

    public boolean isValid() {
        return start >= 0 && end > start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Result)) {
            return false;
        }

        Result result = (Result) o;

        if (start != result.start) {
            return false;
        }
        return end == result.end;

    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    @Override
    public String toString() {
        return String.format(Locale.CHINA, "%d ~ %d", start, end);
    }

    @Override
    public int compareTo(Result another) {
        if (start > another.start) {
            return 1;
        } else if (start < another.start) {
            return -1;
        } else {
            return 0;
        }
    }
}
