package com.github.bradjacobs.stock.util;

import org.apache.commons.lang3.StringUtils;

public class StringUtil
{
    private StringUtil() {}

    // note: it's possible a library update could remove the need for this
    private static final String NBSP_STRING = "\u00a0";

    public static String cleanWhitespace(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }

        // replace 2 or more adjacent spaces w/ a single space (i.e. "aaa   bbb" -> "aaa bbb")
        //   as well as replace any \n, \r, \t, etc w/ space
        String result = input.replaceAll(NBSP_STRING, " ");
        result = result.replaceAll("\\s+", " ");
        return result.trim();
    }
}
