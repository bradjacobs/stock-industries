package com.github.bradjacobs.stock.serialize.canonical.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubActivityNode
{
    @JsonProperty("subActivityId")
    private String subActivityId;
    @JsonProperty("subActivityName")
    private String subActivityName;

    public SubActivityNode()
    {
    }

    public SubActivityNode(String subActivityId, String subActivityName)
    {
        this.subActivityId = subActivityId;
        this.subActivityName = subActivityName;
    }

    public String getSubActivityId()
    {
        return subActivityId;
    }

    public void setSubActivityId(String subActivityId)
    {
        this.subActivityId = subActivityId;
    }

    public String getSubActivityName()
    {
        return subActivityName;
    }

    public void setSubActivityName(String subActivityName)
    {
        this.subActivityName = subActivityName;
    }
}
