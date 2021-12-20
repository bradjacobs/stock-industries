package com.github.bradjacobs.stock.classifications.tradingview;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
    "sectorId",
    "sectorName",
    "industryId",
    "industryName"
})
public class TradingViewRecord
{
    private String sectorId;
    private String sectorName;
    private String industryId;
    private String industryName;

    public TradingViewRecord()
    {
    }

    public TradingViewRecord(String sectorId, String sectorName, String industryId, String industryName)
    {
        this.sectorId = sectorId;
        this.sectorName = sectorName;
        this.industryId = industryId;
        this.industryName = industryName;
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
