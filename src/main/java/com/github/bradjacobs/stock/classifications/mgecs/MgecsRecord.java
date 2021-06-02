package com.github.bradjacobs.stock.classifications.mgecs;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
        "sectorId",
        "sectorName",
        "industryGroupId",
        "industryGroupName",
        "industryId",
        "industryName"
})
public class MgecsRecord
{
    private String sectorId;
    private String sectorName;
    private String industryGroupId;
    private String industryGroupName;
    private String industryId;
    private String industryName;

    private String description;

    public MgecsRecord() { }


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

    public String getIndustryGroupId() {
        return industryGroupId;
    }

    public void setIndustryGroupId(String industryGroupId) {
        this.industryGroupId = industryGroupId;
    }

    public String getIndustryGroupName() {
        return industryGroupName;
    }

    public void setIndustryGroupName(String industryGroupName) {
        this.industryGroupName = industryGroupName;
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public MgecsRecord copy()
    {
        MgecsRecord newRecord = new MgecsRecord();
        newRecord.sectorId = this.sectorId;
        newRecord.sectorName = this.sectorName;
        newRecord.industryGroupId = this.industryGroupId;
        newRecord.industryGroupName = this.industryGroupName;
        newRecord.industryId = this.industryId;
        newRecord.industryName = this.industryName;
        newRecord.description = this.description;

        return newRecord;
    }


}
