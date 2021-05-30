package com.github.bradjacobs.stock.classifications.naics;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
    "sectorId",
    "sectorName",
    "subSectorId",
    "subSectorName",
    "industryGroupId",
    "industryGroupName",
    "industryId",
    "industryName"
})
public class NaicsRecord
{
    private String sectorId;
    private String sectorName;
    private String subSectorId;
    private String subSectorName;
    private String industryGroupId;
    private String industryGroupName;
    private String industryId;
    private String industryName;

    public NaicsRecord()
    {
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

    public String getSubSectorId()
    {
        return subSectorId;
    }

    public void setSubSectorId(String subSectorId)
    {
        this.subSectorId = subSectorId;
    }

    public String getSubSectorName()
    {
        return subSectorName;
    }

    public void setSubSectorName(String subSectorName)
    {
        this.subSectorName = subSectorName;
    }

    public String getIndustryGroupId()
    {
        return industryGroupId;
    }

    public void setIndustryGroupId(String industryGroupId)
    {
        this.industryGroupId = industryGroupId;
    }

    public String getIndustryGroupName()
    {
        return industryGroupName;
    }

    public void setIndustryGroupName(String industryGroupName)
    {
        this.industryGroupName = industryGroupName;
    }

    public String getIndustryId()
    {
        return industryId;
    }

    public void setIndustryId(String industryId)
    {
        this.industryId = industryId;
    }

    public String getIndustryName()
    {
        return industryName;
    }

    public void setIndustryName(String industryName)
    {
        this.industryName = industryName;
    }


    public NaicsRecord copy()
    {
        NaicsRecord newRecord = new NaicsRecord();
        newRecord.sectorId = this.sectorId;
        newRecord.sectorName = this.sectorName;
        newRecord.subSectorId = this.subSectorId;
        newRecord.subSectorName = this.subSectorName;
        newRecord.industryGroupId = this.industryGroupId;
        newRecord.industryGroupName = this.industryGroupName;
        newRecord.industryId = this.industryId;
        newRecord.industryName = this.industryName;
        return newRecord;
    }
}