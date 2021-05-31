package com.github.bradjacobs.stock.classifications.icb;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {
        "industryCode",
        "industry",
        "superSectorCode",
        "superSector",
        "sectorCode",
        "sector",
        "subSectorCode",
        "subSector",
        "definition"
})
public class IcbRecord
{
    private String industryCode;
    private String industry;
    private String superSectorCode;
    private String superSector;
    private String sectorCode;
    private String sector;
    private String subSectorCode;
    private String subSector;

    private String definition;

    public IcbRecord() { }

    public String getIndustryCode()
    {
        return industryCode;
    }

    public void setIndustryCode(String industryCode)
    {
        this.industryCode = industryCode;
    }

    public String getIndustry()
    {
        return industry;
    }

    public void setIndustry(String industry)
    {
        this.industry = industry;
    }

    public String getSuperSectorCode()
    {
        return superSectorCode;
    }

    public void setSuperSectorCode(String superSectorCode)
    {
        this.superSectorCode = superSectorCode;
    }

    public String getSuperSector()
    {
        return superSector;
    }

    public void setSuperSector(String superSector)
    {
        this.superSector = superSector;
    }

    public String getSectorCode()
    {
        return sectorCode;
    }

    public void setSectorCode(String sectorCode)
    {
        this.sectorCode = sectorCode;
    }

    public String getSector()
    {
        return sector;
    }

    public void setSector(String sector)
    {
        this.sector = sector;
    }

    public String getSubSectorCode()
    {
        return subSectorCode;
    }

    public void setSubSectorCode(String subSectorCode)
    {
        this.subSectorCode = subSectorCode;
    }

    public String getSubSector()
    {
        return subSector;
    }

    public void setSubSector(String subSector)
    {
        this.subSector = subSector;
    }

    public String getDefinition()
    {
        return definition;
    }

    public void setDefinition(String definition)
    {
        this.definition = definition;
    }
}
