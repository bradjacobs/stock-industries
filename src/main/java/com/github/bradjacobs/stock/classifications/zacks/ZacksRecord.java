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
    private String sectorCode;

    @JsonProperty
    @JsonAlias("Sector Group")
    private String sectorName;

    @JsonProperty
    @JsonAlias("Medium(M) Industry Code")
    private String mediumIndustryCode;

    @JsonProperty
    @JsonAlias("Medium(M) Industry Group")
    private String mediumIndustryName;

    @JsonProperty
    @JsonAlias("Expanded(X) Industry Code")
    private String expandedIndustryCode;

    @JsonProperty
    @JsonAlias("Expanded(X) Industry Group")
    private String expandedIndustryName;


    public String getSectorCode() {
        return sectorCode;
    }

    public void setSectorCode(String sectorCode) {
        this.sectorCode = sectorCode;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public String getMediumIndustryCode() {
        return mediumIndustryCode;
    }

    public void setMediumIndustryCode(String mediumIndustryCode) {
        this.mediumIndustryCode = mediumIndustryCode;
    }

    public String getMediumIndustryName() {
        return mediumIndustryName;
    }

    public void setMediumIndustryName(String mediumIndustryName) {
        this.mediumIndustryName = mediumIndustryName;
    }

    public String getExpandedIndustryCode() {
        return expandedIndustryCode;
    }

    public void setExpandedIndustryCode(String expandedIndustryCode) {
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
        int compare = this.sectorCode.compareTo(other.sectorCode);
        if (compare != 0) {
            return compare;
        }
        compare = this.mediumIndustryCode.compareTo(other.mediumIndustryCode);
        if (compare != 0) {
            return compare;
        }
        return this.expandedIndustryCode.compareTo(other.expandedIndustryCode);
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
        return sectorCode.equals(that.sectorCode) &&
            mediumIndustryCode.equals(that.mediumIndustryCode) &&
            expandedIndustryCode.equals(that.expandedIndustryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectorCode, mediumIndustryCode, expandedIndustryCode);
    }
}
