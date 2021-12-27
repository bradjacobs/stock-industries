package com.github.bradjacobs.stock.serialize.canonical.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder( {
        "industryId",
        "industryName",
        "subIndustries"
})
public class IndustryNode
{
    @JsonProperty("industryId")
    private String industryId;
    @JsonProperty("industryName")
    private String industryName;
    @JsonProperty("subIndustries")
    private List<SubIndustryNode> subIndustries = null;

    public IndustryNode()
    {
    }

    public IndustryNode(String industryId, String industryName)
    {
        this.industryId = industryId;
        this.industryName = industryName;
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

    public List<SubIndustryNode> getSubIndustries() {
        return subIndustries;
    }

    public void addSubIndustry(SubIndustryNode subIndustryNode) {
        if (subIndustries == null) {
            subIndustries = new ArrayList<>();
        }
        subIndustries.add(subIndustryNode);
    }

}
