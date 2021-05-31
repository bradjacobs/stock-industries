package com.github.bradjacobs.stock.classifications.isic;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
        "sectorId",
        "sectorName",
        "divisionId",
        "divisionName",
        "groupId",
        "groupName",
        "classId",
        "className"
})
public class IsicRecord
{
    private String sectorId;
    private String sectorName;
    private String divisionId;
    private String divisionName;
    private String groupId;
    private String groupName;
    private String classId;
    private String className;


    public IsicRecord() { }


    public IsicRecord(String sectorId, String sectorName, String divisionId, String divisionName,
        String groupId, String groupName, String classId, String className)
    {
        this.sectorId = sectorId;
        this.sectorName = sectorName;
        this.divisionId = divisionId;
        this.divisionName = divisionName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.classId = classId;
        this.className = className;
    }

    public String getSectorId()
    {
        return sectorId;
    }

    public void setSectorId(String sectorId)
    {
        this.sectorId = sectorId;
    }

    public String getSectorName()
    {
        return sectorName;
    }

    public void setSectorName(String sectorName)
    {
        this.sectorName = sectorName;
    }

    public String getDivisionId()
    {
        return divisionId;
    }

    public void setDivisionId(String divisionId)
    {
        this.divisionId = divisionId;
    }

    public String getDivisionName()
    {
        return divisionName;
    }

    public void setDivisionName(String divisionName)
    {
        this.divisionName = divisionName;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getClassId()
    {
        return classId;
    }

    public void setClassId(String classId)
    {
        this.classId = classId;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }
}
