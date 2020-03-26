package demo;

import com.sollian.stringmatcher.StringMatcher;
import com.sollian.stringmatcher.result.ResultSet;

public class Demo {
    public static void main(String[] args) {
        String src = "明天天气好就出去，天气不好就在家待着";
        String dst = "天气";
        String[] dstPinyin = {"tianqi"};

        ResultSet set1 = StringMatcher.matchWords(src, dst);
        set1.copyResults().forEach(System.out::println);

        ResultSet set2 = StringMatcher.matchMultiPinyins(dstPinyin, dst);
        set2.copyResults().forEach(System.out::println);
    }
}
