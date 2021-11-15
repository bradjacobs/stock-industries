package com.github.bradjacobs.stock.classifications.cpc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


public class GenericLevelRecordv2
{
    private static final int MAX_LEVEL = 6;
    private static final int MAX_ARRAY_SIZE = MAX_LEVEL + 1;

    private final String[] codeIdLevels = new String[MAX_ARRAY_SIZE];
    private final String[] codeNameLevels = new String[MAX_ARRAY_SIZE];

    private String foo = "abc";
    private String bar = "def";

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }

    public GenericLevelRecordv2()
    {
        Arrays.fill(codeIdLevels, "");
        Arrays.fill(codeNameLevels, "");
    }

    public String getCodeId(int level) {
        if (isValidLevel(level)) {
            return codeIdLevels[level];
        }
        return "";
    }

    public String getCodeName(int level) {
        if (isValidLevel(level)) {
            return codeNameLevels[level];
        }
        return "";
    }

    private boolean isValidLevel(int level) {
        return level >= 1 && level <= MAX_LEVEL;
    }

    public boolean isLevelIdSet(int level) {
        return !getCodeId(level).isEmpty();
    }


    public void setLevelIdName(int level, String codeId, String name)
    {
        if (isValidLevel(level)) {
            codeIdLevels[level] = codeId;
            codeNameLevels[level] = name;
        }
    }

    public GenericLevelRecordv2 copy(int levelsToCopy)
    {
        GenericLevelRecordv2 newRecord = new GenericLevelRecordv2();

        if (isValidLevel(levelsToCopy)) {
            System.arraycopy(this.codeIdLevels, 0, newRecord.codeIdLevels, 0, levelsToCopy);
            System.arraycopy(this.codeNameLevels, 0, newRecord.codeNameLevels, 0, levelsToCopy);
        }
        return newRecord;
    }

//    public Map<String,String> generateValueMap()
//    {
//        Map<String,String> resultMap = new LinkedHashMap<>();
//    }

}
