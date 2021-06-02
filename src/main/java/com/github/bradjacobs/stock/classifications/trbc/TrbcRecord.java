package com.github.bradjacobs.stock.classifications.trbc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
        "economicSectorId",
        "economicSectorName",
        "businessSectorId",
        "businessSectorName",
        "industryGroupId",
        "industryGroupName",
        "industryId",
        "industryName",
        "activityId",
        "activityName"
})
public class TrbcRecord
{
    private String economicSectorId;
    private String economicSectorName;
    private String businessSectorId;
    private String businessSectorName;
    private String industryGroupId;
    private String industryGroupName;
    private String industryId;
    private String industryName;
    private String activityId;
    private String activityName;


    public TrbcRecord() { }

    public TrbcRecord(String economicSectorId, String economicSectorName, String businessSectorId, String businessSectorName, String industryGroupId,
        String industryGroupName, String industryId, String industryName, String activityId, String activityName)
    {
        this.economicSectorId = economicSectorId;
        this.economicSectorName = economicSectorName;
        this.businessSectorId = businessSectorId;
        this.businessSectorName = businessSectorName;
        this.industryGroupId = industryGroupId;
        this.industryGroupName = industryGroupName;
        this.industryId = industryId;
        this.industryName = industryName;
        this.activityId = activityId;
        this.activityName = activityName;
    }

    public String getEconomicSectorId()
    {
        return economicSectorId;
    }

    public void setEconomicSectorId(String economicSectorId)
    {
        this.economicSectorId = economicSectorId;
    }

    public String getEconomicSectorName()
    {
        return economicSectorName;
    }

    public void setEconomicSectorName(String economicSectorName)
    {
        this.economicSectorName = economicSectorName;
    }

    public String getBusinessSectorId()
    {
        return businessSectorId;
    }

    public void setBusinessSectorId(String businessSectorId)
    {
        this.businessSectorId = businessSectorId;
    }

    public String getBusinessSectorName()
    {
        return businessSectorName;
    }

    public void setBusinessSectorName(String businessSectorName)
    {
        this.businessSectorName = businessSectorName;
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

    public String getActivityId()
    {
        return activityId;
    }

    public void setActivityId(String activityId)
    {
        this.activityId = activityId;
    }

    public String getActivityName()
    {
        return activityName;
    }

    public void setActivityName(String activityName)
    {
        this.activityName = activityName;
    }
}
