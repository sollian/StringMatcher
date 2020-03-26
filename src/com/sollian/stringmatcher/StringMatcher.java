package com.sollian.stringmatcher;

import com.sollian.stringmatcher.algorithm.IAlgorithm;
import com.sollian.stringmatcher.algorithm.KMPAlgorithm;
import com.sollian.stringmatcher.result.Result;
import com.sollian.stringmatcher.result.ResultSet;
import com.sollian.stringmatcher.utils.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * @author sollian on 2016/11/24.
 */

public class StringMatcher {
    /**
     * 包含关键字索所包含的所有词（单词默认以" "分隔），则匹配成功
     */
    public static final int FLAG_MATCH_WORD = 0;
    /**
     * 包含整个关键字，则匹配成功
     */
    public static final int FLAG_MATCH_SENTENCE = 1;
    /**
     * 包含关键字的每个字符（不包含空格），则匹配成功
     */
    public static final int FLAG_MATCH_CHAR = 2;

    /**
     * 默认单词分隔符
     */
    private static final char DEFAULT_SPLIT_CHAR = ' ';

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 拼音的匹配
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static ResultSet matchMultiPinyins(String[] pinyins, String target) {
        return matchMultiPinyins(pinyins, target, false, true);
    }

    /**
     * 匹配带有多音字的一组拼音，
     *
     * @param pinyins            如：单独-{"dan du", "shan du", "chan du"} 不区分大小写
     * @param target             如：dandu，不区分大小写
     * @param fullMatch          true：target子串匹配整个pinyin或者pinyin首字母时匹配成功；
     *                           false：匹配pinyin第0个字符开始的任意大于0的字符时匹配成功
     * @param enableSampledMatch 是否开启非连续的匹配。如lx匹配lishouxian，true匹配成功，false匹配失败
     */
    public static ResultSet matchMultiPinyins(String[] pinyins, String target, boolean fullMatch,
                                              boolean enableSampledMatch) {
        if (pinyins == null || pinyins.length < 1 || TextUtils.isEmpty(target)) {
            return new ResultSet();
        }
        ResultSet resultSet = new ResultSet();
        for (String pinyin : pinyins) {
            ResultSet set = matchPinyins(pinyin.toLowerCase(), target.toLowerCase(), fullMatch, enableSampledMatch);
            if (set.found()) {
                resultSet.add(set);
            }
        }
        return resultSet;
    }

