package com.github.bradjacobs.stock.classifications.sitc;

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
    "subGroupId",
    "subGroupName",
    "basicHeadingId",
    "basicHeadingName",
})
public class SitcRecord
{
    private String sectionId = "";
    private String sectionName = "";

    private String divisionId = "";
    private String divisionName = "";

    private String groupId = "";
    private String groupName = "";

    private String subGroupId = "";
    private String subGroupName = "";

    private String basicHeadingId = "";
    private String basicHeadingName = "";

    public SitcRecord()
    {
    }

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

    public String getSubGroupId() {
        return subGroupId;
    }

    public void setSubGroupId(String subGroupId) {
        this.subGroupId = subGroupId;
    }

    public String getSubGroupName() {
        return subGroupName;
    }

    public void setSubGroupName(String subGroupName) {
        this.subGroupName = subGroupName;
    }

    public String getBasicHeadingId() {
        return basicHeadingId;
    }

    public void setBasicHeadingId(String basicHeadingId) {
        this.basicHeadingId = basicHeadingId;
    }

    public String getBasicHeadingName() {
        return basicHeadingName;
    }

    public void setBasicHeadingName(String basicHeadingName) {
        this.basicHeadingName = basicHeadingName;
    }
}
