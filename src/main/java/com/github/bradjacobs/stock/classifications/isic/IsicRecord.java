package com.github.bradjacobs.stock.classifications.isic;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
        "sectionId",
        "sectionLabel",
        "divisionId",
        "divisionLabel",
        "groupId",
        "groupLabel",
        "classId",
        "classLabel"
})
public class IsicRecord
{
    @JsonAlias("section")
    private String sectionId;
    @JsonAlias("section_label")
    private String sectionLabel;
    @JsonAlias("division")
    private String divisionId;
    @JsonAlias("division_label")
    private String divisionLabel;
    @JsonAlias("group")
    private String groupId;
    @JsonAlias("group_label")
    private String groupLabel;
    @JsonAlias("4-digits")
    private String classId;
    @JsonAlias("description")
    private String classLabel;


    public IsicRecord() { }


    public IsicRecord(String sectionId, String sectionLabel, String divisionId, String divisionLabel,
        String groupId, String groupLabel, String classId, String classLabel)
    {
        this.sectionId = sectionId;
        this.sectionLabel = sectionLabel;
        this.divisionId = divisionId;
        this.divisionLabel = divisionLabel;
        this.groupId = groupId;
        this.groupLabel = groupLabel;
        this.classId = classId;
        this.classLabel = classLabel;
    }

    public String getSectionId()
    {
        return sectionId;
    }

    public void setSectionId(String sectionId)
    {
        this.sectionId = sectionId;
    }

    public String getSectionLabel()
    {
        return sectionLabel;
    }

    public void setSectionLabel(String sectionLabel)
    {
        this.sectionLabel = sectionLabel;
    }

    public String getDivisionId()
    {
        return divisionId;
    }

    public void setDivisionId(String divisionId)
    {
        this.divisionId = divisionId;
    }

    public String getDivisionLabel()
    {
        return divisionLabel;
    }

    public void setDivisionLabel(String divisionLabel)
    {
        this.divisionLabel = divisionLabel;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public String getGroupLabel()
    {
        return groupLabel;
    }

    public void setGroupLabel(String groupLabel)
    {
        this.groupLabel = groupLabel;
    }

    public String getClassId()
    {
        return classId;
    }

    public void setClassId(String classId)
    {
        this.classId = classId;
    }

    public String getClassLabel()
    {
        return classLabel;
    }

    public void setClassLabel(String classLabel)
    {
        this.classLabel = classLabel;
    }
}
