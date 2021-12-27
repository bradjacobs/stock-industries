package com.github.bradjacobs.stock.serialize.canonical.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder( {
        "activityId",
        "activityName",
        "subActivities"
})
public class ActivityNode
{
    @JsonProperty("activityId")
    private String activityId;
    @JsonProperty("activityName")
    private String activityName;

    @JsonProperty("subActivities")
    private List<SubActivityNode> subActivities = null;

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

    public void addSubActivity(SubActivityNode subActivityNode) {
        if (subActivities == null) {
            subActivities = new ArrayList<>();
        }
        subActivities.add(subActivityNode);
    }

}
