package bwj.stock.classifications.common.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

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
