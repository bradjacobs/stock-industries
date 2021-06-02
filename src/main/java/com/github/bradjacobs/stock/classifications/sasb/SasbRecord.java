package com.github.bradjacobs.stock.classifications.sasb;

public class SasbRecord
{
    private String sectorId = "";
    private String sectorName = "";
    private String subSectorId = "";
    private String subSectorName = "";
    private String industryId = "";
    private String industryName = "";

    private String description = null;

    public SasbRecord()
    {
    }

    public SasbRecord(
        String sectorId, String sectorName,
        String subSectorId, String subSectorName,
        String industryId, String industryName)
    {
        this.sectorId = sectorId;
        this.sectorName = sectorName;
        this.subSectorId = subSectorId;
        this.subSectorName = subSectorName;
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
