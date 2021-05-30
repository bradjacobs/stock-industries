package bwj.stock.classifications.common.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GroupNode
{
    @JsonProperty("groupId")
    private String groupId;

    @JsonProperty("groupName")
    private String groupName;

    @JsonProperty("industries")
    private List<IndustryNode> industries = null;

//    @JsonIgnore
//    private SectorNode parentNode;

    public GroupNode()
    {
    }

    public GroupNode(String groupId, String groupName)
    {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<IndustryNode> getIndustries() {
        return industries;
    }

    public void addIndustry(IndustryNode industryNode) {
        if (industries == null) {
            industries = new ArrayList<>();
        }
        industries.add(industryNode);
    }



//    public SectorNode getParentNode() {
//        return parentNode;
//    }
//
//    public void setParentNode(SectorNode parentNode) {
//        this.parentNode = parentNode;
//    }
}
