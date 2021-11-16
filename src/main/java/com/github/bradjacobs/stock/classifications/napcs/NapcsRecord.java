package com.github.bradjacobs.stock.classifications.napcs;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * https://www.census.gov/eos/www/napcs/structure.html
 *   names from here:   24 sections, 61 subsections, 172 divisions, 276 groups, 497 subgroups, and 1,167 trilateral products.
 */
@JsonPropertyOrder( {
    "sectionId",
    "sectionName",
    "subSectionId",
    "subSectionName",
    "divisionId",
    "divisionName",
    "groupId",
    "groupName",
    "subGroupId",
    "subGroupName",
    "trilateralProductId",
    "trilateralProductName",
})
public class NapcsRecord
{
    private String sectionId = "";
    private String sectionName = "";

    private String subSectionId = "";
    private String subSectionName = "";

    private String divisionId = "";
    private String divisionName = "";

    private String groupId = "";
    private String groupName = "";

    private String subGroupId = "";
    private String subGroupName = "";

    private String trilateralProductId = "";
    private String trilateralProductName = "";

    public String getSectionId()
    {
        return sectionId;
    }

    public void setSectionId(String sectionId)
    {
        this.sectionId = sectionId;
    }

    public String getSectionName()
    {
        return sectionName;
    }

    public void setSectionName(String sectionName)
    {
        this.sectionName = sectionName;
    }

    public String getSubSectionId()
    {
        return subSectionId;
    }

    public void setSubSectionId(String subSectionId)
    {
        this.subSectionId = subSectionId;
    }

    public String getSubSectionName()
    {
        return subSectionName;
    }

    public void setSubSectionName(String subSectionName)
    {
        this.subSectionName = subSectionName;
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

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getSubGroupId()
    {
        return subGroupId;
    }

    public void setSubGroupId(String subGroupId)
    {
        this.subGroupId = subGroupId;
    }

    public String getSubGroupName()
    {
        return subGroupName;
    }

    public void setSubGroupName(String subGroupName)
    {
        this.subGroupName = subGroupName;
    }

    public String getTrilateralProductId()
    {
        return trilateralProductId;
    }

    public void setTrilateralProductId(String trilateralProductId)
    {
        this.trilateralProductId = trilateralProductId;
    }

    public String getTrilateralProductName()
    {
        return trilateralProductName;
    }

    public void setTrilateralProductName(String trilateralProductName)
    {
        this.trilateralProductName = trilateralProductName;
    }

    public NapcsRecord copy(int levelsToCopy)
    {
        NapcsRecord newRecord = new NapcsRecord();
        if (levelsToCopy >= 1) {
            newRecord.sectionId = this.sectionId;
            newRecord.sectionName = this.sectionName;
        }
        if (levelsToCopy >= 2) {
            newRecord.subSectionId = this.subSectionId;
            newRecord.subSectionName = this.subSectionName;
        }
        if (levelsToCopy >= 3) {
            newRecord.divisionId = this.divisionId;
            newRecord.divisionName = this.divisionName;
        }
        if (levelsToCopy >= 4) {
            newRecord.groupId = this.groupId;
            newRecord.groupName = this.groupName;
        }
        if (levelsToCopy >= 5) {
            newRecord.subGroupId = this.subGroupId;
            newRecord.subGroupName = this.subGroupName;
        }
        if (levelsToCopy >= 6) {
            newRecord.trilateralProductId = this.trilateralProductId;
            newRecord.trilateralProductName = this.trilateralProductName;
        }
        return newRecord;
    }


}
