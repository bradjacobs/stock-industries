package com.github.bradjacobs.stock.classifications.cpc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 */
@JsonPropertyOrder( {
    "sectionId",
    "sectionName",
    "divisionId",
    "divisionName",
    "groupId",
    "groupName",
    "classId",
    "className",
    "subClassId",
    "subClassName",
})
public class CpcRecord
{
    private String sectionId = "";
    private String sectionName = "";

    private String divisionId = "";
    private String divisionName = "";

    private String groupId = "";
    private String groupName = "";

    private String classId = "";
    private String className = "";

    private String subClassId = "";
    private String subClassName = "";

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(String divisionId) {
        this.divisionId = divisionId;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubClassId() {
        return subClassId;
    }

    public void setSubClassId(String subClassId) {
        this.subClassId = subClassId;
    }

    public String getSubClassName() {
        return subClassName;
    }

    public void setSubClassName(String subClassName) {
        this.subClassName = subClassName;
    }

    public CpcRecord copy(int levelsToCopy)
    {
        CpcRecord newRecord = new CpcRecord();
        if (levelsToCopy >= 1) {
            newRecord.sectionId = this.sectionId;
            newRecord.sectionName = this.sectionName;
        }
        if (levelsToCopy >= 2) {
            newRecord.divisionId = this.divisionId;
            newRecord.divisionName = this.divisionName;
        }
        if (levelsToCopy >= 3) {
            newRecord.groupId = this.groupId;
            newRecord.groupName = this.groupName;
        }
        if (levelsToCopy >= 4) {
            newRecord.classId = this.classId;
            newRecord.className = this.className;
        }
        if (levelsToCopy >= 5) {
            newRecord.subClassId = this.subClassId;
            newRecord.subClassName = this.subClassName;
        }
        return newRecord;
    }

}
