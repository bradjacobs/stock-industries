package com.github.bradjacobs.stock.classifications.nace;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
    "sectionCode",
    "sectionName",
    "divisionCode",
    "divisionName",
    "groupCode",
    "groupName",
    "classCode",
    "className"
})
public class NaceRecord
{
    private String sectionCode = "";
    private String sectionName = "";
    private String divisionCode = "";
    private String divisionName = "";
    private String groupCode = "";
    private String groupName = "";
    private String classCode = "";
    private String className = "";

    public String getSectionCode()
    {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode)
    {
        this.sectionCode = sectionCode;
    }

    public String getSectionName()
    {
        return sectionName;
    }

    public void setSectionName(String sectionName)
    {
        this.sectionName = sectionName;
    }

    public String getDivisionCode()
    {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode)
    {
        this.divisionCode = divisionCode;
    }

    public String getDivisionName()
    {
        return divisionName;
    }

    public void setDivisionName(String divisionName)
    {
        this.divisionName = divisionName;
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    public void setGroupCode(String groupCode)
    {
        this.groupCode = groupCode;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getClassCode()
    {
        return classCode;
    }

    public void setClassCode(String classCode)
    {
        this.classCode = classCode;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public NaceRecord copy(int level)
    {
        NaceRecord newRecord = new NaceRecord();
        if (level >= 1) {
            newRecord.sectionCode = this.sectionCode;
            newRecord.sectionName = this.sectionName;
        }
        if (level >= 2) {
            newRecord.divisionCode = this.divisionCode;
            newRecord.divisionName = this.divisionName;
        }
        if (level >= 3) {
            newRecord.groupCode = this.groupCode;
            newRecord.groupName = this.groupName;
        }
        if (level >= 4) {
            newRecord.classCode = this.classCode;
            newRecord.className = this.className;
        }
        return newRecord;
    }

}
