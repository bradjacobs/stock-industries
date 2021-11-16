package com.github.bradjacobs.stock.classifications.cpc;

import java.util.Arrays;


public class AllLevelsRecord
{
    private static final int MAX_LEVEL = 6;
    private static final int MAX_ARRAY_SIZE = MAX_LEVEL + 1;

    private final String[] codeIdLevels = new String[MAX_ARRAY_SIZE];
    private final String[] codeTitleLevels = new String[MAX_ARRAY_SIZE];

    public AllLevelsRecord()
    {
        Arrays.fill(codeIdLevels, "");
        Arrays.fill(codeTitleLevels, "");
    }

    public String getCodeId(int level) {
        if (isValidLevel(level)) {
            return codeIdLevels[level];
        }
        return "";
    }

    public String getCodeTitle(int level) {
        if (isValidLevel(level)) {
            return codeTitleLevels[level];
        }
        return "";
    }

    private boolean isValidLevel(int level) {
        return level >= 1 && level <= MAX_LEVEL;
    }

    public boolean isLevelIdSet(int level) {
        return !getCodeId(level).isEmpty();
    }


    public void setLevelIdTitle(int level, String codeId, String title)
    {
        if (isValidLevel(level)) {
            codeIdLevels[level] = codeId;
            codeTitleLevels[level] = title;
        }
    }

    public AllLevelsRecord copy(int levelsToCopy)
    {
        AllLevelsRecord newRecord = new AllLevelsRecord();

        if (isValidLevel(levelsToCopy)) {
            System.arraycopy(this.codeIdLevels, 0, newRecord.codeIdLevels, 0, levelsToCopy);
            System.arraycopy(this.codeTitleLevels, 0, newRecord.codeTitleLevels, 0, levelsToCopy);
        }
        return newRecord;
    }
}
