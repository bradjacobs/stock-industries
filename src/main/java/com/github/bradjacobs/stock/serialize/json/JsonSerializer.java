package com.github.bradjacobs.stock.serialize.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.serialize.BaseSerializer;
import com.github.bradjacobs.stock.serialize.canonical.CanonicalHeaderUpdater;
import com.github.bradjacobs.stock.serialize.canonical.objects.ActivityNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.GroupNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.IndustryNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SectorNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SubIndustryNode;
import com.github.bradjacobs.stock.types.JsonDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonSerializer extends BaseSerializer
{
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
        JsonMapper jsonMapper = createJsonMapper();

        if (! this.jsonDefinition.isIncludeDescription()) {
            jsonMapper.addMixIn(clazz, NoDescriptionMixin.class);
        }

        String jsonData = null;


        if (this.jsonDefinition.isTree()) {
            jsonData = serializeObjectsToTree(jsonMapper, objectList);
        }
        else {
            //  note:  jsonDefinition.getJsonKeyName()  currently not used for this case.
            jsonData = jsonMapper.writeValueAsString(objectList);
        }

        return jsonData;
    }


    // todo - most likely will move this
    public <T> List<Map<String,String>> convertToListOfMaps(List<T> objectList) throws IOException
    {
        JsonMapper jsonMapper = createJsonMapper();
        String jsonData = jsonMapper.writeValueAsString(objectList);
        List<Map<String, String>> listOfMaps = jsonMapper.readValue(jsonData, new TypeReference<List<Map<String, String>>>() {});
        return listOfMaps;
    }



    protected <T> String serializeObjectsToTree(JsonMapper jsonMapper, List<T> objectList) throws IOException
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
                }
            }
        }

        return sectorNodeList;
    }


    protected JsonMapper createJsonMapper()
    {
        JsonMapper mapper = new JsonMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);  // for now always get pretty indenting

        // note: avoid marshalling out an empty array.
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }

}
