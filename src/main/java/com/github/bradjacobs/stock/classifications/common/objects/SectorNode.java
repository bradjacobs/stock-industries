package com.github.bradjacobs.stock.classifications.common.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class SectorNode
{
    @JsonProperty("sectorId")
    private String sectorId;
    @JsonProperty("sectorName")
    private String sectorName;
    @JsonProperty("groups")
    private List<GroupNode> groups = null;

    public SectorNode()
    {
    }

    public SectorNode(String sectorId, String sectorName) {
        this.sectorId = sectorId;
        this.sectorName = sectorName;
    }

    public String getSectorId() {
        return sectorId;
    }

    public void setSectorId(String sectorId) {
        this.sectorId = sectorId;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public List<GroupNode> getGroups() {
        return groups;
    }

    public void addGroup(GroupNode groupNode) {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        groups.add(groupNode);
    }

//    public void setGroups(List<GroupNode> groups) {
//        this.groups = groups;
//    }

}
