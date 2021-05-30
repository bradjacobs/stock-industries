package com.github.bradjacobs.stock.classifications.common.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class SubIndustryNode
{
    @JsonProperty("subIndustryId")
    private String subIndustryId;
    @JsonProperty("subIndustryName")
    private String subIndustryName;

    @JsonProperty("activities")
    private List<ActivityNode> activities = null;


    public SubIndustryNode()
    {
    }

    public SubIndustryNode(String subIndustryId, String subIndustryName)
    {
        this.subIndustryId = subIndustryId;
        this.subIndustryName = subIndustryName;
    }

    public String getSubIndustryId() {
        return subIndustryId;
    }

    public void setSubIndustryId(String subIndustryId) {
        this.subIndustryId = subIndustryId;
    }

    public String getSubIndustryName() {
        return subIndustryName;
    }

    public void setSubIndustryName(String subIndustryName) {
        this.subIndustryName = subIndustryName;
    }

    public List<ActivityNode> getActivities()
    {
        return activities;
    }

    public void addActivity(ActivityNode activityNode) {
        if (activities == null) {
            activities = new ArrayList<>();
        }
        activities.add(activityNode);
    }

}
