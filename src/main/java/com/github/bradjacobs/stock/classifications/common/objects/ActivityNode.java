package com.github.bradjacobs.stock.classifications.common.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivityNode
{
    @JsonProperty("activityId")
    private String activityId;
    @JsonProperty("activityName")
    private String activityName;

    public ActivityNode()
    {
    }

    public ActivityNode(String activityId, String activityName)
    {
        this.activityId = activityId;
        this.activityName = activityName;
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
