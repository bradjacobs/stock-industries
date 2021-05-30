package com.github.bradjacobs.stock.classifications.gics;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
        "sectorId",
        "sectorName",
        "groupId",
        "groupName",
        "industryId",
        "industryName",
        "subIndustryId",
        "subIndustryName",
        "description"
})
public class GicsRecord
{
    private String sectorId;
    private String sectorName;
    private String groupId;
    private String groupName;
    private String industryId;
    private String industryName;
    private String subIndustryId;
    private String subIndustryName;

    private String description;

    public GicsRecord() { }

    public GicsRecord(String sectorId, String sectorName,
                      String groupId, String groupName,
                      String industryId, String industryName,
                      String subIndustryId, String subIndustryName,
                      String description) {
        this.sectorId = sectorId;
        this.sectorName = sectorName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.industryId = industryId;
        this.industryName = industryName;
        this.subIndustryId = subIndustryId;
        this.subIndustryName = subIndustryName;
        this.description = description;
    }

    public String getSectorId() {
        return sectorId;
    }

    public void setSectorId(String sectorId) {
        this.sectorId = sectorId;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
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

    public String getIndustryId() {
        return industryId;
    }

    public void setIndustryId(String industryId) {
        this.industryId = industryId;
    }

    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String industryName) {
        this.industryName = industryName;
    }

    public String getSubIndustryId() {
        return subIndustryId;
    }

    public void setSubIndustryId(String subIndustryId) {
        this.subIndustryId = subIndustryId;
    }

    public String getSubIndustryName() {
        return subIndustryName;
    }

    public void setSubIndustryName(String subIndustryName) {
        this.subIndustryName = subIndustryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
