package com.github.bradjacobs.stock.classifications.cpc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.bradjacobs.stock.classifications.napcs.NapcsRecord;


@JsonPropertyOrder({
        "level1Id",
        "level1Name",
        "level2Id",
        "level2Name",
        "level3Id",
        "level3Name",
        "level4Id",
        "level4Name",
        "level5Id",
        "level5Name",
        "level6Id",
        "level6Name",
})
public class GenericLevelRecord
{
    private String level1Id = "";
    private String level1Name = "";
    private String level2Id = "";
    private String level2Name = "";
    private String level3Id = "";
    private String level3Name = "";
    private String level4Id = "";
    private String level4Name = "";
    private String level5Id = "";
    private String level5Name = "";
    private String level6Id = "";
    private String level6Name = "";

    public String getLevel1Id() {
        return level1Id;
    }

    public void setLevel1Id(String level1Id) {
        this.level1Id = level1Id;
    }

    public String getLevel1Name() {
        return level1Name;
    }

    public void setLevel1Name(String level1Name) {
        this.level1Name = level1Name;
    }

    public String getLevel2Id() {
        return level2Id;
    }

    public void setLevel2Id(String level2Id) {
        this.level2Id = level2Id;
    }

    public String getLevel2Name() {
        return level2Name;
    }

    public void setLevel2Name(String level2Name) {
        this.level2Name = level2Name;
    }

    public String getLevel3Id() {
        return level3Id;
    }

    public void setLevel3Id(String level3Id) {
        this.level3Id = level3Id;
    }

    public String getLevel3Name() {
        return level3Name;
    }

    public void setLevel3Name(String level3Name) {
        this.level3Name = level3Name;
    }

    public String getLevel4Id() {
        return level4Id;
    }

    public void setLevel4Id(String level4Id) {
        this.level4Id = level4Id;
    }

    public String getLevel4Name() {
        return level4Name;
    }

    public void setLevel4Name(String level4Name) {
        this.level4Name = level4Name;
    }

    public String getLevel5Id() {
        return level5Id;
    }

    public void setLevel5Id(String level5Id) {
        this.level5Id = level5Id;
    }

    public String getLevel5Name() {
        return level5Name;
    }

    public void setLevel5Name(String level5Name) {
        this.level5Name = level5Name;
    }

    public String getLevel6Id() {
        return level6Id;
    }

    public void setLevel6Id(String level6Id) {
        this.level6Id = level6Id;
    }

    public String getLevel6Name() {
        return level6Name;
    }

    public void setLevel6Name(String level6Name) {
        this.level6Name = level6Name;
    }

    public boolean isLevelIdSet(int level) {
        if (level == 1) {
            return !level1Id.isEmpty();
        }
        else if (level == 2) {
            return !level2Id.isEmpty();
        }
        else if (level == 3) {
            return !level3Id.isEmpty();
        }
        else if (level == 4) {
            return !level4Id.isEmpty();
        }
        else if (level == 5) {
            return !level5Id.isEmpty();
        }
        else if (level == 6) {
            return !level6Id.isEmpty();
        }
        return false;
    }

    public void setLevelIdName(int level, String codeId, String name)
    {
        if (level == 1) {
            this.level1Id = codeId;
            this.level1Name = name;
        }
        else if (level == 2) {
            this.level2Id = codeId;
            this.level2Name = name;
        }
        else if (level == 3) {
            this.level3Id = codeId;
            this.level3Name = name;
        }
        else if (level == 4) {
            this.level4Id = codeId;
            this.level4Name = name;
        }
        else if (level == 5) {
            this.level5Id = codeId;
            this.level5Name = name;
        }
        else if (level == 6) {
            this.level6Id = codeId;
            this.level6Name = name;
        }
    }

    public GenericLevelRecord copy(int levelsToCopy)
    {
        GenericLevelRecord newRecord = new GenericLevelRecord();
        if (levelsToCopy >= 1) {
            newRecord.level1Id = this.level1Id;
            newRecord.level1Name = this.level1Name;
        }
        if (levelsToCopy >= 2) {
            newRecord.level2Id = this.level2Id;
            newRecord.level2Name = this.level2Name;
        }
        if (levelsToCopy >= 3) {
            newRecord.level3Id = this.level3Id;
            newRecord.level3Name = this.level3Name;
        }
        if (levelsToCopy >= 4) {
            newRecord.level4Id = this.level4Id;
            newRecord.level4Name = this.level4Name;
        }
        if (levelsToCopy >= 5) {
            newRecord.level5Id = this.level5Id;
            newRecord.level5Name = this.level5Name;
        }
        if (levelsToCopy >= 6) {
            newRecord.level6Id = this.level6Id;
            newRecord.level6Name = this.level6Name;
        }
        return newRecord;
    }
}
