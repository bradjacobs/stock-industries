package com.github.bradjacobs.stock.classifications.sic;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
        "divisionId",
        "divisionName",
        "majorGroupId",
        "majorGroupName",
        "industryGroupId",
        "industryGroupName",
        "industryId",
        "industryName"
})
public class SicRecord
{
    private String divisionId = "";
    private String divisionName = "";
    private String majorGroupId = "";
    private String majorGroupName = "";
    private String industryGroupId = "";
    private String industryGroupName = "";
    private String industryId = "";
    private String industryName = "";


    public SicRecord() { }


    public SicRecord(String divisionId, String divisionName,
        String majorGroupId, String majorGroupName,
        String industryGroupId, String industryGroupName,
        String industryId, String industryName)
    {
        this.divisionId = divisionId;
        this.divisionName = divisionName;
        this.majorGroupId = majorGroupId;
        this.majorGroupName = majorGroupName;
        this.industryGroupId = industryGroupId;
        this.industryGroupName = industryGroupName;
        this.industryId = industryId;
        this.industryName = industryName;
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

    public String getMajorGroupId()
    {
        return majorGroupId;
    }

    public void setMajorGroupId(String majorGroupId)
    {
        this.majorGroupId = majorGroupId;
    }

    public String getMajorGroupName()
    {
        return majorGroupName;
    }

    public void setMajorGroupName(String majorGroupName)
    {
        this.majorGroupName = majorGroupName;
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
}