    /**
     * 匹配单音字的一组拼音，每个拼音用' '分隔，如：李守宪-"li shou xian"
     *
     * @param enableSampledMatch 是否开启非连续的匹配。如lx匹配lishouxian，true匹配成功，false匹配失败
     */
    private static ResultSet matchPinyins(String pinyin, String target, boolean fullMatch, boolean enableSampledMatch) {
        if (pinyin == null || pinyin.length() < 1) {
            return new ResultSet();
        }
        String[] arr = TextUtils.split(pinyin, ' ');
        target = target.replaceAll("[^a-zA-Z0-9]", "");
        int targetLength = target.length();
        ResultSet resultSet = new ResultSet();
        Result result = null;
        boolean begin = false;
        boolean end = false;
        int start = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!enableSampledMatch && end) {
                break;
            }
            int newStart = matchPinyin(arr[i], target, start, fullMatch);
            if (start == newStart) {
                if (begin) {
                    // 本次结束
                    end = true;
                    begin = false;
                    result.end = i;
                    result = null;
                }
            } else {
                // 匹配中
                start = newStart;
                if (!begin) {
                    // 开始
                    begin = true;
                    end = false;
                    result = new Result(i, i);
                    resultSet.add(result);
                }
                if (start == targetLength) {
                    // 结束退出
                    result.end = i + 1;
                    break;
                }
            }
        }
        if (start != targetLength) {
            // target未全部匹配时，匹配失败
            resultSet.clear();
        }
        return resultSet;
    }

    /**
     * @param pinyin    单个字的拼音，如shou 区分大小写
     * @param target    要匹配的拼音，如lishouxian 区分大小写
     * @param start     标记从target的第几个字符开始匹配
     * @param fullMatch true：target子串匹配整个pinyin或者pinyin首字母时匹配成功；
     *                  false：匹配pinyin第0个字符开始的任意大于0的字符时匹配成功
     * @return 本次匹配完毕，start游标的位置
     */
    private static int matchPinyin(String pinyin, String target, int start, boolean fullMatch) {
        do {
            if (TextUtils.isEmpty(pinyin) || start < 0 || start >= target.length()) {
                break;
            }
            if (target.charAt(start) != pinyin.charAt(0)) {
                break;
            }
            int pinyinLength = pinyin.length();
            int targetLength = target.length();
            if (fullMatch) {
                if (targetLength >= start + pinyinLength
                        && target.substring(start, start + pinyinLength).equals(pinyin)) {
                    start += pinyinLength;
                } else {
                    start++;
                }
            } else {
                start++;
                for (int i = 1; i < pinyinLength; i++) {
                    if (targetLength > start && pinyin.charAt(i) == target.charAt(start)) {
                        start++;
                    } else {
                        break;
                    }
                }
            }
        } while (false);
        return start;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 字符串的匹配
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static ResultSet matchSentence(String from, String target) {
        return match(from, target, new KMPAlgorithm(), FLAG_MATCH_SENTENCE, false, false, false, null);
    }

    public static ResultSet matchWords(String from, String target) {
        return matchWords(from, target, false, null);
    }

    public static ResultSet matchWords(String from, String target, boolean customSplit, char[] splitChars) {
        return match(from, target, new KMPAlgorithm(), FLAG_MATCH_WORD, false, false, customSplit, splitChars);
    }

    public static ResultSet matchChars(String from, String target) {
        return match(from, target, new KMPAlgorithm(), FLAG_MATCH_CHAR, false, false, false, null);
    }

    /**
     * @param from           源字符串
     * @param target         目标字符串
     * @param algorithm      查找算法
     * @param flag           forceMatchFlag为false时，
     *                       <ul>
     *                       {@link #FLAG_MATCH_SENTENCE}:需要匹配整个target
     *                       </ul>
     *                       <ul>
     *                       {@link #FLAG_MATCH_WORD}:首先执行整串匹配，若匹配则直接返回结果；否则执行单词匹配
     *                       </ul>
     *                       <ul>
     *                       {@link #FLAG_MATCH_CHAR}：首先执行整串匹配，失败执行单词匹配，再次失败则执行单个字符的匹配
     *                       </ul>
     * @param forceMatchFlag true执行精确颗粒度（flag）的匹配；false执行最大颗粒度的匹配
     * @param inOrder        是否按照目标字符串中单词（flag为{@link #FLAG_MATCH_WORD}时）或者字符（flag为
     *                       {@link #FLAG_MATCH_CHAR}时）的顺序进行匹配。flag为
     *                       {@link #FLAG_MATCH_SENTENCE}时无效
     * @param customSplit    flag为{@link #FLAG_MATCH_WORD}
     *                       时，true使用splitChars作为单词分隔符；false使用默认方法分割单词
     * @param splitChars     flag为{@link #FLAG_MATCH_WORD}时，作为单词间的分隔符。若为null，则使用
     *                       {@link #DEFAULT_SPLIT_CHAR}。只在customSplit为true是有效
     */
    public static ResultSet match(String from, String target, IAlgorithm algorithm, int flag, boolean forceMatchFlag,
                                  boolean inOrder, boolean customSplit, char[] splitChars) {
        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(target)) {
            return new ResultSet();
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm不能为空");
        }
        ResultSet resultSet;
        switch (flag) {
            case FLAG_MATCH_SENTENCE:
                resultSet = matchSentence(from, target, algorithm);
                break;
            case FLAG_MATCH_WORD:
                if (forceMatchFlag) {
                    resultSet = matchWords(from, target, algorithm, inOrder, customSplit, splitChars);
                } else {
                    resultSet = matchSentence(from, target, algorithm);
                    if (!resultSet.found()) {
                        resultSet = matchWords(from, target, algorithm, inOrder, customSplit, splitChars);
                    }
                }
                break;
            case FLAG_MATCH_CHAR:
                if (forceMatchFlag) {
                    resultSet = matchCharacters(from, target, algorithm, inOrder);
                } else {
                    resultSet = matchSentence(from, target, algorithm);
                    if (!resultSet.found()) {
                        resultSet = matchWords(from, target, algorithm, inOrder, customSplit, splitChars);
                    }
                    if (!resultSet.found()) {
                        resultSet = matchCharacters(from, target, algorithm, inOrder);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("flag值不合法");
        }
        return resultSet;
    }

    private static ResultSet matchSentence(String from, String target, IAlgorithm algorithm) {
        return algorithm.find(from, target);
    }

    private static ResultSet matchWords(String from, String target, IAlgorithm algorithm, boolean inOrder,
                                        boolean customSplit, char[] splitChars) {
        String[] words = customSplit ? splitTargetCustom(target, splitChars) : splitTargetCommon(target);
        if (words.length == 0) {
            return new ResultSet();
        }
        ResultSet result = null;
        int minStart = 0;
        for (String word : words) {
            if (TextUtils.isEmpty(word)) {
                continue;
            }
            ResultSet resultSet = algorithm.find(from, word);
            if (!resultSet.found()) {
                return resultSet;
            }
            if (result == null) {
                result = new ResultSet();
            }
            if (inOrder) {
                if ((minStart = appendResultSetInOrder(result, resultSet, minStart)) < 0) {
                    return new ResultSet();
                }
            } else {
                result.add(resultSet);
            }
        }
        return result;
    }

    private static ResultSet matchCharacters(String from, String target, IAlgorithm algorithm, boolean inOrder) {
        char[] chs = target.toCharArray();
        ResultSet result = null;
        int minStart = 0;
        for (char c : chs) {
            if (c == ' ') {
                continue;
            }
            ResultSet resultSet = algorithm.find(from, String.valueOf(c));
            if (!resultSet.found()) {
                return resultSet;
            }
            if (result == null) {
                result = new ResultSet();
            }
            if (inOrder) {
                if ((minStart = appendResultSetInOrder(result, resultSet, minStart)) < 0) {
                    return new ResultSet();
                }
            } else {
                result.add(resultSet);
            }
        }
        return result;
    }

    /**
     * 将src添加到dest当中，src中须存在start不小于dest的start的值，否则append操作失败。
     *
     * @param minStart 参与append的src的start必须大于该值
     * @return -1:append失败，否则返回append成功的src的最小的start值
     */
    private static int appendResultSetInOrder(ResultSet dest, ResultSet src, int minStart) {
        if (!dest.found()) {
            dest.add(src);
            return 0;
        }
        int result = -1;
        List<Result> srcResults = src.copyResults();
        List<Result> destResults = dest.copyResults();
        Collection<Result> deletes = new LinkedList<>();
        Collection<Result> adds = new HashSet<>();
        Collection<Result> dels = new HashSet<>();
        for (Result destResult : destResults) {
            boolean needDelete = true;
            for (Result srcResult : srcResults) {
                if (destResult.start <= srcResult.start && minStart <= srcResult.start) {
                    needDelete = false;
                    if (destResult.end >= srcResult.start) {
                        if (destResult.end < srcResult.end) {
                            destResult.end = srcResult.end;
                        }
                        dels.add(srcResult);
                    } else {
                        adds.add(srcResult);
                    }
                    if (result == -1 || srcResult.start < result) {
                        result = srcResult.start;
                    }
                }
            }
            if (needDelete) {
                deletes.add(destResult);
            }
        }
        destResults.removeAll(deletes);

        adds.removeAll(dels);
        destResults.addAll(adds);

        dest.clear();
        dest.add(destResults);
        return result;
    }

    private static String[] splitTargetCommon(String target) {
        String[] arr = target.split("\\b");
        List<String> results = new ArrayList<>(Arrays.asList(arr));
        for (int i = results.size() - 1; i >= 0; i--) {
            String str = results.get(i);
            if (TextUtils.isEmpty(str) || str.length() == 1 && !Character.isLetterOrDigit(str.charAt(0))) {
                results.remove(i);
            }
        }
        return results.toArray(new String[results.size()]);
    }

    private static String[] splitTargetCustom(String target, char[] splitChars) {
        splitChars = getValidSplitChars(splitChars);
        char splitChar = splitChars[0];
        for (int i = 1; i < splitChars.length; i++) {
            String str;
            if (splitChars[i] >= 'a' && splitChars[i] <= 'z' || splitChars[i] >= 'A' && splitChars[i] <= 'Z') {
                str = String.valueOf(splitChars[i]);
            } else {
                str = "\\" + splitChars[i];
            }
            target = target.replaceAll(str, String.valueOf(splitChar));
        }
        return TextUtils.split(target, splitChar);
    }

    private static char[] getValidSplitChars(char[] splitChars) {
        if (splitChars == null || splitChars.length < 1) {
            return new char[]{DEFAULT_SPLIT_CHAR};
        }
        List<Integer> list = null;
        for (int i = 0; i < splitChars.length; i++) {
            if (splitChars[i] == 0) {
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(i);
            }
        }
        if (list == null) {
            return splitChars;
        }
        char[] newSplit = new char[splitChars.length - list.size()];
        if (newSplit.length < 1) {
            return new char[]{DEFAULT_SPLIT_CHAR};
        }
        int j = 0;
        for (int i = 0; i < splitChars.length; i++) {
            if (list.contains(i)) {
                continue;
            }
            newSplit[j++] = splitChars[i];
        }
        return newSplit;
    }
}
