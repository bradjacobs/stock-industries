package com.github.bradjacobs.stock.serialize.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.serialize.BaseSerializer;
import com.github.bradjacobs.stock.serialize.canonical.CanonicalHeaderUpdater;
import com.github.bradjacobs.stock.serialize.canonical.objects.ActivityNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.GroupNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.IndustryNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SectorNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SubActivityNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SubIndustryNode;
import com.github.bradjacobs.stock.types.JsonDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonSerializer extends BaseSerializer
{
    private static final JsonMapper jsonMapper = MapperBuilder.json().build();
    private final JsonDefinition jsonDefinition;

    public JsonSerializer(JsonDefinition jsonDefinition)
    {
        this.jsonDefinition = jsonDefinition;
    }

    @Override
    public String generateFileName(Classification classification)
    {
        return this.jsonDefinition.generateFileName(classification);
    }


    @Override
    protected <T> String serializeObjects(List<T> objectList) throws IOException
    {
        Class<?> clazz = identifyClass(objectList);

        if (! this.jsonDefinition.isIncludeDescription()) {
            jsonMapper.addMixIn(clazz, NoDescriptionMixin.class);
        }

        String jsonString = null;


        if (this.jsonDefinition.isTree()) {
            jsonString = serializeObjectsToTree(objectList);
        }
        else {
            //  note:  jsonDefinition.getJsonKeyName()  currently not used for this case.
            jsonString = jsonMapper.writeValueAsString(objectList);
        }

        return jsonString;
    }


    // todo - most likely will move this
    public <T> List<Map<String,String>> convertToListOfMaps(List<T> objectList) throws IOException
    {
        String jsonData = jsonMapper.writeValueAsString(objectList);
        List<Map<String, String>> listOfMaps = jsonMapper.readValue(jsonData, new TypeReference<List<Map<String, String>>>() {});
        return listOfMaps;
    }



    protected <T> String serializeObjectsToTree(List<T> objectList) throws IOException
    {
        List<Map<String, String>> listOfMaps = convertToListOfMaps(objectList);
        List<SectorNode> sectorNodes = createCanonicalJsonTree(listOfMaps);

        String[] headerRow = new ArrayList<>(listOfMaps.get(0).keySet()).toArray(new String[0]);

        String jsonTree = null;
        String canonicalTreeJson = jsonMapper.writeValueAsString(sectorNodes);

        if (! this.jsonDefinition.getJsonKeyName().equals(JsonDefinition.JsonKeyName.CANONICAL))
        {
            CanonicalHeaderUpdater canonicalHeaderUpdater = new CanonicalHeaderUpdater(headerRow);
            if (this.jsonDefinition.getJsonKeyName().equals(JsonDefinition.JsonKeyName.NORMAL)) {
                jsonTree = canonicalHeaderUpdater.convertToNormalKeyNames(canonicalTreeJson);
            }
            else if (this.jsonDefinition.getJsonKeyName().equals(JsonDefinition.JsonKeyName.BASIC)) {
                jsonTree = canonicalHeaderUpdater.convertToGenericKeyNames(canonicalTreeJson);
            }
        }
        else {
            jsonTree = canonicalTreeJson;
        }

        return jsonTree;
    }


    private List<SectorNode> createCanonicalJsonTree(List<Map<String,String>> listOfMaps) throws IOException
    {
        List<SectorNode> sectorNodeList = new ArrayList<>();
        SectorNode currentSector = new SectorNode("", "");
        GroupNode currentGroup = new GroupNode("", "");
        IndustryNode currentIndustry = new IndustryNode("", "");
        SubIndustryNode currentSubIndustry = new SubIndustryNode("", "");
        ActivityNode currentActivity = new ActivityNode("", "");


        for (Map<String, String> entryMap : listOfMaps)
        {
            List<String> entryValues = new ArrayList<>(entryMap.values());
            String[] rowData = entryValues.toArray(new String[0]);

            if (rowData.length >= 2) {
                SectorNode sectorNode = new SectorNode(rowData[0], rowData[1]);

                if (! sectorNode.getSectorId().equals(currentSector.getSectorId())) {
                    sectorNodeList.add(sectorNode);
                    currentSector = sectorNode;
                }
            }
            if (rowData.length >= 4) {
                GroupNode groupNode = new GroupNode(rowData[2], rowData[3]);

                if (! groupNode.getGroupId().equals(currentGroup.getGroupId())) {
                    currentSector.addGroup(groupNode);
                    currentGroup = groupNode;
                }
            }
            if (rowData.length >= 6) {
                IndustryNode industryNode = new IndustryNode(rowData[4], rowData[5]);

                if (! industryNode.getIndustryId().equals(currentIndustry.getIndustryId())) {
                    currentGroup.addIndustry(industryNode);
                    currentIndustry = industryNode;
                }
            }
            if (rowData.length >= 8) {
                SubIndustryNode subIndustryNode = new SubIndustryNode(rowData[6], rowData[7]);

                if (! subIndustryNode.getSubIndustryId().equals(currentSubIndustry.getSubIndustryId())) {
                    currentIndustry.addSubIndustry(subIndustryNode);
                    currentSubIndustry = subIndustryNode;
                }
            }
            if (rowData.length >= 10) {
                ActivityNode activityNode = new ActivityNode(rowData[8], rowData[9]);

                if (! activityNode.getActivityId().isEmpty()) {
                    currentSubIndustry.addActivity(activityNode);
                    currentActivity = activityNode;
                }
            }
            if (rowData.length >= 12) {
                SubActivityNode subactivityNode = new SubActivityNode(rowData[10], rowData[11]);

                if (! subactivityNode.getSubActivityId().isEmpty()) {
                    currentActivity.addSubActivity(subactivityNode);
                }
            }
        }

        return sectorNodeList;
    }
}
