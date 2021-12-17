package com.github.bradjacobs.stock.classifications.zacks;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

@JsonPropertyOrder( {
        "sectorCode",
        "sectorName",
        "mediumIndustryCode",
        "mediumIndustryName",
        "expandedIndustryCode",
        "expandedIndustryName"
})
public class ZacksRecord implements Comparable<ZacksRecord>
{
    @JsonProperty
    @JsonAlias("Sector Code")
    private int sectorCode;

    @JsonProperty
    @JsonAlias("Sector Group")
    private String sectorName;

    @JsonProperty
    @JsonAlias("Medium(M) Industry Code")
    private int mediumIndustryCode;

    @JsonProperty
    @JsonAlias("Medium(M) Industry Group")
    private String mediumIndustryName;

    @JsonProperty
    @JsonAlias("Expanded(X) Industry Code")
    private int expandedIndustryCode;

    @JsonProperty
    @JsonAlias("Expanded(X) Industry Group")
    private String expandedIndustryName;


    public String getSectorCode() {
        return String.valueOf(sectorCode);
    }

    public void setSectorCode(String sectorCode) {
        setSectorCode(Integer.parseInt(sectorCode));
    }
    public void setSectorCode(int sectorCode) {
        this.sectorCode = sectorCode;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public String getMediumIndustryCode() {
        return String.valueOf(mediumIndustryCode);
    }

    public void setMediumIndustryCode(String mediumIndustryCode) {
        setMediumIndustryCode(Integer.parseInt(mediumIndustryCode));
    }

    public void setMediumIndustryCode(int mediumIndustryCode) {
        this.mediumIndustryCode = mediumIndustryCode;
    }

    public String getMediumIndustryName() {
        return mediumIndustryName;
    }

    public void setMediumIndustryName(String mediumIndustryName) {
        this.mediumIndustryName = mediumIndustryName;
    }

    public String getExpandedIndustryCode() {
        return String.valueOf(expandedIndustryCode);
    }

    public void setExpandedIndustryCode(String expandedIndustryCode) {
        setExpandedIndustryCode(Integer.parseInt(expandedIndustryCode));
    }

    public void setExpandedIndustryCode(int expandedIndustryCode) {
        this.expandedIndustryCode = expandedIndustryCode;
    }

    public String getExpandedIndustryName() {
        return expandedIndustryName;
    }

    public void setExpandedIndustryName(String expandedIndustryName) {
        this.expandedIndustryName = expandedIndustryName;
    }


    @Override
    public int compareTo(ZacksRecord other)
    {
        int compare = Integer.compare(this.sectorCode, other.sectorCode);
        if (compare != 0) {
            return compare;
        }
        compare = Integer.compare(this.mediumIndustryCode, other.mediumIndustryCode);
        if (compare != 0) {
            return compare;
        }
        return Integer.compare(this.expandedIndustryCode, other.expandedIndustryCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ZacksRecord)) {
            return false;
        }
        ZacksRecord that = (ZacksRecord) o;
        return sectorCode == that.sectorCode &&
            mediumIndustryCode == that.mediumIndustryCode &&
            expandedIndustryCode == that.expandedIndustryCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectorCode, mediumIndustryCode, expandedIndustryCode);
    }
}
